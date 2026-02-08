package com.example.gameswiper.common

import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
import com.example.gameswiper.model.Game
import org.json.JSONArray
import org.json.JSONException

fun parseJsonToImageList(json: String): List<Pair<Int, String>>{
    val imageList = mutableListOf<Pair<Int, String>>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val imageId = jsonObject.getString("image_id")
            val imageUrl = imageBuilder(imageId, ImageSize.HD, ImageType.PNG)
            imageList.add(id to imageUrl)
        }
    } catch (e: JSONException){

        e.printStackTrace()
    }
    return imageList
}

fun parseJsonToVideosList(json: String): List<Pair<Int, String>>{
    val videosList = mutableListOf<Pair<Int, String>>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            var videoId = "sss"
            if (jsonObject.has("video_id")){
                videoId = jsonObject.optString("video_id")
            }
            videosList.add(id to videoId)
        }
    } catch(e: JSONException){
        e.printStackTrace()
    }
    return videosList
}

fun parseJsonToPopularityList(json: String): List<Pair<Int, Double?>>{
    val popularityList = mutableListOf<Triple<Int, Int, Double>>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val game_id = jsonObject.getInt("game_id")
            val value = jsonObject.getDouble("value")
            popularityList.add(Triple(id, game_id, value))
        }
    } catch(e: JSONException){
        e.printStackTrace()
    }

    val popularityListFiltered = popularityList.groupBy { it.second }.mapValues { (_, valList) ->
        valList.maxOfOrNull { it.third }
    }.toList()

    return popularityListFiltered
}

fun parseJsonToGamesList(json: String): List<Game>{
    val gameList = mutableListOf<Game>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val gameId = jsonObject.getInt("id")
            val cover = jsonObject.getInt("cover")

            var videoList = JSONArray()
            if(jsonObject.has("videos")){
                videoList = jsonObject.getJSONArray("videos")
            }
            var video = 12653
            if(videoList.length() > 0)
                video = videoList.getInt(0)

            val genresArray = jsonObject.getJSONArray("genres")
            val genres = mutableListOf<Int>()
            for(j in 0 until genresArray.length()){
                genres.add(genresArray.getInt(j))
            }

            val name = jsonObject.getString("name")

            val platformsArray = jsonObject.getJSONArray("platforms")
            val platforms = mutableListOf<Int>()
            for(j in 0 until platformsArray.length()){
                platforms.add(platformsArray.getInt(j))
            }

            val themesArray = jsonObject.getJSONArray("themes")
            val themes = mutableListOf<Int>()
            for(j  in 0 until themesArray.length()){
                themes.add(themesArray.getInt(j))
            }

            val summary = jsonObject.getString("summary")

            val similarGamesArray = jsonObject.getJSONArray("similar_games")
            val similarGames = mutableListOf<Int>()

            for(j in 0 until similarGamesArray.length()){
                similarGames.add(similarGamesArray.getInt(j))
            }

            gameList.add(Game(gameId, cover, video, genres, name, platforms, themes, summary, similarGames))

        }
    }catch (e: JSONException){
        e.printStackTrace()
    }

    return gameList
}