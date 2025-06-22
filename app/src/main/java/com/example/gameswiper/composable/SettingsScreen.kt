package com.example.gameswiper.composable

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.LogInActivity
import com.example.gameswiper.R
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.google.firebase.auth.FirebaseAuth
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.SettingsRepository
import kotlinx.coroutines.flow.forEach


@Composable
fun SettingsScreen(onBackPressed: () -> Unit, context: Context, logOut: () -> Unit, wrapper: GamesWrapper){
    val genres = GENRES
    val platforms = PLATFORMS
    val backgroundColor = Color(0xFF4100B1)
    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }
    val viewModel: GamesViewModel = viewModel()
    val modelGenres by viewModel.selectedGenres.collectAsState()
    val modelPlatforms by viewModel.selectedPlatforms.collectAsState()
    val settingsRepository = SettingsRepository()

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
            settingsRepository.setSettings(modelGenres.toList(), modelPlatforms.toList())
            println(modelPlatforms + "platforms " + platformsCopy + " " + modelGenres + " genres" + genresCopy + "!!!!!")
            if(modelPlatforms != platformsCopy.toSet() || modelGenres != genresCopy.toSet()) {
                viewModel.fetchGames(
                    context,
                    viewModel.selectedGenres.value.toList(),
                    viewModel.selectedPlatforms.value.toList(), wrapper
                )
            }
            onBackPressed()
        }
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().padding(top = 30.dp)) {

            Column(
                modifier = Modifier.padding(10.dp).clip(RoundedCornerShape(10.dp))
            ) {
                Row(modifier = Modifier.background(Color(0xFF4635B1))) {
                    LazyVerticalGrid(columns = GridCells.Fixed(3),) {
                        items(genres) { genre ->
                            val isChecked = checkedGenres[genre.name] ?: false
                            Button(
                                modifier = Modifier.padding(4.dp),
                                onClick = { checkedGenres[genre.name] = !isChecked;
                                          if(!isChecked) viewModel.addGenre(genre.id)
                                          else {viewModel.removeGenre(genre.id)}; println(viewModel.selectedGenres.value)},
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

                Row(modifier = Modifier.background(Color(0xFF4635B1)).padding(top=40.dp)) {
                    LazyVerticalGrid(columns = GridCells.Fixed(3)) {
                        items(platforms) { platform ->
                            val isChecked = checkedPlatforms[platform.name] ?: false
                            Button(
                                modifier = Modifier.padding(4.dp),
                                onClick = { checkedPlatforms[platform.name] = !isChecked;
                                          if(!isChecked) viewModel.addPlatform(platform.id)
                                          else viewModel.removePlatform(platform.id)},
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

                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF4635B1)).padding(top=40.dp), horizontalArrangement = Arrangement.Center) {
                    Button(
                        modifier = Modifier.padding(4.dp),
                        onClick = { FirebaseAuth.getInstance().signOut(); logOut() },
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



