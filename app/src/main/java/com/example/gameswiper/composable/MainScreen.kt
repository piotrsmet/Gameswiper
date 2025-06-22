package com.example.gameswiper.composable

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.gameswiper.R
import com.example.gameswiper.network.GamesWrapper
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.request.ImageRequest
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.SettingsRepository
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS


@Composable
fun MainScreen(modifier: Modifier, context: Context, wrapper: GamesWrapper, viewModel: GamesViewModel, gamesRepository: GameRepository){
    val offsetX = remember { Animatable(0f) }

    val settingsRepository = SettingsRepository()
    val coroutineScope = rememberCoroutineScope()
    val maxRotationAngle = 15f
    var imageUrl by remember { mutableStateOf(" ") }
    val themesFile = File(context.filesDir, "themes.json")
    var borderColor by remember { mutableStateOf(Color(0xFF4100B1)) }
    var borderColor2 by remember { mutableStateOf(Color(0xFF4100B1)) }


    val images by viewModel.images.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val games by viewModel.games.collectAsState()
    val genres by viewModel.selectedGenres.collectAsState()
    val genresList = GENRES
    val platformsList = PLATFORMS
    val platforms by viewModel.selectedPlatforms.collectAsState()
    var currentImage = images.getOrNull(currentIndex)




    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.fillMaxSize().weight(90f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardColors(
                        Color(0xFF4635B1), Color(0xFFBBBBBB),
                        Color(0xff9933ff), Color(0xFF816BA8)
                    ),
                    modifier = Modifier
                        .height(600.dp)
                        .width(350.dp)
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .graphicsLayer {
                            rotationZ = (offsetX.value / 1000f) * maxRotationAngle
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                        viewModel.update0ffset(offsetX.value + dragAmount.x)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (offsetX.value < -100f) {
                                            offsetX.animateTo(
                                                targetValue = -1500f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            delay(200)
                                            offsetX.snapTo(0f)
                                            print(viewModel.images.value.size)
                                            viewModel.nextImage()

                                        } else if (offsetX.value > 100f) {
                                            offsetX.animateTo(
                                                targetValue = 1500f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            delay(200)
                                            offsetX.snapTo(0f)
                                            println(games[currentIndex])
                                            gamesRepository.addGame(games[currentIndex])
                                            viewModel.nextImage()

                                        } else {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                        }
                                        viewModel.update0ffset(0f)
                                    }
                                }
                            )
                        }

                ) {

                    Box(
                        modifier = Modifier
                            .size(350.dp)

                    )
                    {
                        if (currentImage != null) {
                            println(currentImage)

                            val imageLoader = ImageLoader(context)

                            LaunchedEffect(currentIndex) {
                                val nextImages = images.drop(currentIndex + 1)
                                    .take(3)
                                nextImages.forEach { imageUrl ->
                                    val request = ImageRequest.Builder(context)
                                        .data(imageUrl)
                                        .build()
                                    imageLoader.enqueue(request)
                                }
                            }

                            AsyncImage(
                                model = currentImage,
                                contentDescription = "Example Image",
                                modifier = Modifier
                                    .height(340.dp)
                                    .width(650.dp)
                                    .padding(start = 60.dp, top = 15.dp, end = 60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(10.dp, borderColor2, RoundedCornerShape(8.dp))
                                    .background(Color.White)

                            )


                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color(0xFFFFF2AF)
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    if (currentImage != null) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(5.dp)
                        ) {
                            Box()
                            {
                                Text(
                                    text = games[currentIndex].name,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(borderColor).padding(5.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(5.dp)
                        ) {

                            Box(modifier = Modifier.padding(start = 40.dp).width(100.dp))
                            {
                                var genresToString = ""
                                games[currentIndex].genres.forEach{ genreId ->
                                    genresList.find { it.id == genreId }?.let { genre ->
                                        genresToString+="${genre.name} "
                                    }
                                }

                                Text(
                                    text = genresToString,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(borderColor).padding(5.dp),
                                    maxLines = 2
                                )
                            }

                            Box(modifier = Modifier.padding(end = 40.dp).width(100.dp))
                            {
                                var platformsToString = ""
                                games[currentIndex].platforms.forEach{ platformId ->
                                    platformsList.find { it.id == platformId }?.let { platform ->
                                        platformsToString+="${platform.name} "
                                    }
                                }
                                Text(
                                    text = platformsToString,
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clip(RoundedCornerShape(15.dp))
                                        .background(borderColor).padding(5.dp),
                                    maxLines = 2,
                                )
                            }

                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(5.dp)
                        ) {
                            Box()
                            {
                                Text(
                                    text = games[currentIndex].summary,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                                        .background(borderColor).padding(5.dp),
                                    maxLines = 4,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                    }
                    if(offsetX.value>100){
                        borderColor2 = Color(0xFF5D8736)
                    }
                    else if(offsetX.value<-100){
                        borderColor2 = Color(0xFFA31D1D)
                    }
                    else borderColor2 = Color(0xFF4100B1)

                }
            }
        }
    }

}

@Composable
fun ImageBackground(modifier: Modifier, context: Context, logOut: () -> Unit) {
    val viewModel: GamesViewModel = viewModel()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize()
        )

        var current by remember { mutableIntStateOf(1) }
        val wrapper = GamesWrapper()
        wrapper.getStaticToken()
        val gamesRepository = GameRepository()
        var currentScreen by remember { mutableStateOf("home_screen") }
        val settingsRepository = SettingsRepository()

        LaunchedEffect (Unit){viewModel.fetchSettings(settingsRepository, context, wrapper)}

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            NotificationPermissionHandler()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { current = -1; currentScreen = "settings_screen"},
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4100B1))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(30.dp), tint = Color.White)
                }
                Button(onClick = { current = 1; currentScreen = "library_screen"},
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4100B1))
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Library", modifier = Modifier.size(30.dp), tint = Color.White)
                }
            }
        }
        AnimatedVisibility(
            visible = currentScreen == "home_screen",
            enter = slideInHorizontally(
                initialOffsetX = { -it * current },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it * current },
                animationSpec = tween(500)
            )
        ) {
            MainScreen(modifier, context, wrapper, viewModel, gamesRepository)
        }

        AnimatedVisibility(
            visible = currentScreen == "settings_screen",
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            )
        ) {
            SettingsScreen(onBackPressed = { currentScreen = "home_screen"}, context, logOut, wrapper)
        }


        AnimatedVisibility(
            visible = currentScreen == "library_screen",
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(500)
            )
        ) {
            LibraryScreen(
                context = context,
                viewModel = viewModel,
                gameRepository = gamesRepository,
                gamesWrapper = wrapper,
                onBackPressed = { currentScreen = "home_screen" }
            )
        }

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