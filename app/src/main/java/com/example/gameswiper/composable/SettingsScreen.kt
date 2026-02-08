package com.example.gameswiper.composable

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.model.SettingsViewModel
import com.example.gameswiper.model.SwipeViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.UserRepository


@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    context: Context,
    logOut: () -> Unit,
    wrapper: GamesWrapper,
    settingsViewModel: SettingsViewModel,
    swipeViewModel: SwipeViewModel
) {
    val genres = GENRES
    val platforms = PLATFORMS
    val backgroundColor = Color(Color.Black.value)
    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }
    val modelGenres by settingsViewModel.selectedGenres.collectAsState()
    val modelPlatforms by settingsViewModel.selectedPlatforms.collectAsState()
    val userRepository = UserRepository()

    val genresCopy = remember { modelGenres.toSet() }
    val platformsCopy = remember { modelPlatforms.toSet() }

    LaunchedEffect(Unit) {
        modelGenres.forEach{ genreId ->
            genres.find { it.id == genreId }?.let { genre ->
                checkedGenres[genre.name] = true
            }
        }
        modelPlatforms.forEach{ platformId ->
            platforms.find {it.id == platformId}?.let { platform ->
                checkedPlatforms[platform.name] = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BackHandler {
            userRepository.setSettings(modelGenres.toList(), modelPlatforms.toList())
            println(modelPlatforms + "platforms " + platformsCopy + " " + modelGenres + " genres" + genresCopy + "!!!!!")
            if(modelPlatforms != platformsCopy.toSet() || modelGenres != genresCopy.toSet()) {
                swipeViewModel.fetchGames(
                    context,
                    settingsViewModel.selectedGenres.value.toList(),
                    settingsViewModel.selectedPlatforms.value.toList(), wrapper
                )
            }
            onBackPressed()
        }
        Box(modifier = Modifier.fillMaxSize().padding(top = 30.dp)) {

            Column(
                modifier = Modifier.padding(10.dp).clip(RoundedCornerShape(10.dp))
            ) {
                Row(modifier = Modifier.background(Color(Color.Black.value))) {
                    LazyVerticalGrid(columns = GridCells.Fixed(3),) {
                        items(genres) { genre ->
                            val isChecked = checkedGenres[genre.name] ?: false
                            Button(
                                modifier = Modifier.padding(4.dp),
                                onClick = { checkedGenres[genre.name] = !isChecked;
                                          if(!isChecked) settingsViewModel.addGenre(genre.id)
                                          else {settingsViewModel.removeGenre(genre.id)}; println(settingsViewModel.selectedGenres.value)},
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(isChecked) Color(0xFF4150B1) else backgroundColor),
                                contentPadding = PaddingValues(5.dp),
                                border = BorderStroke(3.dp, Color(0xFF4100B1))
                            ) {
                                Text(
                                    text = genre.name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                        }
                    }


                }

                Row(modifier = Modifier.background(Color(Color.Black.value)).padding(top=40.dp)) {
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                        items(platforms) { platform ->
                            val isChecked = checkedPlatforms[platform.name] ?: false
                            Button(
                                modifier = Modifier.padding(4.dp),
                                onClick = { checkedPlatforms[platform.name] = !isChecked;
                                          if(!isChecked) settingsViewModel.addPlatform(platform.id)
                                          else settingsViewModel.removePlatform(platform.id)},
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if(isChecked) Color(0xFF4150B1) else backgroundColor),
                                contentPadding = PaddingValues(5.dp),
                                border = BorderStroke(3.dp, Color(0xFF4100B1))
                            ) {
                                Text(
                                    text = platform.name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }

                        }
                    }




                }

                Row(modifier = Modifier.fillMaxWidth().background(Color(Color.Black.value)).padding(top=40.dp), horizontalArrangement = Arrangement.Center) {
                    Button(
                        modifier = Modifier.padding(4.dp),
                        onClick = { swipeViewModel.clearDataStore(context); FirebaseAuth.getInstance().signOut(); logOut() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4100B1)),
                        contentPadding = PaddingValues(5.dp),
                        border = BorderStroke(3.dp, Color(0xFF4100B1))
                    ) {
                        Text(
                            text = "Logout",
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

            }
        }
    }
}



