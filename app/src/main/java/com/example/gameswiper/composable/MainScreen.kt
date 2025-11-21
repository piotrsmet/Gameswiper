package com.example.gameswiper.composable

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.SettingsRepository

enum class Screens{
    swipingScreen,
    libraryScreen,
    profileScreen,
    settingsScreen
}

@Composable
fun MainScreen(modifier: Modifier, context: Context, logOut: () -> Unit){

    val viewModel: GamesViewModel = viewModel()
    var current by remember { mutableIntStateOf(1) }
    val wrapper = GamesWrapper()
    wrapper.getStaticToken()
    val gamesRepository = GameRepository()
    val settingsRepository = SettingsRepository()
    var currentScreen: Screens = Screens.swipingScreen

    LaunchedEffect (Unit){viewModel.fetchSettings(settingsRepository, context, wrapper)}

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)){
        Column {
            if(current == 1) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)) {
                    SwipingScreen(modifier, context, wrapper, viewModel, gamesRepository)
                }
            }
            else if(current == 2){
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)) {
                    SettingsScreen(onBackPressed = { current = 1 }, context, logOut, wrapper)
                }
            }
            else if(current == 3){
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)) {
                    LibraryScreen(context, viewModel, gamesRepository, wrapper){}
                }
            }
            else if(current == 4){
                Row(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)) {
                            ProfileScreen(context)
                        }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(2f)){
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {current = 1},
                    shape = RectangleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)) {
                    Icon(Icons.Default.Settings, contentDescription = "settings", tint = Color.White)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {current = 2},
                    shape = RectangleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "settings", tint = Color.White)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {current = 3},
                    shape = RectangleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)) {
                    Icon(Icons.Default.Settings, contentDescription = "settings", tint = Color.White)
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {current = 4},
                    shape = RectangleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "settings", tint = Color.White)
                }
            }
        }
    }
}