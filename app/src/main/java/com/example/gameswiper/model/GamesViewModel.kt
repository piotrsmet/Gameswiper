package com.example.gameswiper.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GamesViewModel: ViewModel() {
    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images = _images.asStateFlow()
    private val _currentImage = MutableStateFlow<String?>(null)
    val currentImage = _currentImage.asStateFlow()
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _games = MutableStateFlow<List<Game>>(emptyList())
    val games = _games.asStateFlow()

    fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val result = gamesWrapper.wrapImages(context, imageIds)
            if (result != null) {
                _images.value = result
                print(images.value)
            }
        }
    }

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val result = gamesWrapper.wrapGames(context, genres, platforms)
            if(result != null){
                _games.value = result
                for(i in result){
                    println(i.name)
                    println(i.genres)
                    println(i.platforms)
                }
                val imagesList = mutableListOf<Int>()
                for(i in result.indices){
                    imagesList.add(result[i].cover)
                }
                fetchImages(context, imagesList, gamesWrapper)
            }
        }
    }

    fun add(value: Int){
        _currentIndex.value += value
    }

    fun nextImage(){
        _images.value.let{
            if(it.isNotEmpty()){
                _currentIndex.value = (_currentIndex.value + 1)
            }
        }
    }
}