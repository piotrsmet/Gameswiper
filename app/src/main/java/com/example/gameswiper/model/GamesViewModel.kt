package com.example.gameswiper.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.userDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GamesViewModel(): ViewModel() {

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images = _images.asStateFlow()

    private val _videos = MutableStateFlow<List<String>>(emptyList())
    val videos = _videos.asStateFlow()

    private val _gameCards = MutableStateFlow<List<GameCard>>(emptyList())
    val gameCards = _gameCards.asStateFlow()

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

    private val _userPreferences = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val userPreferences = _userPreferences.asStateFlow()

    private val _userDisplay = MutableStateFlow<UserDisplay>(UserDisplay())
    val userDisplay = _userDisplay.asStateFlow()

    private val _savedGames = MutableStateFlow<List<Game>>(emptyList())
    val savedGames = _savedGames.asStateFlow()

    private val dataStoreKey = stringPreferencesKey("CARDS")

    fun fetchUserPreferences(userRepository: UserRepository){
        viewModelScope.launch {
            val prefsRep = userRepository.getUserPreferences()
            _userPreferences.value = prefsRep
        }
    }

    fun saveUserPreferences(userRepository: UserRepository){
        userRepository.setUserPreferences(_userPreferences.value)
    }

    fun updateUserDisplay(userDisplay: UserDisplay){
        _userDisplay.value = userDisplay
    }

    fun clearDataStore(context: Context){
        val dataStore = context.userDataStore
        viewModelScope.launch {
            dataStore.edit { userData ->
                userData[dataStoreKey] = ""
            }
        }
    }

    private fun readCardsFromDataStore(context: Context): Flow<List<Int>> {
        val dataStore = context.userDataStore
        return dataStore.data
            .map{ preferences ->
                val storedCards = preferences[dataStoreKey] ?: ""
                if(storedCards.isBlank())
                    emptyList()
                else{
                    storedCards
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                }

            }

    }


    fun saveCardsToDataStore(context: Context){
        val dataStore = context.userDataStore
        val stringValue = _gameCards.value.map { it.game.id }.joinToString(separator = ",")
        viewModelScope.launch {
            dataStore.edit { userData ->
                userData[dataStoreKey] = stringValue
            }
        }
    }

    private fun updatePositivePreferences(idList: List<Int>){
        _userPreferences.update { currentMap ->
            val newMap = HashMap(currentMap)
            for(id in idList){
                newMap[id.toString()] = (newMap[id.toString()] ?: 0) + 1
            }
            newMap
        }
    }

    fun swipedRight(idList: List<Int>){
        updatePositivePreferences(idList)
        _userDisplay.update { display ->
            display.copy(
                swiped = display.swiped + 1,
                liked = display.liked + 1
            )
        }
    }


    private fun updateNegativePreferences(idList: List<Int>){
        _userPreferences.update{ currentMap ->
            val newMap = HashMap(currentMap)
            for(id in idList){
                val key = id.toString()
                if(newMap[key] != null){
                    if(newMap[key] == 0){
                        newMap.remove(key)
                        continue
                    }
                    newMap[key] = newMap[key]?.minus(1)
                }
            }
            newMap
        }
    }

    fun swipedLeft(idList: List<Int>){
        updateNegativePreferences(idList)
        _userDisplay.update { display ->
            display.copy(
                swiped = display.swiped + 1,
                disliked = display.disliked + 1
            )
        }
    }

    fun getPreferredGameIds(): List<Int>{
        val idList: List<Int> = _userPreferences.value
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map {it.key.toInt()}
            .toList()

        _userPreferences.update { currentMap ->
            val newMap = HashMap(
                currentMap
                    .entries
                    .sortedByDescending { it.value }
                    .drop(10)
                    .associate { it.toPair() }
            )
            newMap
        }

        return idList
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

    fun removeGame(){
        _games.value = _games.value.toMutableList().apply { removeAt(0) }
    }

    fun removeImage(){
        _images.value = _images.value.toMutableList().apply { removeAt(0)}
    }

    fun removeVideo(){
        _videos.value = _videos.value.toMutableList().apply { removeAt(0) }
    }

    fun removeCard(){
        _gameCards.update { it.drop(1) }
    }

    suspend fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper): List<String> {
        val result = gamesWrapper.wrapImages(context, imageIds)
        return result ?: emptyList()
    }

    suspend fun fetchVideos(context: Context, videoIds: List<Int>, gamesWrapper: GamesWrapper): List<String>{
        val result = gamesWrapper.wrapVideos(videoIds)
        return result ?: emptyList()
    }

    fun fetchUserDisplay(userRepository: UserRepository){
        viewModelScope.launch {
            val userDisplayRep = userRepository.getUserDisplay()
            _userDisplay.value = userDisplayRep
        }
    }

    fun setUserDisplay(userRepository: UserRepository, userDisplay: UserDisplay){
        userRepository.setUserDisplay(userDisplay)
        _userDisplay.value = userDisplay
    }

    fun fetchImages2(context: Context, gamesWrapper: GamesWrapper, gameRepository: GameRepository, onComplete: (() -> Unit)? = null){
        viewModelScope.launch {
            val gamesRep = gameRepository.getGames()
            val coversId = gamesRep.map { it.cover }

            val result = if (gamesRep.isNotEmpty()) {
                gamesWrapper.wrapImages(context, coversId)
            } else {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _savedGames.value = gamesRep
                _coverId.value = coversId
                _images2.value = result?.toMutableList() ?: mutableListOf()
                onComplete?.invoke()
            }
        }
    }

    fun fetchSettings(userRepository: UserRepository, context: Context, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val settingsRep = userRepository.getSettings()
            _selectedGenres.value = settingsRep.genres.toSet()
            _selectedPlatforms.value = settingsRep.platforms.toSet()
            val gameIds = readCardsFromDataStore(context).first()
            if(gameIds.isNotEmpty() && settingsRep.genres.isNotEmpty()){
                fetchGames(context, settingsRep.genres, settingsRep.platforms, gamesWrapper, gameIds)
            }
            else if(settingsRep.genres.isNotEmpty()) {
                fetchGames(context, settingsRep.genres, settingsRep.platforms, gamesWrapper)
            }
        }
    }

    fun removeImage2(id: String){
        val index = _images2.value.indexOf(id)
        _images2.value = _images2.value.toMutableList().apply { remove(id) }
        _coverId.value = _coverId.value.toMutableList().apply { removeAt(index) }
    }

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val gamesResult = gamesWrapper.wrapGames(context, genres, platforms) ?: return@launch

            _games.value += gamesResult

            val imageIds = gamesResult.map { it.cover }
            val videoIds = gamesResult.map { it.video }

            val imagesResult = fetchImages(context, imageIds, gamesWrapper)
            val videosResult = fetchVideos(context, videoIds, gamesWrapper)
            _images.update { it + imagesResult }
            _videos.update { it + videosResult }
            Log.i("GAMES IMAGES VIDEOS", _games.value.size.toString() + " " + _images.value.size.toString() + " " + _videos.value.size.toString())
            for (i in gamesResult.indices) {
                val card = GameCard(
                    gamesResult[i],
                    imagesResult[i],
                    videosResult[i]
                )
                _gameCards.update { it + card }


            }
        }
    }

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper, idsList: List<Int>){
        viewModelScope.launch {
            val gamesResult = gamesWrapper.wrapGames(context, genres, platforms, idsList) ?: return@launch

            _games.value += gamesResult

            val imageIds = gamesResult.map { it.cover }
            val videoIds = gamesResult.map { it.video }

            val imagesResult = fetchImages(context, imageIds, gamesWrapper)
            val videosResult = fetchVideos(context, videoIds, gamesWrapper)
            _images.update { it + imagesResult }
            _videos.update { it + videosResult }
            Log.i("GAMES IMAGES VIDEOS", _games.value.size.toString() + " " + _images.value.size.toString() + " " + _videos.value.size.toString())
            for (i in gamesResult.indices) {
                val card = GameCard(
                    gamesResult[i],
                    imagesResult[i],
                    videosResult[i]
                )
                _gameCards.update { it + card }


            }
        }
    }

    fun nextImage(){
        _images.value.let{
            if(it.isNotEmpty()){
                _currentIndex.value = (_currentIndex.value + 1) % 500
            }
        }
    }
}