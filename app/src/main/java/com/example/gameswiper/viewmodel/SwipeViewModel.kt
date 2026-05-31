package com.example.gameswiper.viewmodel

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.model.Game
import com.example.gameswiper.model.GameCard
import com.example.gameswiper.network.GamesWrapper
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

    // ===== FETCH GAMES FROM API (OPTIMIZED — 1 request instead of 3) =====

    /**
     * Pobiera gry z API — jedno zapytanie z cover.image_id i videos.video_id
     * zamiast trzech osobnych (games + covers + game_videos).
     */
    fun fetchGames(context: Context, genres: List<Int>, platforms: List<Int>, gamesWrapper: GamesWrapper) {
        viewModelScope.launch {
            try {
                val gameCards = gamesWrapper.wrapGamesWithMedia(context, genres, platforms) ?: return@launch

                _games.value += gameCards.map { it.game }
                _images.update { it + gameCards.map { card -> card.imageUrl } }
                _videos.update { it + gameCards.mapNotNull { card -> card.videoId } }
                _gameCards.update { it + gameCards }

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
                val gameCards = gamesWrapper.wrapGamesWithMedia(context, genres, platforms, idsList) ?: return@launch

                _games.value += gameCards.map { it.game }
                _images.update { it + gameCards.map { card -> card.imageUrl } }
                _videos.update { it + gameCards.mapNotNull { card -> card.videoId } }
                _gameCards.update { it + gameCards }

                Log.i(
                    "GAMES IMAGES VIDEOS2", _games.value.size.toString() + " " +
                            _images.value.size.toString() + " " + _videos.value.size.toString()
                )
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
