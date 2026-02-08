package com.example.gameswiper.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel odpowiedzialny za ustawienia użytkownika.
 * Zarządza wybranymi gatunkami i platformami.
 */
class SettingsViewModel : ViewModel() {

    private val _selectedGenres = MutableStateFlow<Set<Int>>(emptySet())
    val selectedGenres = _selectedGenres.asStateFlow()

    private val _selectedPlatforms = MutableStateFlow<Set<Int>>(emptySet())
    val selectedPlatforms = _selectedPlatforms.asStateFlow()

    // ===== FETCH SETTINGS =====
    fun fetchSettings(
        userRepository: UserRepository,
        context: Context,
        gamesWrapper: GamesWrapper,
        swipeViewModel: SwipeViewModel
    ) {
        viewModelScope.launch {
            try {
                val settingsRep = userRepository.getSettings()
                _selectedGenres.value = settingsRep.genres.toSet()
                _selectedPlatforms.value = settingsRep.platforms.toSet()

                // Deleguj pobieranie gier do SwipeViewModel
                swipeViewModel.fetchGamesFromSettings(
                    context,
                    settingsRep.genres,
                    settingsRep.platforms,
                    gamesWrapper
                )
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error fetching settings", e)
            }
        }
    }

    // ===== GENRE OPERATIONS =====
    fun addGenre(id: Int) {
        _selectedGenres.value = _selectedGenres.value.toMutableSet().apply { add(id) }
    }

    fun removeGenre(id: Int) {
        _selectedGenres.value = _selectedGenres.value.toMutableSet().apply { remove(id) }
    }

    // ===== PLATFORM OPERATIONS =====
    fun addPlatform(id: Int) {
        _selectedPlatforms.value = _selectedPlatforms.value.toMutableSet().apply { add(id) }
    }

    fun removePlatform(id: Int) {
        _selectedPlatforms.value = _selectedPlatforms.value.toMutableSet().apply { remove(id) }
    }
}

