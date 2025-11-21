package com.example.gameswiper.common

import android.util.Log
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
            val imageUrl = imageBuilder(imageId, ImageSize.FHD, ImageType.PNG)
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

fun parseJsonToPopularityList(json: String): List<Pair<Int, Int>>{
    val popularityList = mutableListOf<Pair<Int, Int>>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            TODO()
        }
    } catch(e: JSONException){
        e.printStackTrace()
    }
    return popularityList
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

            gameList.add(Game(gameId, cover, video, genres, name, platforms, themes, summary))

        }
    }catch (e: JSONException){
        e.printStackTrace()
    }
    Log.d("vnm", gameList.map{ x -> x.video  }.toString())
    return gameList
}