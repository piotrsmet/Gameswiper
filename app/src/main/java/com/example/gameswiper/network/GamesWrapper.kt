package com.example.gameswiper.network

import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.IGDBWrapper.apiJsonRequest
import com.api.igdb.request.TwitchAuthenticator
import com.api.igdb.request.games
import com.api.igdb.request.jsonGames
import com.api.igdb.request.jsonGenres
import com.api.igdb.utils.Endpoints
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.TwitchToken
import com.api.igdb.utils.imageBuilder
import com.example.gameswiper.utils.ACCESS_TOKEN
import com.example.gameswiper.utils.CLIENT_ID
import com.example.gameswiper.utils.CLIENT_SECRET
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

    fun wrapGenres(){
        if(token == null){
            println("No token generated yet!")
            return
        }
        var json: String = " "
        CoroutineScope(Dispatchers.IO).launch{
            try{
                json = IGDBWrapper.jsonGenres(APICalypse())
                withContext(Dispatchers.Main){

                }
            } catch(e: Exception){e.printStackTrace()}
        }

    }

    fun getImage(): String{
        val image_id = "mnljdjtrh44x4snmierh"
        val imageURL = imageBuilder(image_id, ImageSize.SCREENSHOT_HUGE, ImageType.PNG)
        return imageURL
    }

}