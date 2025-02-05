package com.example.gameswiper.composable

import android.content.Context
import android.graphics.drawable.Icon
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.gameswiper.R
import com.example.gameswiper.model.Game
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository

@Composable
fun LibraryScreen(context: Context, viewModel: GamesViewModel, gameRepository: GameRepository, gamesWrapper: GamesWrapper, onBackPressed: () -> Unit){
    var change by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        viewModel.fetchImages2(context, gamesWrapper, gameRepository)
    }
    val imagesList = viewModel.images2.collectAsState()
    val coverIdList = viewModel.coverId.collectAsState()
    BackHandler {
        onBackPressed()
    }

    Scaffold(
        content = {padding ->
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.fillMaxSize()
            )
            if(imagesList.value.isEmpty() || coverIdList.value.isEmpty()){
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Row(modifier = Modifier.fillMaxHeight().align(Alignment.CenterHorizontally)) {
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color(0xFFFFF2AF)
                            )
                        }
                    }
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (imagesList.value.isEmpty() || coverIdList.value.isEmpty()) {
                    item {
                        Color(0xFFFFF2AF)
                    }
                }
                else if(imagesList.value.size == 0){
                    item {
                        Text(
                            text = "No games in library",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.White
                        )
                    }
                }
                else {
                    items(imagesList.value.size) { index ->
                        ImageCard(
                            imagesList.value[index], coverIdList.value.get(index),
                            {gameRepository.deleteGame(coverIdList.value.get(index),
                                { viewModel.fetchImages2(context, gamesWrapper, gameRepository)})},
                            {viewModel.removeImage(imagesList.value[index])}
                            )
                    }
                }
            }
        }
    )

}

@Composable
fun ImageCard(imageUrl: String,
              gameId: Int,
              onDelete: (Int) -> Unit,
              removeCover: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Gray)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(bottom = 35.dp)
            )

            IconButton(
                onClick = {onDelete(gameId); removeCover(imageUrl)},
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Delete",
                    tint = Color.Black
                )
            }
        }
    }
}