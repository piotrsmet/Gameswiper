package com.example.gameswiper.composable

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.LogInActivity
import com.example.gameswiper.R
import com.example.gameswiper.model.SettingsViewModel
import com.example.gameswiper.model.SwipeViewModel
import com.example.gameswiper.model.UserViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.AVATARS
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    swipeViewModel: SwipeViewModel,
    settingsViewModel: SettingsViewModel,
    userRepository: UserRepository,
    gamesWrapper: GamesWrapper,
    context: Context,
    isActive: Boolean
) {
    val user by userViewModel.userDisplay.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showPreferencesPicker by remember { mutableStateOf(false) }

    val avatar = AVATARS[user.profilePicture.toInt() % AVATARS.size]

    val stats = listOf(
        Triple("Przesunięto", user.swiped, R.drawable.browsers_svgrepo_com),
        Triple("Polubiono", user.liked, R.drawable.like),
        Triple("Nie polubiono", user.disliked, R.drawable.dislike),
        Triple("Ulubiony gatunek", GENRES.find { it.id == user.favouriteGenre }?.name ?: "Brak", R.drawable.gamepad_svgrepo_com)
    )

    BackHandler(enabled = isActive) {
        if (showPreferencesPicker) {
            showPreferencesPicker = false
        } else if (showAvatarPicker) {
            showAvatarPicker = false
        } else {
            (context as? Activity)?.finish()
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)
                    .padding(top = paddingValues.calculateTopPadding())
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { showPreferencesPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ustawienia",
                            tint = DETAILS_COLOR,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preferencje",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            swipeViewModel.clearDataStore(context)
                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(context, LogInActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as? Activity)?.finish()

                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        ),
                        border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Wyloguj",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier.size(140.dp),
                        color = Color(0xFF1A1A1A),
                        border = BorderStroke(3.dp, DETAILS_COLOR)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(avatar),
                                contentDescription = "Profile Picture",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(90.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showAvatarPicker = true },
                        color = DETAILS_COLOR
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Avatar",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Gracz",
                    color = DETAILS_COLOR,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Statystyki",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Pierwszy wiersz statystyk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = stats[0].first,
                        value = stats[0].second.toString(),
                        iconRes = stats[0].third,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stats[1].first,
                        value = stats[1].second.toString(),
                        iconRes = stats[1].third,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Drugi wiersz statystyk
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = stats[2].first,
                        value = stats[2].second.toString(),
                        iconRes = stats[2].third,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = stats[3].first,
                        value = stats[3].second.toString(),
                        iconRes = stats[3].third,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (showPreferencesPicker) {
                PreferencesPickerDialog(
                    settingsViewModel = settingsViewModel,
                    swipeViewModel = swipeViewModel,
                    userViewModel = userViewModel,
                    gamesWrapper = gamesWrapper,
                    context = context,
                    onDismiss = { showPreferencesPicker = false }
                )
            }

            if (showAvatarPicker) {
                AvatarPickerDialog(
                    currentAvatar = user.profilePicture.toInt(),
                    onDismiss = { showAvatarPicker = false },
                    onAvatarSelected = { index ->
                        userViewModel.updateAvatar(userRepository, index)
                        showAvatarPicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, iconRes: Int, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = DETAILS_COLOR,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AvatarPickerDialog(
    currentAvatar: Int,
    onDismiss: () -> Unit,
    onAvatarSelected: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = false
                ) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wybierz awatar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DETAILS_COLOR
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Awatary w wierszach po 4
                val avatarRows = AVATARS.chunked(4)
                avatarRows.forEachIndexed { rowIndex, rowAvatars ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = if (rowIndex < avatarRows.size - 1) 12.dp else 0.dp)
                    ) {
                        rowAvatars.forEachIndexed { colIndex, _ ->
                            val index = rowIndex * 4 + colIndex
                            val isSelected = index == currentAvatar % AVATARS.size
                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onAvatarSelected(index) },
                                color = if (isSelected) DETAILS_COLOR.copy(alpha = 0.3f) else Color(0xFF2A2A2A),
                                border = if (isSelected) BorderStroke(2.dp, DETAILS_COLOR) else null
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(AVATARS[index]),
                                        contentDescription = "Avatar $index",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A2A2A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Anuluj", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PreferencesPickerDialog(
    settingsViewModel: SettingsViewModel,
    swipeViewModel: SwipeViewModel,
    userViewModel: UserViewModel,
    gamesWrapper: GamesWrapper,
    context: Context,
    onDismiss: () -> Unit
) {
    val genres = GENRES
    val platforms = PLATFORMS
    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }
    val modelGenres by settingsViewModel.selectedGenres.collectAsState()
    val modelPlatforms by settingsViewModel.selectedPlatforms.collectAsState()
    val userRepository = remember { UserRepository() }

    // Kopia początkowych wartości do porównania
    val genresCopy = remember { modelGenres.toSet() }
    val platformsCopy = remember { modelPlatforms.toSet() }

    // Walidacja - minimum 5 gatunków i 1 platforma
    val selectedGenresCount = checkedGenres.count { it.value }
    val selectedPlatformsCount = checkedPlatforms.count { it.value }
    val canSave = selectedGenresCount >= 5 && selectedPlatformsCount >= 1

    LaunchedEffect(Unit) {
        modelGenres.forEach { genreId ->
            genres.find { it.id == genreId }?.let { genre ->
                checkedGenres[genre.name] = true
            }
        }
        modelPlatforms.forEach { platformId ->
            platforms.find { it.id == platformId }?.let { platform ->
                checkedPlatforms[platform.name] = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { }
            )
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.95f)
                .padding(vertical = 40.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            ),
            border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preferencje gier",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DETAILS_COLOR
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Komunikat o wymaganiach
                Text(
                    text = "Wybierz minimum 5 gatunków i 1 platformę",
                    fontSize = 12.sp,
                    color = if (canSave) Color.Gray else Color(0xFFFF6B6B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sekcja gatunków
                Text(
                    text = "Gatunki ($selectedGenresCount/5+)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedGenresCount >= 5) DETAILS_COLOR else Color(0xFFFF6B6B),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(genres) { genre ->
                        val isChecked = checkedGenres[genre.name] ?: false
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    checkedGenres[genre.name] = !isChecked
                                    if (!isChecked) settingsViewModel.addGenre(genre.id)
                                    else settingsViewModel.removeGenre(genre.id)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) DETAILS_COLOR.copy(alpha = 0.3f) else Color(0xFF2A2A2A),
                            border = BorderStroke(
                                width = if (isChecked) 2.dp else 1.dp,
                                color = if (isChecked) DETAILS_COLOR else Color(0xFF3A3A3A)
                            )
                        ) {
                            Text(
                                text = genre.name,
                                color = if (isChecked) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sekcja platform
                Text(
                    text = "Platformy ($selectedPlatformsCount/1+)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selectedPlatformsCount >= 1) DETAILS_COLOR else Color(0xFFFF6B6B),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(platforms) { platform ->
                        val isChecked = checkedPlatforms[platform.name] ?: false
                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    checkedPlatforms[platform.name] = !isChecked
                                    if (!isChecked) settingsViewModel.addPlatform(platform.id)
                                    else settingsViewModel.removePlatform(platform.id)
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isChecked) DETAILS_COLOR.copy(alpha = 0.3f) else Color(0xFF2A2A2A),
                            border = BorderStroke(
                                width = if (isChecked) 2.dp else 1.dp,
                                color = if (isChecked) DETAILS_COLOR else Color(0xFF3A3A3A)
                            )
                        ) {
                            Text(
                                text = platform.name,
                                color = if (isChecked) Color.White else Color.Gray,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Przyciski
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Text("Anuluj", color = Color.White)
                    }

                    Button(
                        onClick = {
                            userRepository.setSettings(modelGenres.toList(), modelPlatforms.toList())
                            // Jeśli zmieniono preferencje, wyczyść karty i preferencje, pobierz nowe gry
                            if (modelPlatforms != platformsCopy || modelGenres != genresCopy) {
                                // Wyczyść aktualne karty gier
                                swipeViewModel.clearGameCards(context)
                                // Wyczyść preferencje użytkownika
                                userViewModel.clearUserPreferences(userRepository)
                                // Pobierz nowe gry według nowych preferencji
                                swipeViewModel.fetchGames(
                                    context,
                                    settingsViewModel.selectedGenres.value.toList(),
                                    settingsViewModel.selectedPlatforms.value.toList(),
                                    gamesWrapper
                                )
                            }
                            onDismiss()
                        },
                        enabled = canSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DETAILS_COLOR,
                            disabledContainerColor = Color(0xFF2A2A2A)
                        )
                    ) {
                        Text(
                            "Zapisz",
                            color = if (canSave) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

