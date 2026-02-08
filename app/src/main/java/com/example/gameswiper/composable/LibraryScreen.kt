package com.example.gameswiper.composable

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Precision
import com.example.gameswiper.R
import com.example.gameswiper.model.Game
import com.example.gameswiper.model.LibraryViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.utils.BUTTON_COLOR
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS

private enum class FullScreenImageState {
    HIDDEN,
    VISIBLE_FRONT,
    VISIBLE_BACK
}

private enum class SortOrder {
    NONE, A_TO_Z, Z_TO_A, NEW_TO_OLD, OLD_TO_NEW
}


@Composable
fun LibraryScreen(context: Context,
                  libraryViewModel: LibraryViewModel,
                  gameRepository: GameRepository,
                  gamesWrapper: GamesWrapper,
                  isActive: Boolean
) {

    val savedGamesWithMedia by libraryViewModel.savedGamesWithMedia.collectAsState()

    var columns by remember { mutableStateOf(3) }

    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var selectedGameId by remember { mutableStateOf<Int?>(null) }

    var isFlipped by remember { mutableStateOf(false) }
    var isDetailsExpanded by remember { mutableStateOf(false) }
    var aspectRatio by remember(selectedImageUrl) { mutableStateOf<Float?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showLikedOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf<SortOrder>(SortOrder.NONE) }
    var showFilterMenu by remember { mutableStateOf(false) }

    var likeAnimationScale by remember { mutableStateOf(1f) }
    val focusManager = LocalFocusManager.current

    val filteredGames = remember(searchQuery, savedGamesWithMedia, showLikedOnly, sortOrder) {
        savedGamesWithMedia
            .filter { item ->
                val game = item.game
                val matchesSearch = if (searchQuery.isBlank()) true else game.name.contains(searchQuery, ignoreCase = true)
                val matchesLike = if (showLikedOnly) game.liked else true
                matchesSearch && matchesLike
            }
            .let { list ->
                when (sortOrder) {
                    SortOrder.A_TO_Z -> list.sortedBy { it.game.name.lowercase() }
                    SortOrder.Z_TO_A -> list.sortedByDescending { it.game.name.lowercase() }
                    SortOrder.NEW_TO_OLD -> list.sortedByDescending { it.game.dateOfAddition }
                    SortOrder.OLD_TO_NEW -> list.sortedBy { it.game.dateOfAddition }
                    SortOrder.NONE -> list
                }
            }
    }


    fun buildImageRequest(url: String?): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .crossfade(500)
            .allowHardware(false)
            .build()
    }

    val imageLoader = remember { ImageLoader(context) }


    LaunchedEffect(selectedImageUrl) {
        if (selectedImageUrl == null) {
            isFlipped = false
            isDetailsExpanded = false
            selectedGameId = null
            selectedVideoId = null
        }
    }

    BackHandler {
        if (isDetailsExpanded) {
            isDetailsExpanded = false
        } else if (isFlipped) {
            isFlipped = false
        } else if (selectedImageUrl != null) {
            selectedImageUrl = null
            selectedGameId = null
            selectedVideoId = null
        }
        else
            (context as? Activity)?.finish()
    }

    Scaffold(
        containerColor = Color.Black,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Wyszukaj grę...", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.White
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedContainerColor = Color.DarkGray,
                            unfocusedContainerColor = Color.DarkGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = TextStyle(fontSize = 16.sp),
                        singleLine = true
                    )

                    Box {
                        IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Filters",
                                tint = if (showFilterMenu || showLikedOnly || sortOrder != SortOrder.NONE) DETAILS_COLOR else Color.Gray
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(Color(0xFF2A2A2A)).width(200.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = when (sortOrder) {
                                                SortOrder.NONE -> "Bez sortowania"
                                                SortOrder.A_TO_Z -> "Sortuj A-Z"
                                                SortOrder.Z_TO_A -> "Sortuj Z-A"
                                                SortOrder.NEW_TO_OLD -> "Od najnowszych"
                                                SortOrder.OLD_TO_NEW -> "Od najstarszych"
                                            },
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    sortOrder = when (sortOrder) {
                                        SortOrder.NONE -> SortOrder.A_TO_Z
                                        SortOrder.A_TO_Z -> SortOrder.Z_TO_A
                                        SortOrder.Z_TO_A -> SortOrder.NEW_TO_OLD
                                        SortOrder.NEW_TO_OLD -> SortOrder.OLD_TO_NEW
                                        SortOrder.OLD_TO_NEW -> SortOrder.NONE
                                    }
                                },
                                leadingIcon = {
                                    Text(
                                        text = when (sortOrder) {
                                            SortOrder.NONE -> " "
                                            SortOrder.A_TO_Z -> "A↓"
                                            SortOrder.Z_TO_A -> "Z↓"
                                            SortOrder.NEW_TO_OLD -> "N↓"
                                            SortOrder.OLD_TO_NEW -> "O↓"
                                        },
                                        color = if (sortOrder != SortOrder.NONE) DETAILS_COLOR else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Tylko ulubione", color = Color.White) },
                                onClick = { showLikedOnly = !showLikedOnly },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Filter Liked",
                                        tint = if (showLikedOnly) Color.Yellow else Color.Gray
                                    )
                                }
                            )

                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                            DropdownMenuItem(
                                text = { Text("1 kolumna", color = Color.White) },
                                onClick = { columns = 1 },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.column_one_svgrepo_com),
                                        contentDescription = "1 Column",
                                        tint = if (columns == 1) DETAILS_COLOR else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("2 kolumny", color = Color.White) },
                                onClick = { columns = 2 },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.columns_02_svgrepo_com),
                                        contentDescription = "2 Columns",
                                        tint = if (columns == 2) DETAILS_COLOR else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("3 kolumny", color = Color.White) },
                                onClick = { columns = 3 },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(R.drawable.columns_03_svgrepo_com),
                                        contentDescription = "3 Columns",
                                        tint = if (columns == 3) DETAILS_COLOR else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )

                        }
                    }
                }


                Row(Modifier.weight(12f)) {
                    Box(Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { focusManager.clearFocus() }
                        )) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier
                                .background(Color.Black)
                                .fillMaxSize()
                        ) {
                            items(filteredGames.size) { index ->
                                val item = filteredGames[index]
                                ImageCard(
                                    imageUrl = item.cover,
                                    columns = columns,
                                    imageLoader = imageLoader,
                                    onClick = {
                                        focusManager.clearFocus()
                                        selectedImageUrl = item.cover
                                        selectedGameId = item.game.id
                                        selectedVideoId = item.video
                                    },
                                    modifier = Modifier.animateItem(),
                                    title = item.game.name
                                )
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
                        tween(durationMillis = 300)
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
                        initialState == FullScreenImageState.VISIBLE_BACK && targetState == FullScreenImageState.VISIBLE_FRONT
                    ) {
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    } else {
                        tween(durationMillis = 100)
                    }
                }
            ) { state ->
                if (state == FullScreenImageState.VISIBLE_BACK) 180f else 0f
            }

            if (transition.currentState != FullScreenImageState.HIDDEN || transition.targetState != FullScreenImageState.HIDDEN) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f * scale))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (!isDetailsExpanded) selectedImageUrl = null
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    if (!isDetailsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {}
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            if (scale > 0.5f) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = {
                                            selectedGameId?.let { gameId ->
                                                libraryViewModel.deleteGameById(gameId)
                                                gameRepository.deleteGame(gameId) {}
                                                selectedImageUrl = null
                                                selectedGameId = null
                                                selectedVideoId = null
                                            }
                                        },
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = BUTTON_COLOR,
                                            modifier = Modifier
                                                .size(40.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            selectedGameId?.let { gameId ->
                                                libraryViewModel.toggleLikeById(gameId)
                                                gameRepository.likeGame(gameId)
                                                likeAnimationScale = 1.3f
                                            }
                                        },
                                        modifier = Modifier
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
                                    ) {
                                        val isLiked = selectedGameId?.let { gameId ->
                                            libraryViewModel.getGameById(gameId)?.liked ?: false
                                        } ?: false

                                        val animatedScale by animateFloatAsState(
                                            targetValue = likeAnimationScale,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessHigh
                                            ),
                                            finishedListener = {
                                                likeAnimationScale = 1f
                                            }
                                        )

                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Like",
                                            tint = if (isLiked) Color.Yellow else Color.White,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .graphicsLayer {
                                                    scaleX = animatedScale
                                                    scaleY = animatedScale
                                                }
                                        )
                                    }
                                }
                            }
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
                                        model = buildImageRequest(selectedImageUrl),
                                        imageLoader = imageLoader,
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
                                            .clip(RoundedCornerShape(20.dp))
                                            .border(5.dp, Color.White.copy(alpha=0.2f), RoundedCornerShape(20.dp))
                                    )
                                } else {
                                    val currentGame = selectedGameId?.let { libraryViewModel.getGameById(it) }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(aspectRatio ?: (3f / 4f))
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF1A1A1A))
                                            .graphicsLayer {
                                                rotationY = 180f
                                            }
                                            .padding(20.dp)
                                    ) {
                                        if (currentGame != null) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = currentGame.name,
                                                        style = MaterialTheme.typography.headlineSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = "Genres: ${
                                                            currentGame.genres.map { g -> GENRES.find { gn -> gn.id == g }?.name ?: "" }
                                                                .joinToString()
                                                        }",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.LightGray
                                                    )
                                                    Spacer(Modifier.height(4.dp))
                                                    Text(
                                                        text = "Platforms: ${
                                                            currentGame.platforms.map { p -> PLATFORMS.find { pl -> pl.id == p }?.name ?: "" }
                                                                .filter { p -> p != "" }.joinToString()
                                                        }",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.LightGray
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = currentGame.summary,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    if (selectedVideoId != null) {
                                                        if(selectedVideoId == "ihyrf2jfpIk"){
                                                            Text(
                                                                text = "No trailer available",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        else {
                                                            YouTubePlayerScreen(
                                                                videoId = selectedVideoId!!,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(200.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCard(imageUrl: String,
              columns: Int,
              imageLoader: ImageLoader,
              onClick: () -> Unit,
              modifier: Modifier = Modifier,
              title: String) {
    val height = when (columns) {
        1 -> 500.dp
        2 -> 250.dp
        else -> 165.dp
    }

    val titleFontSize = when(columns){
        1 -> 32
        2 -> 16
        else -> 14
    }

    val round = when(columns){
        1 -> 20.dp
        2 -> 12.dp
        else -> 8.dp
    }
    val requestSize = when (columns) {
        1 -> 800
        2 -> 400
        else -> 250
    }

    val context = LocalContext.current
    val imageRequest = remember(imageUrl, columns) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .crossfade(300)
            .size(requestSize)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .build()
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
            shape = RoundedCornerShape(round),
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            AsyncImage(
                model = imageRequest,
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