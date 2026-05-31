package com.example.gameswiper.network

import android.content.Context
import android.util.Log
import com.api.igdb.apicalypse.APICalypse

import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.api.igdb.request.jsonCovers
import com.api.igdb.request.jsonGameVideos
import com.api.igdb.request.jsonGames
import com.api.igdb.request.jsonGenres
import com.api.igdb.request.jsonPopularityPrimitives
import com.api.igdb.request.jsonThemes
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.TwitchToken
import com.api.igdb.utils.imageBuilder

import com.example.gameswiper.common.parseJsonToGamesListExpanded
import com.example.gameswiper.common.parseJsonToImageList
import com.example.gameswiper.common.parseJsonToPopularityList
import com.example.gameswiper.common.parseJsonToVideosList
import com.example.gameswiper.common.saveToJsonFile

import com.example.gameswiper.model.GameCard
import com.example.gameswiper.utils.ACCESS_TOKEN
import com.example.gameswiper.utils.CLIENT_ID
import com.example.gameswiper.utils.CLIENT_SECRET
import com.example.gameswiper.utils.EXPIRES_IN
import com.example.gameswiper.utils.MAX_GAME_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GamesWrapper{
    var token: TwitchToken? = null

    fun getToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                token = TwitchAuthenticator.requestTwitchToken(CLIENT_ID, CLIENT_SECRET)

                withContext(Dispatchers.Main) {
                    if (token != null) {
                        IGDBWrapper.setCredentials(CLIENT_ID, token!!.access_token)
                        ACCESS_TOKEN = token!!.access_token
                    } else {
                        println("Failed to fetch token.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getStaticToken(){
        token = TwitchToken(ACCESS_TOKEN.toString(), EXPIRES_IN, "bearer")
        IGDBWrapper.setCredentials(CLIENT_ID, token!!.access_token)
    }

    fun wrap(){
        if(token == null){
            println("No token generated yet!")
            return
        }
        var json: String = " "
        CoroutineScope(Dispatchers.IO).launch {
            try {
                json = IGDBWrapper.jsonGames(APICalypse())
                withContext(Dispatchers.Main){
                    print(json)
                }
            } catch(e: Exception){ e.printStackTrace()}
        }

    }

    fun wrapGenres(context: Context){
        if(token == null){
            println("No token generated yet!")
            return
        }
        var json: String = " "
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val apiCalypse = APICalypse()
                    .fields("*")
                json = IGDBWrapper.jsonGenres(apiCalypse)
                withContext(Dispatchers.Main){
                    //print(json)
                    saveToJsonFile(context, "genres.json", json)
                }
            } catch(e: Exception){e.printStackTrace()}
        }

    }

    fun wrapThemes(context: Context){
        if(token == null){
            println("No token generated yet!")
            return
        }
        var json: String = " "
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val apiCalypse = APICalypse()
                    .fields("*")
                    .limit(100)
                json = IGDBWrapper.jsonThemes(apiCalypse)
                withContext(Dispatchers.Main){
                    saveToJsonFile(context, "themes.json", json)
                }
            } catch(e: Exception){e.printStackTrace()}
        }
    }

    // Kept for library use — fetches cover image URLs by cover IDs stored in Firestore
    suspend fun wrapImages(context: Context, imageIds: List<Int>): List<String>? {
        if(token == null){
            println("No token generated yet!")
            return null
        }
        if (imageIds.isEmpty()) return emptyList()

        Log.i("IDS", imageIds.toString())
        val requestString = imageIds.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        Log.i("String", requestString)
        return withContext(Dispatchers.IO) {
            try {
                val apiCalypse = APICalypse()
                    .fields("id, image_id")
                    .where("id = $requestString")
                    .limit(imageIds.size)
                val json = IGDBWrapper.jsonCovers(apiCalypse)
                val images = parseJsonToImageList(json)

                val imageMap = images.associateBy({it.first}, {it.second}) as LinkedHashMap

                imageIds.mapNotNull { imageMap[it] }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }

    // Kept for library use — fetches video IDs by game_video IDs stored in Firestore
    suspend fun wrapVideos(videosIds: List<Int>): List<String>?{
        if(token == null){
            println("No token generated yet!")
            return null
        }
        if (videosIds.isEmpty()) return emptyList()

        val requestString = videosIds.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        return withContext(Dispatchers.IO){
            try {
                val apiCalypse = APICalypse()
                    .fields("id, video_id")
                    .where("id = $requestString")
                    .limit(videosIds.size)
                val json = IGDBWrapper.jsonGameVideos(apiCalypse)
                val videos = parseJsonToVideosList(json)

                val videosMap = videos.associateBy({it.first}, {it.second}) as LinkedHashMap

                videosIds.mapNotNull { videosMap[it] }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Pobiera gry z IGDB w jednym zapytaniu — z cover.image_id i videos.video_id
     * zamiast osobnych zapytań do endpointów /covers i /game_videos.
     * Zwraca od razu listę GameCard gotowych do wyświetlenia.
     */
    suspend fun wrapGamesWithMedia(context: Context, genresList: List<Int>, platformsList: List<Int>): List<GameCard>? {
        if(token == null){
            println("No token generated yet!")
            return null
        }

        val genresString = genresList.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        val platformsString = platformsList.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        val idString = List<Int>(100){
            Random.nextInt(1, MAX_GAME_ID)}
            .joinToString(
                separator = ",",
                prefix = "(",
                postfix = ")"
            )

        return withContext(Dispatchers.IO){
            try{
                // Jedno zapytanie — cover i videos rozwinięte przez dot notation
                val apiCalypse = APICalypse()
                    .fields("id, cover.image_id, videos.video_id, genres, platforms, name, themes, summary, similar_games")
                    .limit(200)
                    .where("id = $idString & genres = $genresString & platforms = $platformsString " +
                            "& themes != null & summary != null & cover != null & similar_games != null " +
                            "& themes != 42")
                val json = IGDBWrapper.jsonGames(apiCalypse)
                val gameCards = parseJsonToGamesListExpanded(json).shuffled()

                // Filtruj po popularności
                val gamesIdString = gameCards
                    .map { it.game.id }
                    .joinToString(
                        separator = ",",
                        prefix = "(",
                        postfix = ")"
                    )

                if (gameCards.isEmpty()) return@withContext emptyList()

                val apiCalypse2 = APICalypse()
                    .fields("id, game_id, value")
                    .where("game_id = $gamesIdString")
                    .limit(500)
                val json2 = IGDBWrapper.jsonPopularityPrimitives(apiCalypse2)
                val popularityList = parseJsonToPopularityList(json2)
                val filteredGameCards = gameCards.filter{ card -> popularityList.any{ pair ->
                    pair.first == card.game.id && pair.second!! > 0.00002
                }}
                Log.i("POPULARITY", filteredGameCards.size.toString())
                filteredGameCards
            } catch(e: Exception){
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Wariant z podaną listą ID gier — też pobiera cover i videos w jednym zapytaniu.
     */
    suspend fun wrapGamesWithMedia(context: Context, genresList: List<Int>,
                          platformsList: List<Int>, idsList: List<Int>): List<GameCard>?{
        if(token == null){
            println("No token generated yet!")
            return null
        }

        val genresString = genresList.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        val platformsString = platformsList.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        val idString = idsList.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")"
        )

        return withContext(Dispatchers.IO){
            try{
                val apiCalypse = APICalypse()
                    .fields("id, cover.image_id, videos.video_id, genres, platforms, name, themes, summary, similar_games")
                    .limit(200)
                    .where("id = $idString & genres = $genresString & platforms = $platformsString " +
                            "& themes != null & summary != null & cover != null & themes != 42")
                val json = IGDBWrapper.jsonGames(apiCalypse)
                parseJsonToGamesListExpanded(json)
            } catch(e: Exception){
                e.printStackTrace()
                null
            }
        }
    }

}