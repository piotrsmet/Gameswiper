package com.example.gameswiper.utils

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun ViewModel.launchSafe(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    onError: (Throwable) -> Unit = { e ->
        Log.e("NetworkError", "Błąd sieci: ${e.message}")
    },
    action: suspend () -> Unit
) {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError(throwable)
    }

    viewModelScope.launch(dispatcher + exceptionHandler) {
        action()
    }
}
