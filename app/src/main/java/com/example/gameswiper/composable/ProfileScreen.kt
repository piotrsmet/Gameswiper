package com.example.gameswiper.composable

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.AVATARS
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    viewModel: GamesViewModel,
    userRepository: UserRepository,
    context: Context
) {
    val user by viewModel.userDisplay.collectAsState()

    val avatar = AVATARS[user.profilePicture.toInt() % AVATARS.size]

    val stats = listOf(
        "Przesunięto" to user.swiped,
        "Polubiono" to user.liked,
        "Nie polubiono" to user.disliked
    )

    Scaffold(
        containerColor = Color(0xFF0B0B0B)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                modifier = Modifier.padding(4.dp),
                onClick = { viewModel.clearDataStore(context); FirebaseAuth.getInstance().signOut()},
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

            Surface(
                shape = CircleShape,
                modifier = Modifier.size(120.dp),
                color = Color(0xFF13111F),
                contentColor = Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(avatar),
                        contentDescription = "Profile Picture",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = user.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))


            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(stats) { (label, value) ->
                    StatCard(label = label, value = value.toString())
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF13111F),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
