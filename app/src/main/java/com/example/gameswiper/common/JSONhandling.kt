package com.example.gameswiper.common

import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
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