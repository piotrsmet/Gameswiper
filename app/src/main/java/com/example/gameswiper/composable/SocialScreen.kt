package com.example.gameswiper.composable

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.model.UserDisplay
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.AVATARS
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.GENRES


@Composable
fun SocialScreen(
    viewModel: GamesViewModel,
    userRepository: UserRepository,
    isActive: Boolean
) {
    val context = LocalContext.current
    var addFriendNickname by remember { mutableStateOf("") }
    var addFriendButtonPressed by remember { mutableStateOf(false) }
    val friendsList by viewModel.friends.collectAsState()
    var addFriendResponseCode by remember { mutableStateOf(4) }
    var selectedFriend by remember { mutableStateOf<UserDisplay?>(null) }

    BackHandler(enabled = isActive) {
        if (selectedFriend != null) {
            selectedFriend = null
        } else if (addFriendButtonPressed) {
            addFriendResponseCode = 4
            addFriendButtonPressed = false
        } else {
            (context as? Activity)?.finish()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = { addFriendButtonPressed = true }) {
                    Icon(
                        painter = painterResource(R.drawable.user_add_svgrepo_com__1_),
                        tint = DETAILS_COLOR,
                        contentDescription = "Add Friend",
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(friendsList.size) { index ->
                    ProfileTile(
                        profilePicture = friendsList[index].profilePicture.toInt(),
                        username = friendsList[index].name,
                        onClick = { selectedFriend = friendsList[index] }
                    )
                }
            }
        }

        // Overlay ze statystykami znajomego
        selectedFriend?.let { friend ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selectedFriend = null }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(AVATARS[friend.profilePicture.toInt()]),
                            contentDescription = "Profile",
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = friend.name,
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Statystyki",
                            fontSize = 18.sp,
                            color = DETAILS_COLOR,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(16.dp))

                        FriendStatRow(label = "Przesunięte gry", value = friend.swiped.toString())
                        FriendStatRow(label = "Polubione gry", value = friend.liked.toString())
                        FriendStatRow(label = "Odrzucone gry", value = friend.disliked.toString())
                        FriendStatRow(
                            label = "Ulubiony gatunek",
                            value = GENRES.find { it.id == friend.favouriteGenre }?.name ?: "Brak"
                        )

                        Spacer(Modifier.height(20.dp))

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.removeFriend(userRepository, friend.name)
                                    selectedFriend = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
                            ) {
                                Text("Usuń", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { selectedFriend = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DETAILS_COLOR)
                            ) {
                                Text("Zamknij", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (addFriendButtonPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add Friend",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = DETAILS_COLOR
                            )
                            IconButton(
                                onClick = {
                                    addFriendResponseCode = 4
                                    addFriendButtonPressed = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        TextField(
                            value = addFriendNickname,
                            onValueChange = { addFriendNickname = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter username", color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = DETAILS_COLOR,
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.addFriend(userRepository, addFriendNickname) { i ->
                                    addFriendResponseCode = i
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DETAILS_COLOR
                            )
                        ) {
                            Text("Add", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        if (addFriendResponseCode != 4) {
                            Spacer(Modifier.height(12.dp))
                            val (responseText, responseColor) = when (addFriendResponseCode) {
                                0 -> "User not found." to Color(0xFFFF6B6B)
                                1 -> "Friend added successfully!" to Color(0xFF4CAF50)
                                2 -> "Already friends." to Color(0xFFFFA726)
                                3 -> "Cannot add yourself." to Color(0xFFFF6B6B)
                                else -> "" to Color.White
                            }
                            Text(
                                text = responseText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = responseColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 16.sp)
        Text(text = value, color = DETAILS_COLOR, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun ProfileTile(
    profilePicture: Int,
    username: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ){ onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(AVATARS[profilePicture]),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .height(64.dp)
                .width(64.dp)
        )
        Text(
            text = username,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
