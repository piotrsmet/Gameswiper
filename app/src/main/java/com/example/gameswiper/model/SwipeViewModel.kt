package com.example.gameswiper.model

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.userDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel odpowiedzialny za ekran swipe'owania gier.
 * Zarządza kartami gier, obrazami i filmami.
 */
class SwipeViewModel : ViewModel() {

    // ===== GAME CARDS STATE =====
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

    // ===== DATASTORE =====
    private val dataStoreKey = stringPreferencesKey("CARDS")

    // ===== CLEAR GAME CARDS =====
    fun clearGameCards(context: Context) {
        _gameCards.value = emptyList()
        _games.value = emptyList()
        _images.value = emptyList()
        _videos.value = emptyList()
        _currentIndex.value = 0
        clearDataStore(context)
    }

    fun clearDataStore(context: Context) {
        val dataStore = context.userDataStore
        viewModelScope.launch {
            try {
                dataStore.edit { userData ->
                    userData[dataStoreKey] = ""
                }
            } catch (e: Exception) {
                Log.e("SwipeViewModel", "Error clearing Datastore", e)
            }
        }
    }

    fun readCardsFromDataStore(context: Context): Flow<List<Int>> {
        val dataStore = context.userDataStore
        return dataStore.data
            .map { preferences ->
                val storedCards = preferences[dataStoreKey] ?: ""
                if (storedCards.isBlank())
                    emptyList()
                else {
                    storedCards
                        .split(",")
                        .mapNotNull { it.trim().toIntOrNull() }
                }
            }
    }

    fun saveCardsToDataStore(context: Context) {
        val dataStore = context.userDataStore
        val stringValue = _gameCards.value.map { it.game.id }.joinToString(separator = ",")
        viewModelScope.launch {
            try {
                dataStore.edit { userData ->
                    userData[dataStoreKey] = stringValue
                }
            } catch (e: Exception) {
                Log.e("SwipeViewModel", "Error saving to Datastore", e)
            }
        }
    }

    // ===== CARD OPERATIONS =====
    fun removeCard() {
        _gameCards.update { it.drop(1) }
    }

    fun nextImage() {
        _images.value.let {
            if (it.isNotEmpty()) {
                _currentIndex.value = (_currentIndex.value + 1) % 500
            }
        }
    }

    // ===== IMAGE & VIDEO FETCHING =====
    suspend fun fetchImages(context: Context, imageIds: List<Int>, gamesWrapper: GamesWrapper): List<String> {
        return try {
            val result = gamesWrapper.wrapImages(context, imageIds)
            result ?: emptyList()
        } catch (e: Exception) {
            Log.e("SwipeViewModel", "Error fetching images", e)
            emptyList()
        }
    }

    suspend fun fetchVideos(context: Context, videoIds: List<Int>, gamesWrapper: GamesWrapper): List<String> {
        return try {
            val result = gamesWrapper.wrapVideos(videoIds)
            Log.i("VIDEOS FETCHED", result?.toString() ?: "null")
            result ?: emptyList()
        } catch (e: Exception) {
            Log.e("SwipeViewModel", "Error fetching videos", e)
            emptyList()
        }
    }

    // ===== FETCH GAMES FROM API =====
    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper) {
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

                for (i in gamesResult.indices) {
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
                Log.e("SwipeViewModel", "Error fetching games", e)
            }
        }
    }

    fun fetchGames(
        context: Context, genres: List<Int>, platforms: List<Int>,
        gamesWrapper: GamesWrapper, idsList: List<Int>
    ) {
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
                Log.e("SwipeViewModel", "Error fetching games with IDs", e)
            }
        }
    }

    // ===== SETTINGS INTEGRATION =====
    fun fetchGamesFromSettings(
        context: Context,
        genres: List<Int>,
        platforms: List<Int>,
        gamesWrapper: GamesWrapper
    ) {
        viewModelScope.launch {
            try {
                val gameIds = readCardsFromDataStore(context).first()
                Log.i("game ids from datastore", gameIds.toString())
                if (gameIds.isNotEmpty() && genres.isNotEmpty()) {
                    fetchGames(context, genres, platforms, gamesWrapper, gameIds)
                } else if (genres.isNotEmpty()) {
                    fetchGames(context, genres, platforms, gamesWrapper)
                }
            } catch (e: Exception) {
                Log.e("SwipeViewModel", "Error fetching games from settings", e)
            }
        }
    }
}

