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

    private val _savedGamesWithMedia = MutableStateFlow<List<GameWithMedia>>(emptyList())
    val savedGamesWithMedia = _savedGamesWithMedia.asStateFlow()


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
            try {
                dataStore.edit { userData ->
                    userData[dataStoreKey] = ""
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error clearing Datastore", e)
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
            try {
                dataStore.edit { userData ->
                    userData[dataStoreKey] = stringValue
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error saving to Datastore", e)
            }
        }
    }

    // ===== USER PREFERENCES & SETTINGS OPERATIONS =====
    fun fetchUserPreferences(userRepository: UserRepository){
        viewModelScope.launch {
            try {
                val prefsRep = userRepository.getUserPreferences()
                _userPreferences.value = prefsRep
                Log.i("FETCHED PREFS", prefsRep.toString())
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching user preferences", e)
            }
        }
    }

    fun saveUserPreferences(userRepository: UserRepository){
        // To wywołanie nie jest w korutynie w oryginalnym kodzie, ale powinno być bezpieczne
        // jeśli repository obsługuje wątki. Jeśli nie, warto to owinąć w viewModelScope.launch(Dispatchers.IO)
        try {
            userRepository.setUserPreferences(_userPreferences.value)
        } catch (e: Exception) {
            Log.e("GamesViewModel", "Error saving user preferences", e)
        }
    }

    fun updateUserDisplay(userDisplay: UserDisplay){
        val favouriteGenreId = getFavouriteGenre()
        _userDisplay.value = userDisplay.copy(favouriteGenre = favouriteGenreId ?: 0)
    }

    fun getFavouriteGenre(): Int? {
        val genreCounts = mutableMapOf<Int, Int>()

        _savedGamesWithMedia.value.forEach { gameWithMedia ->
            gameWithMedia.game.genres.forEach { genreId ->
                genreCounts[genreId] = (genreCounts[genreId] ?: 0) + 1
            }
        }

        return genreCounts.maxByOrNull { it.value }?.key
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
    fun swipedRight(idList: List<Int>, userRepository: UserRepository){
        updatePositivePreferences(idList)
        val favouriteGenreId = getFavouriteGenre()
        _userDisplay.update { display ->
            display.copy(
                swiped = display.swiped + 1,
                liked = display.liked + 1,
                favouriteGenre = favouriteGenreId ?: display.favouriteGenre
            )
        }

        viewModelScope.launch {
            userRepository.setUserDisplay(_userDisplay.value)
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

    fun swipedLeft(idList: List<Int>, userRepository: UserRepository){
        updateNegativePreferences(idList)
        _userDisplay.update { display ->
            display.copy(
                swiped = display.swiped + 1,
                disliked = display.disliked + 1
            )
        }

        // Zapisz zaktualizowane statystyki do Firebase
        viewModelScope.launch {
            userRepository.setUserDisplay(_userDisplay.value)
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
    // Te metody są suspend, więc obsługę błędów lepiej robić w miejscu wywołania
    // lub tutaj, zwracając pusty wynik. Dla bezpieczeństwa dodaję try-catch.
    suspend fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper): List<String> {
        return try {
            val result = gamesWrapper.wrapImages(context, imageIds)
            result ?: emptyList()
        } catch (e: Exception) {
            Log.e("GamesViewModel", "Error fetching images", e)
            emptyList()
        }
    }

    //ihyrf2jfpIk
    suspend fun fetchVideos(context: Context, videoIds: List<Int>, gamesWrapper: GamesWrapper): List<String>{
        return try {
            val result = gamesWrapper.wrapVideos(videoIds)
            Log.i("VIDEOS FETCHED", result?.toString() ?: "null")
            result ?: emptyList()
        } catch (e: Exception) {
            Log.e("GamesViewModel", "Error fetching videos", e)
            emptyList()
        }
    }

    // ===== USER DISPLAY OPERATIONS =====
    fun fetchUserDisplay(userRepository: UserRepository){
        viewModelScope.launch {
            try {
                val userDisplayRep = userRepository.getUserDisplay()
                _userDisplay.value = userDisplayRep
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching user display", e)
            }
        }
    }

    fun updateAvatar(userRepository: UserRepository, avatarIndex: Int) {
        viewModelScope.launch {
            userRepository.updateAvatar(avatarIndex)
            _userDisplay.update { it.copy(profilePicture = avatarIndex.toString()) }
        }
    }


    fun setUserDisplay(userRepository: UserRepository, userDisplay: UserDisplay){
        viewModelScope.launch {
            try {
                // Przenieś to do IO dla bezpieczeństwa
                withContext(Dispatchers.IO) {
                    userRepository.setUserDisplay(userDisplay)
                }
                _userDisplay.value = userDisplay
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error setting user display", e)
            }
        }
    }

    fun addFriend(userRepository: UserRepository, friendName: String, onResult: (Int) -> Unit) {

        if(friendName == _userDisplay.value.name){
            onResult(3)
            return
        }
        if (_friends.value.any { it.name == friendName }) {
            onResult(2)
            return
        }
        viewModelScope.launch {
            try {
                val newFriend = withContext(Dispatchers.IO) { userRepository.addFriend(friendName) }
                if (newFriend != null) {
                    _friends.update { current -> current + newFriend }
                    onResult(1)
                } else {
                    onResult(0)
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error adding friend", e)
                onResult(0) // Traktuj błąd jako brak znalezienia użytkownika
            }
        }
    }

    fun removeFriend(userRepository: UserRepository, friendName: String){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.deleteFriend(friendName)
                }
                _friends.update { current -> current.filter { it.name != friendName } }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error removing friend", e)
            }
        }
    }

    fun fetchFriends(userRepository: UserRepository){
        viewModelScope.launch {
            try {
                val friendsRep = userRepository.getFriends()
                _friends.value = friendsRep
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching friends", e)
            }
        }
    }

    fun fetchImages2(context: Context, gamesWrapper: GamesWrapper, gameRepository: GameRepository, onComplete: (() -> Unit)? = null){
        viewModelScope.launch {
            try {
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
                    _savedGamesWithMedia.value = gamesRep.mapIndexed { index, game ->
                        GameWithMedia(
                            gameIds[index],
                            game,
                            result?.getOrNull(index) ?: "", // Poprawka na get -> getOrNull dla bezpieczeństwa
                            videos?.getOrNull(index) ?: ""
                        )
                    }
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching library images", e)
                onComplete?.invoke() // Wywołaj callback nawet przy błędzie, żeby UI nie wisiało
            }
        }
    }

    fun fetchSettings(userRepository: UserRepository, context: Context, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            try {
                val settingsRep = userRepository.getSettings()
                _selectedGenres.value = settingsRep.genres.toSet()
                _selectedPlatforms.value = settingsRep.platforms.toSet()
                val gameIds = readCardsFromDataStore(context).first()
                Log.i("game ids from datastore", gameIds.toString())
                if(gameIds.isNotEmpty() && settingsRep.genres.isNotEmpty()){
                    fetchGames(context, settingsRep.genres, settingsRep.platforms, gamesWrapper, gameIds)
                }
                else if(settingsRep.genres.isNotEmpty()) {
                    fetchGames(context, settingsRep.genres, settingsRep.platforms, gamesWrapper)
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching settings", e)
                // W przypadku błędu można tu ustawić jakieś wartości domyślne lub pusty stan
            }
        }
    }

    // ===== LIBRARY OPERATIONS - SAVED GAMES =====
    private fun removeGame(game: Game) {
        _savedGamesWithMedia.update { currentGamesWithMedia ->
            currentGamesWithMedia.filter { it.id != game.id }
        }
    }

    private fun likeGame(id : Int) {
        _savedGamesWithMedia.update { currentGamesWithMedia ->
            currentGamesWithMedia.map {
                if (it.id == id) {
                    it.copy(game = it.game.copy(liked = !it.game.liked))
                } else {
                    it
                }
            }
        }
    }

    fun addGame(context: Context, game: Game, gameRepository: GameRepository, gamesWrapper: GamesWrapper){
        viewModelScope.launch {
            try {
                val gameWithDate = game.copy(dateOfAddition = System.currentTimeMillis())
                gameRepository.addGame(gameWithDate)
                val images = gamesWrapper.wrapImages(
                    context = context,
                    imageIds = mutableListOf(game.cover)
                ) ?: mutableListOf("")
                val video = gamesWrapper.wrapVideos(
                    videosIds = mutableListOf(game.video)
                ) ?: mutableListOf("")
                _savedGamesWithMedia.update { currentGamesWithMedia ->
                    currentGamesWithMedia + GameWithMedia(
                        game.id,
                        gameWithDate,
                        images.getOrElse(0) { "" },
                        video.getOrElse(0) { "" }
                    )
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error adding game", e)
            }
        }
    }
    // ===== FETCH GAMES FROM API =====
    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper){

        viewModelScope.launch {
            try {
                val gamesResult = gamesWrapper.wrapGames(context, genres, platforms) ?: return@launch
                _games.value += gamesResult
                val imageIds = gamesResult.map { it.cover }
                val videoIds = gamesResult.map { it.video }

                if (imageIds.isEmpty() || videoIds.isEmpty()) {
                    return@launch
                }

                val imagesResult = fetchImages(context, imageIds, gamesWrapper)
                val videosResult = fetchVideos(context, videoIds, gamesWrapper)
                _images.update { it + imagesResult }
                _videos.update { it + videosResult }
                Log.i(
                    "GAMES IMAGES VIDEOS", _games.value.size.toString() + " " +
                            _images.value.size.toString() + " " + _videos.value.size.toString()
                )
                for (i in gamesResult.indices) {
                    // Sprawdzenie granic tablicy dla bezpieczeństwa
                    if (i < imagesResult.size && i < videosResult.size) {
                        val card = GameCard(
                            gamesResult[i],
                            imagesResult[i],
                            videosResult[i]
                        )
                        _gameCards.update { it + card }
                    }
                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching games", e)
            }
        }
    }

    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>,
                   gamesWrapper: GamesWrapper, idsList: List<Int>){
        Log.i("FETCH GAMES WITH IDS", idsList.toString())
        viewModelScope.launch {
            try {
                val gamesResult = gamesWrapper.wrapGames(context, genres, platforms, idsList) ?: return@launch

                _games.value += gamesResult

                val imageIds = gamesResult.map { it.cover }
                val videoIds = gamesResult.map { it.video }

                val imagesResult = fetchImages(context, imageIds, gamesWrapper)
                val videosResult = fetchVideos(context, videoIds, gamesWrapper)
                _images.update { it + imagesResult }
                _videos.update { it + videosResult }
                Log.i(
                    "GAMES IMAGES VIDEOS2", _games.value.size.toString() + " " +
                            _images.value.size.toString() + " " + _videos.value.size.toString()
                )
                for (i in gamesResult.indices) {
                    // Sprawdzenie granic tablicy dla bezpieczeństwa
                    if (i < imagesResult.size && i < videosResult.size) {
                        val card = GameCard(
                            gamesResult[i],
                            imagesResult[i],
                            videosResult[i]
                        )
                        _gameCards.update { it + card }
                    }

                }
            } catch (e: Exception) {
                Log.e("GamesViewModel", "Error fetching games with IDs", e)
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

    fun getGameById(gameId: Int): Game? {
        return _savedGamesWithMedia.value.find { it.id == gameId }?.game
    }

    fun deleteGameById(gameId: Int) {
        _savedGamesWithMedia.value.find { it.id == gameId }?.let {
            removeGame(it.game)
        }
    }

    fun toggleLikeById(gameId: Int) {
        likeGame(gameId)
    }
}
