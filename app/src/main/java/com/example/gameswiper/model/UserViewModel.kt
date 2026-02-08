package com.example.gameswiper.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gameswiper.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel odpowiedzialny za dane użytkownika.
 * Zarządza profilem, znajomymi i preferencjami.
 */
class UserViewModel : ViewModel() {

    // ===== USER DISPLAY & PROFILE =====
    private val _userDisplay = MutableStateFlow<UserDisplay>(UserDisplay())
    val userDisplay = _userDisplay.asStateFlow()

    private val _friends = MutableStateFlow<List<UserDisplay>>(emptyList())
    val friends = _friends.asStateFlow()

    // ===== USER PREFERENCES =====
    private val _userPreferences = MutableStateFlow<HashMap<String, Int>>(HashMap())
    val userPreferences = _userPreferences.asStateFlow()

    // ===== CLEAR USER PREFERENCES =====
    fun clearUserPreferences(userRepository: UserRepository) {
        _userPreferences.value = HashMap()
        viewModelScope.launch {
            try {
                userRepository.setUserPreferences(HashMap())
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error clearing user preferences", e)
            }
        }
    }

    // ===== FETCH OPERATIONS =====
    fun fetchUserPreferences(userRepository: UserRepository) {
        viewModelScope.launch {
            try {
                val prefsRep = userRepository.getUserPreferences()
                _userPreferences.value = prefsRep
                Log.i("FETCHED PREFS", prefsRep.toString())
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user preferences", e)
            }
        }
    }

    fun saveUserPreferences(userRepository: UserRepository) {
        try {
            userRepository.setUserPreferences(_userPreferences.value)
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error saving user preferences", e)
        }
    }

    fun fetchUserDisplay(userRepository: UserRepository) {
        viewModelScope.launch {
            try {
                val userDisplayRep = userRepository.getUserDisplay()
                _userDisplay.value = userDisplayRep
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching user display", e)
            }
        }
    }

    fun fetchFriends(userRepository: UserRepository) {
        viewModelScope.launch {
            try {
                val friendsRep = userRepository.getFriends()
                _friends.value = friendsRep
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching friends", e)
            }
        }
    }

    // ===== USER DISPLAY OPERATIONS =====
    fun updateUserDisplay(userDisplay: UserDisplay, favouriteGenreId: Int?) {
        _userDisplay.value = userDisplay.copy(favouriteGenre = favouriteGenreId ?: 0)
    }

    fun updateAvatar(userRepository: UserRepository, avatarIndex: Int) {
        viewModelScope.launch {
            userRepository.updateAvatar(avatarIndex)
            _userDisplay.update { it.copy(profilePicture = avatarIndex.toString()) }
        }
    }

    fun setUserDisplay(userRepository: UserRepository, userDisplay: UserDisplay) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.setUserDisplay(userDisplay)
                }
                _userDisplay.value = userDisplay
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error setting user display", e)
            }
        }
    }

    // ===== FRIENDS OPERATIONS =====
    fun addFriend(userRepository: UserRepository, friendName: String, onResult: (Int) -> Unit) {
        if (friendName == _userDisplay.value.name) {
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
                Log.e("UserViewModel", "Error adding friend", e)
                onResult(0)
            }
        }
    }

    fun removeFriend(userRepository: UserRepository, friendName: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    userRepository.deleteFriend(friendName)
                }
                _friends.update { current -> current.filter { it.name != friendName } }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error removing friend", e)
            }
        }
    }

    // ===== SWIPE STATISTICS =====
    fun swipedRight(idList: List<Int>, userRepository: UserRepository, favouriteGenreId: Int?) {
        updatePositivePreferences(idList)
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

    fun swipedLeft(idList: List<Int>, userRepository: UserRepository) {
        updateNegativePreferences(idList)
        _userDisplay.update { display ->
            display.copy(
                swiped = display.swiped + 1,
                disliked = display.disliked + 1
            )
        }

        viewModelScope.launch {
            userRepository.setUserDisplay(_userDisplay.value)
        }
    }

    private fun updatePositivePreferences(idList: List<Int>) {
        _userPreferences.update { currentMap ->
            val newMap = HashMap(currentMap)
            for (id in idList) {
                newMap[id.toString()] = (newMap[id.toString()] ?: 0) + 1
            }
            newMap
        }
    }

    private fun updateNegativePreferences(idList: List<Int>) {
        _userPreferences.update { currentMap ->
            val newMap = HashMap(currentMap)
            for (id in idList) {
                val key = id.toString()
                if (newMap[key] != null) {
                    if (newMap[key] == 0) {
                        newMap.remove(key)
                        continue
                    }
                    newMap[key] = newMap[key]?.minus(1)
                }
            }
            newMap
        }
    }

    fun getPreferredGameIds(): List<Int> {
        val idList: List<Int> = _userPreferences.value
            .entries
            .sortedByDescending { it.value }
            .take(10)
            .map { it.key.toInt() }
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
}

