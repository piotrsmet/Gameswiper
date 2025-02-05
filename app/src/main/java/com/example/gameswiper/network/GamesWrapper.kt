package com.example.gameswiper.network

import android.content.Context
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.api.igdb.request.jsonCovers
import com.api.igdb.request.jsonGames
import com.api.igdb.request.jsonGenres
import com.api.igdb.request.jsonThemes
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.TwitchToken
import com.api.igdb.utils.imageBuilder
import com.example.gameswiper.common.parseJsonToGamesList
import com.example.gameswiper.common.parseJsonToImageList
import com.example.gameswiper.common.saveToJsonFile
import com.example.gameswiper.model.Game
import com.example.gameswiper.utils.ACCESS_TOKEN
import com.example.gameswiper.utils.CLIENT_ID
import com.example.gameswiper.utils.CLIENT_SECRET
import com.example.gameswiper.utils.EXPIRES_IN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    //print(json)
                    saveToJsonFile(context, "themes.json", json)
                }
            } catch(e: Exception){e.printStackTrace()}
        }
    }

    suspend fun wrapImages(context: Context, imageIds: List<Int>): List<String>? {
        if(token == null){
            println("No token generated yet!")
            return null
        }

        var requestString = "("+ imageIds[0]
        println(imageIds.size)
        for(i in 1..imageIds.size-1){
            requestString += ',' + imageIds[i].toString()
        }
        println(imageIds.size)
        requestString+=")"

        return withContext(Dispatchers.IO) {
            try {
                val apiCalypse = APICalypse()
                    .fields("id, image_id")
                    .where("id = $requestString")
                    .limit(imageIds.size)
                val json = IGDBWrapper.jsonCovers(apiCalypse)
                val images =parseJsonToImageList(json)

                val imageMap = images.associateBy({it.first}, {it.second}) as LinkedHashMap

                imageIds.mapNotNull { imageMap[it] }

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }


    suspend fun wrapGames(context: Context, genresList: List<Int>, platformsList: List<Int>): List<Game>?{
        if(token == null){
            println("No token generated yet!")
            return null
        }

        var genresString = "("+ genresList[0]
        for(i in 1..<genresList.size){
            genresString += ',' + genresList[i].toString()
        }
        genresString+=")"


        var platformsString = "("+ platformsList[0]
        for(i in 1..<platformsList.size){
            platformsString += ',' + platformsList[i].toString()
        }
        platformsString+=")"

        var json: String = " "
        return withContext(Dispatchers.IO){
            try{
                val apiCalypse = APICalypse()
                    .fields("id, cover, genres, platforms, name, themes, summary")
                    .limit(100)
                    .where("genres = $genresString & platforms = $platformsString & themes != null & summary != null & cover != null")
                json = IGDBWrapper.jsonGames(apiCalypse)
                //print(json)
                parseJsonToGamesList(json)
            } catch(e: Exception){
                e.printStackTrace()
                null
            }
        }
    }

    fun getImage(imageId: String): String{
        val imageURL = imageBuilder(imageId, ImageSize.FHD, ImageType.PNG)
        return imageURL
    }

}