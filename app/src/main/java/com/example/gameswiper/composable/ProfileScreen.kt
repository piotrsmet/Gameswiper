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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.LogInActivity
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.AVATARS
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.GENRES
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: GamesViewModel,
    userRepository: UserRepository,
    context: Context,
    isActive: Boolean
) {
    val user by viewModel.userDisplay.collectAsState()
    var showAvatarPicker by remember { mutableStateOf(false) }

    val avatar = AVATARS[user.profilePicture.toInt() % AVATARS.size]

    val stats = listOf(
        Triple("Przesunięto", user.swiped, R.drawable.browsers_svgrepo_com),
        Triple("Polubiono", user.liked, R.drawable.like),
        Triple("Nie polubiono", user.disliked, R.drawable.dislike),
        Triple("Ulubiony gatunek", GENRES.find { it.id == user.favouriteGenre }?.name ?: "Brak", R.drawable.gamepad_svgrepo_com)
    )

    BackHandler(enabled = isActive) {
        if (showAvatarPicker) {
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
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            viewModel.clearDataStore(context)
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

            if (showAvatarPicker) {
                AvatarPickerDialog(
                    currentAvatar = user.profilePicture.toInt(),
                    onDismiss = { showAvatarPicker = false },
                    onAvatarSelected = { index ->
                        viewModel.updateAvatar(userRepository, index)
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
