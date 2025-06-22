package com.example.gameswiper.composable

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.Greeting
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.SettingsRepository
import com.example.gameswiper.ui.theme.GameswiperTheme
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS

@Composable
fun FirstLogin(onSettingsSet: () -> Unit, context: Context) {

    val genres = GENRES
    val platforms = PLATFORMS
    val backgroundColor = Color(0xFF4100B1)
    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }
    val viewModel: GamesViewModel = viewModel()
    val modelGenres by viewModel.selectedGenres.collectAsState()
    val modelPlatforms by viewModel.selectedPlatforms.collectAsState()
    val settingsRepository = SettingsRepository()

    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("SETTINGS", "not_done").apply()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                                    onClick = {
                                        checkedGenres[genre.name] = !isChecked;
                                        if (!isChecked) viewModel.addGenre(genre.id)
                                        else {
                                            viewModel.removeGenre(genre.id)
                                        }; println(viewModel.selectedGenres.value)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isChecked) Color(0xFF4150B1) else backgroundColor
                                    ),
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
                                    onClick = {
                                        checkedPlatforms[platform.name] = !isChecked;
                                        if (!isChecked) viewModel.addPlatform(platform.id)
                                        else viewModel.removePlatform(platform.id)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isChecked) Color(0xFF4150B1) else backgroundColor
                                    ),
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

                    Row(modifier = Modifier.fillMaxWidth().padding(top=40.dp)
                        ,horizontalArrangement = Arrangement.Center){
                        if (modelPlatforms.isNotEmpty() && modelGenres.isNotEmpty()) {
                            Button(
                                onClick = {
                                    settingsRepository.setSettings(
                                        modelGenres.toList(),
                                        modelPlatforms.toList()
                                    );
                                    prefs.edit().putString("SETTINGS", "done").apply()
                                    onSettingsSet()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF4150B1
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text(text = "Proceed", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
