package com.example.gameswiper.composable

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.Greeting
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.AVATARS
import com.example.gameswiper.utils.DETAILS_COLOR

// kotlin
@Composable
fun SocialScreen(
    viewModel: GamesViewModel,
    userRepository: UserRepository,
) {
    var addFriendNickname by remember { mutableStateOf("") }
    var addFriendButtonPressed by remember { mutableStateOf(false) }
    val friendsList by viewModel.friends.collectAsState()

    BackHandler {
        addFriendButtonPressed = false
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
                    ProfileTile(friendsList[index].profilePicture.toInt(), friendsList[index].name)
                }
            }
        }


        if (addFriendButtonPressed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = "Add Friend",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TextField(
                        value = addFriendNickname,
                        onValueChange = { addFriendNickname = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter username", color = Color.Gray) },
                        singleLine = true,
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.addFriend(userRepository, addFriendNickname) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}


@Composable
fun ProfileTile(
    profilePicture: Int,
    username: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp),
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