package com.example.gameswiper.common

import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
import com.example.gameswiper.model.Game
import org.json.JSONArray
import org.json.JSONException

fun parseJsonToImageList(json: String): List<String>{
    val imageList = mutableListOf<String>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val imageId = jsonObject.getString("image_id")
            val imageUrl = imageBuilder(imageId, ImageSize.FHD, ImageType.PNG)
            imageList.add(imageUrl)
        }
    } catch (e: JSONException){
        e.printStackTrace()
    }
    return imageList
}

fun parseJsonToGamesList(json: String): List<Game>{
    val gameList = mutableListOf<Game>()
    try{
        val jsonArray = JSONArray(json)
        for(i in 0 until jsonArray.length()){
            val jsonObject = jsonArray.getJSONObject(i)
            val gameId = jsonObject.getInt("id")
            val cover = jsonObject.getInt("cover")
            val genresArray = jsonObject.getJSONArray("genres")
            val genres = mutableListOf<Int>()
            for(j  in 0 until genresArray.length()){
                genres.add(genresArray.getInt(j))
            }
            val name = jsonObject.getString("name")
            val platformsArray = jsonObject.getJSONArray("platforms")
            val platforms = mutableListOf<Int>()
            for(j  in 0 until platformsArray.length()){
                platforms.add(platformsArray.getInt(j))
            }
            val themesArray = jsonObject.getJSONArray("themes")
            val themes = mutableListOf<Int>()
            for(j  in 0 until themesArray.length()){
                themes.add(themesArray.getInt(j))
            }
            val summary = jsonObject.getString("summary")

            gameList.add(Game(gameId, cover, genres, name, platforms, themes, summary))

        }
    }catch (e: JSONException){
        e.printStackTrace()
    }
    return gameList
}