package com.example.gameswiper.composable

import android.content.Context
import android.graphics.drawable.Icon
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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

    LaunchedEffect(Unit) {
        viewModel.fetchImages2(context, gamesWrapper, gameRepository)
    }
    val imagesList = viewModel.images2.collectAsState()

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
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 128.dp),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (imagesList.value.isEmpty()) {
                    item {
                        Text(
                            text = "Loading...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            color = Color.White
                        )
                    }
                } else {
                    items(imagesList.value.size) { index ->
                        ImageCard(imagesList.value[index])
                    }
                }
            }
        }
    )

}

@Composable
fun ImageCard(imageUrl: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .height(100.dp),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

    }
}