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

    // ===== SWINGING SCREEN - IMAGES & VIDEOS =====
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

    // ===== LIBRARY SCREEN - SAVED GAMES & IMAGES =====
    private val _savedGames = MutableStateFlow<List<Game>>(emptyList())
    val savedGames = _savedGames.asStateFlow()

    private val _gameIds = MutableStateFlow<List<Int>>(emptyList())
    val gameIds = _gameIds.asStateFlow()

    private val _images2 = MutableStateFlow<MutableList<String>>(mutableListOf())
    val images2 = _images2.asStateFlow()

    private val _videos2 = MutableStateFlow<MutableList<String>>(mutableListOf())
    val videos2 = _videos2.asStateFlow()

    // ===== USER PREFERENCES & SETTINGS =====
    private val _selectedGenres = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenres = _selectedGenres.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlatforms = _selectedPlatforms.asStateFlow()

    private val _userPreferences = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val userPreferences = _userPreferences.asStateFlow()

    // ===== USER DISPLAY & PROFILE =====
    private val _userDisplay = MutableStateFlow<UserDisplay>(UserDisplay())
    val userDisplay = _userDisplay.asStateFlow()

    private val _friends = MutableStateFlow<List<UserDisplay>>(emptyList())
    val friends = _friends.asStateFlow()

    // ===== DATASTORE =====
    private val dataStoreKey = stringPreferencesKey("CARDS")

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

    // ===== USER PREFERENCES & SETTINGS OPERATIONS =====
    fun fetchUserPreferences(userRepository: UserRepository){
        viewModelScope.launch {
            val prefsRep = userRepository.getUserPreferences()
            _userPreferences.value = prefsRep
            Log.i("FETCHED PREFS", prefsRep.toString())
        }
    }

    fun saveUserPreferences(userRepository: UserRepository){
        userRepository.setUserPreferences(_userPreferences.value)
    }

    fun updateUserDisplay(userDisplay: UserDisplay){
        _userDisplay.value = userDisplay
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

    // ===== SWIPE OPERATIONS & STATISTICS =====
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

    // ===== GENRE & PLATFORM SELECTION =====
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


    fun removeCard(){
        _gameCards.update { it.drop(1) }
    }

    // ===== IMAGE & VIDEO FETCHING =====
    suspend fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper): List<String> {
        val result = gamesWrapper.wrapImages(context, imageIds)
        return result ?: emptyList()
    }

    suspend fun fetchVideos(context: Context, videoIds: List<Int>, gamesWrapper: GamesWrapper): List<String>{
        val result = gamesWrapper.wrapVideos(videoIds)
        return result ?: emptyList()
    }

    // ===== USER DISPLAY OPERATIONS =====
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

    fun addFriend(userRepository: UserRepository, friendId: String){
        viewModelScope.launch{
            val newFriend = userRepository.addFriend(friendId)
            if(newFriend != null){
                _friends.update { currentFriends ->
                    currentFriends + newFriend
                }
            }
        }
    }

    fun fetchFriends(userRepository: UserRepository){
        viewModelScope.launch {
            val friendsRep = userRepository.getFriends()
            _friends.value = friendsRep
        }
    }

    fun fetchImages2(context: Context, gamesWrapper: GamesWrapper, gameRepository: GameRepository, onComplete: (() -> Unit)? = null){
        viewModelScope.launch {
            val gamesRep = gameRepository.getGames()
            val gameIds = gamesRep.map { it.id }

            val result = if (gamesRep.isNotEmpty()) {
                gamesWrapper.wrapImages(context, gamesRep.map { it.cover })
            } else {
                emptyList()
            }

            val videos = if (gamesRep.isNotEmpty()) {
                gamesWrapper.wrapVideos(gamesRep.map { it.video })
            } else {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _savedGames.value = gamesRep
                _gameIds.value = gameIds
                _images2.value = result?.toMutableList() ?: mutableListOf()
                _videos2.value = videos?.toMutableList() ?: mutableListOf()
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

    // ===== LIBRARY OPERATIONS - SAVED GAMES =====
    private fun removeGame(game: Game) {
        _savedGames.update { currentGames ->
            currentGames.filter { it.id != game.id }
        }
        _images2.update { currentImages ->
            currentImages.toMutableList().apply {
                val index = _gameIds.value.indexOf(game.id)
                if (index >= 0) {
                    removeAt(index)
                }
            }
        }
        _gameIds.update { currentIds ->
            currentIds.filter { it != game.id }
        }
    }

    private fun likeGame(id : Int) {
        _savedGames.update { currentGames ->
            currentGames.map {
                if (it.id == id) {
                    it.copy(liked = !it.liked)
                } else {
                    it
                }
            }
        }
    }

    fun addGame(context: Context, game: Game, gameRepository: GameRepository, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            gameRepository.addGame(game)
            val images = gamesWrapper.wrapImages(
                context = context,
                imageIds = mutableListOf(game.cover)
            ) ?: mutableListOf("")
            _images2.update { currentImages ->
                (currentImages + images[0]).toMutableList()
            }
            _savedGames.update { currentGames ->
                currentGames + game
            }
            _gameIds.update { currentIds ->
                currentIds + game.id
            }
        }
    }
    // ===== FETCH GAMES FROM API =====
    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            val gamesResult = gamesWrapper.wrapGames(context, genres, platforms) ?: return@launch

            _games.value += gamesResult

            val imageIds = gamesResult.map { it.cover }
            val videoIds = gamesResult.map { it.video }

            if(imageIds.isEmpty() || videoIds.isEmpty()){
                return@launch
            }

            val imagesResult = fetchImages(context, imageIds, gamesWrapper)
            val videosResult = fetchVideos(context, videoIds, gamesWrapper)
            _images.update { it + imagesResult }
            _videos.update { it + videosResult }
            Log.i("GAMES IMAGES VIDEOS", _games.value.size.toString() + " " +
                    _images.value.size.toString() + " " + _videos.value.size.toString())
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

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>,
                   gamesWrapper: GamesWrapper, idsList: List<Int>){
        viewModelScope.launch {
            val gamesResult = gamesWrapper.wrapGames(context, genres, platforms, idsList) ?: return@launch

            _games.value += gamesResult

            val imageIds = gamesResult.map { it.cover }
            val videoIds = gamesResult.map { it.video }

            val imagesResult = fetchImages(context, imageIds, gamesWrapper)
            val videosResult = fetchVideos(context, videoIds, gamesWrapper)
            _images.update { it + imagesResult }
            _videos.update { it + videosResult }
            Log.i("GAMES IMAGES VIDEOS2", _games.value.size.toString() + " " +
                    _images.value.size.toString() + " " + _videos.value.size.toString())
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

    // ===== HELPER METHODS =====

    /**
     * Pobiera grę po jej Game ID
     */
    fun getGameById(gameId: Int): Game? {
        return _savedGames.value.find { it.id == gameId }
    }

    /**
     * Usuwa grę po Game ID
     */
    fun deleteGameById(gameId: Int) {
        _savedGames.value.find { it.id == gameId }?.let { game ->
            removeGame(game)
        }
    }

    /**
     * Zmienia stan like dla gry po Game ID
     */
    fun toggleLikeById(gameId: Int) {
        likeGame(gameId)
    }
}