package com.example.gameswiper.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.SettingsRepository
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

    private val _images2 = MutableStateFlow<MutableList<String>>(mutableListOf())
    val images2 = _images2.asStateFlow()

    private val _coverId = MutableStateFlow<List<Int>>(emptyList())
    val coverId = _coverId.asStateFlow()

    private val _selectedGenres = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenres = _selectedGenres.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlatforms = _selectedPlatforms.asStateFlow()

    private val _offSetXValue = MutableStateFlow(0f)
    val offSetXValue = _offSetXValue.asStateFlow()

    fun update0ffset(value: Float){
        _offSetXValue.value = value
    }

    fun addPlatform(id: Int){
        _selectedPlatforms.value = _selectedPlatforms.value.toMutableSet().apply { add(id) }
    }

    fun addGenre(id: Int){
        _selectedGenres.value = _selectedGenres.value.toMutableSet().apply { add(id) }
    }

    fun removePlatform(id: Int){
        _selectedPlatforms.value = _selectedPlatforms.value.toMutableSet().apply { remove(id) }
    }

    fun removeGenre(id: Int){
        _selectedGenres.value = _selectedGenres.value.toMutableSet().apply { remove(id) }
    }

    fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val result = gamesWrapper.wrapImages(context, imageIds)
            if (result != null) {
                for(i in result){
                    println(i)
                }
                for(i in imageIds){
                    println(i)
                }
                _images.value = result
                print(images.value)
            }
        }
    }
    fun fetchImages2(context: Context, gamesWrapper: GamesWrapper, gameRepository: GameRepository){
        viewModelScope.launch {
            val gamesRep = gameRepository.getGames()
            val coversId = mutableListOf<Int>()
            for(game in gamesRep){
                coversId.add(game.cover)
                _coverId.value = coversId
            }
            if(gamesRep.isNotEmpty()) {
                val result = gamesWrapper.wrapImages(context, coversId)
                println(result)
                if (result != null) {
                    _images2.value = result as MutableList<String>
                    println(_images2.value.size)
                }
            }
        }
    }

    fun fetchSettings(settingsRepository: SettingsRepository, context: Context, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val settingsRep = settingsRepository.getSettings()
            _selectedGenres.value = settingsRep.genres.toSet()
            _selectedPlatforms.value = settingsRep.platforms.toSet()
            if(settingsRep.genres.isNotEmpty()) {
                fetchGames(context, settingsRep.genres, settingsRep.platforms, gamesWrapper)
            }
        }
    }

    fun removeImage(id: String){
        val index = _images2.value.indexOf(id)
        _images2.value = _images2.value.toMutableList().apply { remove(id) }
        _coverId.value = _coverId.value.toMutableList().apply { removeAt(index) }
    }

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val result = gamesWrapper.wrapGames(context, genres, platforms)
            if(result != null){
                _games.value = result
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
                _currentIndex.value = (_currentIndex.value + 1) % 500
            }
        }
    }
}