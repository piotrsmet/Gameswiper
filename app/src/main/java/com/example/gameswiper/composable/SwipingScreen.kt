package com.example.gameswiper.composable

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.example.gameswiper.R
import com.example.gameswiper.network.GamesWrapper
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.example.gameswiper.model.LibraryViewModel
import com.example.gameswiper.model.SettingsViewModel
import com.example.gameswiper.model.SwipeViewModel
import com.example.gameswiper.model.UserViewModel
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.BUTTON_COLOR
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.DOMINANT_COLOR
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.example.gameswiper.utils.PLATFORMS
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.math.abs


@Composable
fun SwipingScreen(
    modifier: Modifier,
    context: Context,
    wrapper: GamesWrapper,
    swipeViewModel: SwipeViewModel,
    libraryViewModel: LibraryViewModel,
    userViewModel: UserViewModel,
    settingsViewModel: SettingsViewModel,
    gamesRepository: GameRepository,
    isActive: Boolean,
    userRepository: UserRepository
) {
    val offsetX = remember { Animatable(0f) }
    val nextCardScale = remember { Animatable(0.95f) }
    val nextCardOffsetY = remember { Animatable(20f) }
    val coroutineScope = rememberCoroutineScope()
    val maxRotationAngle = 15f

    val gameCards by swipeViewModel.gameCards.collectAsState()
    val currentCard = gameCards.getOrNull(0)
    val nextCard = gameCards.getOrNull(1)
    val currentImage = currentCard?.imageUrl
    val nextImage = nextCard?.imageUrl
    val currentVideo = currentCard?.videoId
    var border by remember { mutableStateOf(5.dp) }
    var fetchingGames by remember { mutableStateOf(true) }

    fun buildImageRequest(url: String?): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .crossfade(300)
            .allowHardware(false)
            .build()
    }
    val imageLoader = remember { ImageLoader(context) }
    val painter = rememberAsyncImagePainter(
        model = buildImageRequest(currentImage),
        imageLoader = imageLoader
    )

    LaunchedEffect(gameCards.size) {
        val preloadCount = 5
        val upcoming = gameCards.map { it.imageUrl }.take(preloadCount)
        upcoming.forEach { imageUrl ->
            imageLoader.enqueue(buildImageRequest(imageUrl))
        }
    }

    LaunchedEffect(Unit) {
        imageLoader.diskCache?.clear()
        imageLoader.memoryCache?.clear()
    }

    val selectedGenres by settingsViewModel.selectedGenres.collectAsState()
    val selectedPlatforms by settingsViewModel.selectedPlatforms.collectAsState()

    LaunchedEffect(gameCards.size){
        if(gameCards.size < 20 && selectedPlatforms.isNotEmpty() && fetchingGames){
            fetchingGames = false
            val preferredIds = userViewModel.getPreferredGameIds()
            Log.i("preferred ids", preferredIds.toString())
            Log.i("fetched again", gameCards.toString())
            if (preferredIds.size > 500) {
                swipeViewModel.fetchGames(
                    context,
                    selectedGenres.toList(),
                    selectedPlatforms.toList(),
                    wrapper,
                    preferredIds
                )
            } else {
                swipeViewModel.fetchGames(
                    context,
                    selectedGenres.toList(),
                    selectedPlatforms.toList(),
                    wrapper
                )
            }

            fetchingGames = true
        }
    }


    val swipeProgress = (offsetX.value / 600f).coerceIn(-1f, 1f)
    val backgroundColor = when {
        swipeProgress > 0 -> Color(0xFF16C47F)
        swipeProgress < 0 -> BUTTON_COLOR
        else -> Color(0xFF4100B1)
    }
    val gradientAlpha = abs(swipeProgress)

    var expanded by remember { mutableStateOf(false) }
    val cardHeight by animateDpAsState(if (expanded) 730.dp else 500.dp)
    val cardWidth by animateDpAsState(if (expanded) 380.dp else 370.dp)
    val scale by animateFloatAsState(if (expanded) 1.05f else 1f)
    var swipeButtonEnabled by remember { mutableStateOf(true) }

    var dislikedAnim by remember { mutableStateOf(false) }
    val dislikeScale by animateFloatAsState(
        targetValue = if (dislikedAnim) 1.15f else 1f,
        animationSpec = tween(150),
        finishedListener = { dislikedAnim = false }, label = ""
    )

    var likedAnim by remember { mutableStateOf(false) }
    val likeScale by animateFloatAsState(
        targetValue = if (likedAnim) 1.15f else 1f,
        animationSpec = tween(150),
        finishedListener = { likedAnim = false }
    )




    SystemBackHandler(enabled =isActive) {
        if(expanded)
            expanded = false
        else
            (context as? Activity)?.finish()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DETAILS_COLOR,
                        Color.Black
                    ),
                    startY = 400f,
                    endY = 2000f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val currentGame = currentCard?.game
        Row(Modifier.align(Alignment.BottomCenter).padding(30.dp)){
            IconButton(onClick = {
                if(swipeButtonEnabled && offsetX.value.equals(0f) && currentGame != null) {
                    swipeButtonEnabled = false
                    dislikedAnim = true
                    coroutineScope.launch {
                        offsetX.animateTo(-1500f, tween(300))
                        launch {
                            nextCardScale.animateTo(1f, tween(300))
                            nextCardOffsetY.animateTo(0f, tween(300))
                        }
                        delay(250)
                        offsetX.snapTo(0f)
                        nextCardScale.snapTo(0.90f)

                        userViewModel.swipedLeft(currentGame.similarGames, userRepository)
                        swipeViewModel.removeCard()
                        swipeButtonEnabled = true
                    }
                }
            },
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer {
                        scaleX = dislikeScale
                        scaleY = dislikeScale
                    }) {
                Icon(painter = painterResource(R.drawable.dislike), tint = BUTTON_COLOR, contentDescription = "dislike")
            }
            Spacer(Modifier.width(40.dp))
            IconButton(onClick = {
                if(swipeButtonEnabled && offsetX.value.equals(0f) && currentGame != null) {
                    swipeButtonEnabled = false
                    likedAnim = true
                    coroutineScope.launch {
                        offsetX.animateTo(1500f, tween(300))

                        libraryViewModel.addGame(context, currentGame, gamesRepository, wrapper)
                        userViewModel.swipedRight(currentGame.similarGames, userRepository, libraryViewModel.getFavouriteGenre())

                        launch {
                            nextCardScale.animateTo(1f, tween(300))
                            nextCardOffsetY.animateTo(0f, tween(300))
                        }

                        delay(250)
                        offsetX.snapTo(0f)
                        nextCardScale.snapTo(0.90f)

                        swipeViewModel.removeCard()
                        swipeButtonEnabled = true
                    }
                }
            },
                modifier = Modifier
                    .size(70.dp)
                    .graphicsLayer {
                        scaleX = likeScale
                        scaleY = likeScale
                    }) {
                Icon(painter = painterResource(R.drawable.like), tint = Color(0xFF16C47F), contentDescription = "like")
            }
        }

        Card(
            modifier = Modifier
                .height(500.dp)
                .width(370.dp)
                .offset(0.dp , (-50).dp)
                .scale(0.90f)
                .graphicsLayer {
                    rotationZ = 10f
                }
                .blur(1.dp, BlurredEdgeTreatment.Unbounded)
                .background(shape = RoundedCornerShape(25.dp), color = Color.Transparent)
                .border(5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(25.dp)),
            colors = CardDefaults.cardColors(Color(0xFF2CD3E1).copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(25.dp)
        ){}
        Card(
            modifier = Modifier
                .height(500.dp)
                .width(370.dp)
                .offset(0.dp , (-50).dp)
                .scale(0.95f)
                .graphicsLayer {
                    rotationZ = -5f
                }
                .blur(0.5.dp, BlurredEdgeTreatment.Unbounded)
                .background(shape = RoundedCornerShape(25.dp), color = Color.Transparent)
                .border(5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(25.dp)),
            colors = CardDefaults.cardColors(Color(0xFF00DFA2).copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(25.dp)
        ){}
        if (nextImage != null) {
            Card(
                modifier = Modifier
                    .height(500.dp)
                    .width(370.dp)
                    .graphicsLayer {
                        scaleX = nextCardScale.value
                        scaleY = nextCardScale.value
                    }
                    .border(5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(25.dp)),
                colors = CardDefaults.cardColors(Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Box(modifier.fillMaxSize()) {
                    AsyncImage(
                        model = nextImage,
                        contentDescription = "Next card",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        imageLoader = imageLoader,
                    )
                    Column {
                        AnimatedVisibility(
                            visible = !expanded,
                            enter = fadeIn(tween(300)),
                            exit = fadeOut(tween(200))
                        ){
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Transparent
                                        ),
                                        startY = 1000f,
                                        endY = 0f
                                    )
                                )
                            ){
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(
                                        text = nextCard.game.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }



        Card(
            modifier = Modifier
                .height(cardHeight)
                .width(cardWidth)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = (offsetX.value / 1000f) * maxRotationAngle
                    scaleX = scale
                    scaleY = scale
                }
                .border(border, Color.White.copy(alpha = 0.1f), RoundedCornerShape(25.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { expanded = !expanded; border = if(!expanded) 5.dp else 0.dp }
                )
                .pointerInput(expanded, swipeButtonEnabled) {
                    if(!expanded && currentGame != null && swipeButtonEnabled){
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                    val progress = (offsetX.value / 300f).coerceIn(-1f, 1f)
                                    nextCardScale.snapTo(0.90f + (0.05f * abs(progress)))

                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    val targetValue = when {
                                        offsetX.value < -100f -> -1500f
                                        offsetX.value > 100f -> 1500f
                                        else -> 0f
                                    }

                                    if (targetValue != 0f) {
                                        swipeButtonEnabled = false
                                        offsetX.animateTo(targetValue, tween(300))
                                        if (offsetX.value > 0f) {
                                            libraryViewModel.addGame(context, currentGame, gamesRepository, wrapper)
                                            userViewModel.swipedRight(currentGame.similarGames, userRepository, libraryViewModel.getFavouriteGenre())
                                        }
                                        else {
                                            userViewModel.swipedLeft(currentGame.similarGames, userRepository)
                                        }
                                        launch {
                                            nextCardScale.animateTo(1f, tween(300))
                                            nextCardOffsetY.animateTo(0f, tween(300))
                                        }
                                        delay(250)
                                        offsetX.snapTo(0f)
                                        nextCardScale.snapTo(0.90f)

                                        swipeViewModel.removeCard()
                                        swipeViewModel.nextImage()
                                        swipeButtonEnabled = true // Odblokuj po zakończeniu
                                    } else {
                                        offsetX.animateTo(0f, tween(300))
                                        nextCardScale.animateTo(0.90f, tween(300))
                                        nextCardOffsetY.animateTo(20f, tween(300))
                                    }
                                }
                            }
                        )
                    }
                },
            colors = CardDefaults.cardColors(Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = RoundedCornerShape(25.dp)
        ) {

            Box() {
                if (painter.state.value is AsyncImagePainter.State.Loading
                    || painter.state.value is AsyncImagePainter.State.Empty) {
                    ShimmerCard(
                        modifier = Modifier.fillMaxSize().background(Color(0xFF2CD3E1))
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    Image(
                        painter = painter,
                        contentDescription = "Game image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        AnimatedVisibility(
                            visible = !expanded,
                            enter = fadeIn(tween(300)),
                            exit = fadeOut(tween(200))
                        ){
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.85f),
                                            Color.Transparent
                                        ),
                                        startY = 1000f,
                                        endY = 0f
                                    )
                                )
                            ){
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(
                                        text = currentGame?.name ?: "",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = gradientAlpha)),
                    contentAlignment = Alignment.Center){
                    if(swipeProgress < 0)
                        Icon(painter = painterResource(R.drawable.dislike), contentDescription = "dislike",
                            tint = Color.Red,
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer {
                                    alpha = (-swipeProgress).coerceIn(0f, 1f)
                                }
                        )
                    else
                    Icon(painter = painterResource(R.drawable.like), contentDescription = "like",
                        tint = Color.Green,
                        modifier = Modifier
                            .size(120.dp)
                            .graphicsLayer {
                                alpha = swipeProgress.coerceIn(0f, 1f)
                            }
                    )
                }

                if(currentGame != null && swipeButtonEnabled) {
                    Column {
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn(tween(300)),
                            exit = fadeOut(tween(200))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.85f),
                                                Color.Transparent
                                            ),
                                            startY = 1000f,
                                            endY = 0f
                                        )
                                    )
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
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
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Platforms: ${
                                        currentGame.platforms.map { p -> PLATFORMS.find { pl -> pl.id == p }?.name ?: "" }
                                            .filter { p -> p != "" }.joinToString()
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (currentGame.summary.length > 120)
                                        currentGame.summary.take(120) + "…"
                                    else
                                        currentGame.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                if(currentVideo == "ihyrf2jfpIk"){
                                    Text(
                                        text = "No trailer available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    YouTubePlayerScreen(
                                        currentVideo,
                                        Modifier.clip(RoundedCornerShape(15.dp))
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



@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24
) {
    val shimmerColors = listOf(
        Color.DarkGray.copy(alpha = 0.4f),
        Color.DarkGray.copy(alpha = 0.15f),
        Color.DarkGray.copy(alpha = 0.4f)
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ), label = ""
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 500f, 500f)
    )

    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(cornerRadius.dp))
            .background(brush)
    )
}

@Composable
fun SystemBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentOnBack by rememberUpdatedState(onBack)

    DisposableEffect(context, enabled) {
        val activity = context as? ComponentActivity
            ?: return@DisposableEffect onDispose {}
        val callback = object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                currentOnBack()
            }
        }
        activity.onBackPressedDispatcher.addCallback(callback)
        var invokedCallback: OnBackInvokedCallback? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (enabled) {
                invokedCallback = OnBackInvokedCallback { currentOnBack() }
                activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    invokedCallback
                )
            }
        }

        onDispose {
            callback.remove()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                invokedCallback?.let {
                    activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it)
                }
            }
        }
    }
}


@Composable
fun YouTubePlayerScreen(
    videoId: String?,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val id = videoId ?: return

    key(id) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                YouTubePlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    lifecycleOwner.lifecycle.addObserver(this)

                    addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            youTubePlayer.cueVideo(id, 0f)
                        }

                        override fun onError(
                            youTubePlayer: YouTubePlayer,
                            error: PlayerConstants.PlayerError
                        ) {
                            Log.e("YouTubePlayer", "Błąd odtwarzania: $error")
                        }
                    })
                }
            }
        )
    }
}



@Composable
fun NotificationPermissionHandler() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var permissionGranted by remember { mutableStateOf(checkNotificationPermission(context)) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }
    LaunchedEffect(Unit) {
        if (!permissionGranted) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true
}