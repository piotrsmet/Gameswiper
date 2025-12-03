package com.example.gameswiper.composable

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository

private enum class FullScreenImageState {
    HIDDEN,
    VISIBLE_FRONT,
    VISIBLE_BACK
}

@Composable
fun LibraryScreen(context: Context, viewModel: GamesViewModel, gameRepository: GameRepository, gamesWrapper: GamesWrapper, onBackPressed: () -> Unit) {

    LaunchedEffect(Unit) {
        viewModel.fetchImages2(context, gamesWrapper, gameRepository)
    }
    val imagesList = viewModel.images2.collectAsState()
    val coverIdList = viewModel.coverId.collectAsState()
    var columns by remember { mutableStateOf(3) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var isFlipped by remember { mutableStateOf(false) }
    var aspectRatio by remember(selectedImageUrl) { mutableStateOf<Float?>(null) }

    LaunchedEffect(selectedImageUrl) {
        if (selectedImageUrl == null) {
            isFlipped = false
        }
    }

    BackHandler {
        if (isFlipped) {
            isFlipped = false
        } else if (selectedImageUrl != null) {
            selectedImageUrl = null
        } else {
            onBackPressed()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(top = 50.dp)) {
            Row(Modifier.weight(1f).fillMaxWidth()){

                Button(
                    modifier = Modifier.weight(1f).border(2.dp, Color.White),
                    onClick = { columns = 1},
                    shape = CircleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)
                ) {
                    Text("1", color = Color.White)
                }
                Button(
                    modifier = Modifier.weight(1f).border(2.dp, Color.White),
                    onClick = { columns = 2},
                    shape = CircleShape,
                    colors = ButtonColors(Color.Red, Color.Black, Color.Black, Color.Black)
                ) {
                    Text("2", color = Color.White)
                }
                Button(
                    modifier = Modifier.weight(1f).border(2.dp, Color.White),
                    onClick = { columns = 3},
                    shape = CircleShape,
                    colors = ButtonColors(Color.Black, Color.Black, Color.Black, Color.Black)
                ) {
                    Text("3", color = Color.White)
                }
            }
            Row(Modifier.weight(12f)) {
                Box() {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier
                            .background(Color.Black)
                            .fillMaxSize()
                    ) {
                        if (imagesList.value.isEmpty() || coverIdList.value.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Black)
                                )
                            }
                        } else {
                            items(imagesList.value.size) { index ->
                                ImageCard(
                                    imageUrl = imagesList.value[index],
                                    columns = columns,
                                    gameId = coverIdList.value[index],
                                    onDelete = {
                                        gameRepository.deleteGame(
                                            coverIdList.value[index]
                                        ) {
                                            viewModel.fetchImages2(
                                                context,
                                                gamesWrapper,
                                                gameRepository
                                            )
                                        }
                                    },
                                    removeCover = { viewModel.removeImage2(imagesList.value[index]) },
                                    onClick = { selectedImageUrl = imagesList.value[index] },
                                    modifier = Modifier.animateItem(),
                                    title = "Lorem ipsum dolor sit amet"
                                )

                            }
                        }
                    }
                }
            }
        }

        val transitionState = remember(selectedImageUrl, isFlipped) {
            when {
                selectedImageUrl == null -> FullScreenImageState.HIDDEN
                !isFlipped -> FullScreenImageState.VISIBLE_FRONT
                else -> FullScreenImageState.VISIBLE_BACK
            }
        }

        val transition = updateTransition(
            targetState = transitionState,
            label = "FullScreenImageTransition"
        )

        val scale by transition.animateFloat(
            label = "scale",
            transitionSpec = {
                if (targetState == FullScreenImageState.HIDDEN) {
                    tween(durationMillis = 0)
                } else {
                    tween(durationMillis = 300)
                }
            }
        ) { state ->
            if (state == FullScreenImageState.HIDDEN) 0f else 1f
        }

        val rotation by transition.animateFloat(
            label = "rotation",
            transitionSpec = {
                if (initialState == FullScreenImageState.VISIBLE_FRONT && targetState == FullScreenImageState.VISIBLE_BACK ||
                    initialState == FullScreenImageState.VISIBLE_BACK && targetState == FullScreenImageState.VISIBLE_FRONT) {
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                } else {
                    tween(durationMillis = 0)
                }
            }
        ) { state ->
            if (state == FullScreenImageState.VISIBLE_BACK) 180f else 0f
        }

        if (transition.currentState != FullScreenImageState.HIDDEN || transition.targetState != FullScreenImageState.HIDDEN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selectedImageUrl = null }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current.density
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationY = rotation
                            cameraDistance = 8 * density
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isFlipped = !isFlipped }
                        )
                ) {
                    if (rotation <= 90f) {
                        AsyncImage(
                            model = selectedImageUrl,
                            contentDescription = "Full screen image",
                            onState = { state ->
                                if (state is AsyncImagePainter.State.Success) {
                                    val size = state.painter.intrinsicSize
                                    if (size.height > 0f) {
                                        aspectRatio = size.width / size.height
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspectRatio ?: (3f / 4f))
                                .padding(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.DarkGray)
                                .graphicsLayer {
                                    rotationY = 180f
                                }
                        )
                    }
                }
            }
        }
    }
}












@Composable
fun ImageCard(imageUrl: String,
              columns: Int,
              gameId: Int,
              onDelete: (Int) -> Unit,
              removeCover: (String) -> Unit,
              onClick: () -> Unit,
              modifier: Modifier = Modifier,
              title: String) {
    val height = when (columns) {
        1 -> 500.dp
        2 -> 250.dp
        else -> 165.dp
    }

    val titleFontSize = when(columns){
        1 -> 20
        2 -> 16
        else -> 14
    }
    Column(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = title,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = with(LocalDensity.current) { titleFontSize.dp.toSp() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 4.dp, end = 4.dp)
        )
    }
}

