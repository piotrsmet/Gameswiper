package com.example.gameswiper.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel odpowiedzialny za bibliotekę zapisanych gier.
 * Zarządza listą zapisanych gier wraz z mediami.
 */
class LibraryViewModel : ViewModel() {

    private val _savedGamesWithMedia = MutableStateFlow<List<GameWithMedia>>(emptyList())
    val savedGamesWithMedia = _savedGamesWithMedia.asStateFlow()

    // ===== FETCH SAVED GAMES =====
    fun fetchSavedGames(
        context: Context,
        gamesWrapper: GamesWrapper,
        gameRepository: GameRepository,
        onComplete: (() -> Unit)? = null
    ) {
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
                            result?.getOrNull(index) ?: "",
                            videos?.getOrNull(index) ?: ""
                        )
                    }
                    onComplete?.invoke()
                }
            } catch (e: Exception) {
                Log.e("LibraryViewModel", "Error fetching library images", e)
                onComplete?.invoke()
            }
        }
    }

    // ===== GAME OPERATIONS =====
    fun addGame(
        context: Context,
        game: Game,
        gameRepository: GameRepository,
        gamesWrapper: GamesWrapper
    ) {
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
                Log.e("LibraryViewModel", "Error adding game", e)
            }
        }
    }

    private fun removeGame(game: Game) {
        _savedGamesWithMedia.update { currentGamesWithMedia ->
            currentGamesWithMedia.filter { it.id != game.id }
        }
    }

    private fun likeGame(id: Int) {
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

    fun getFavouriteGenre(): Int? {
        val genreCounts = mutableMapOf<Int, Int>()

        _savedGamesWithMedia.value.forEach { gameWithMedia ->
            gameWithMedia.game.genres.forEach { genreId ->
                genreCounts[genreId] = (genreCounts[genreId] ?: 0) + 1
            }
        }

        return genreCounts.maxByOrNull { it.value }?.key
    }
}

