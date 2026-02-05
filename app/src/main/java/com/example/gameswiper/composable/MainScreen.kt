package com.example.gameswiper.composable

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.DETAILS_COLOR
import com.example.gameswiper.utils.isNetworkAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Screens{
    swipingScreen,
    libraryScreen,
    profileScreen,
    settingsScreen
}

@Composable
fun MainScreen(modifier: Modifier, context: Context, viewModel: GamesViewModel, logOut: () -> Unit){

    var current by remember { mutableIntStateOf(1) }

    val wrapper = remember { GamesWrapper() }
    val gamesRepository = remember { GameRepository() }
    val userRepository = remember { UserRepository() }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    // Obserwowanie stanu sieci na żywo
    val isConnected by produceState(initialValue = context.isNetworkAvailable()) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { value = true }
            override fun onLost(network: Network) { value = false }
        }

        try {
            connectivityManager.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            value = context.isNetworkAvailable()
        }

        awaitDispose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            val needsLoading = viewModel.games.value.isEmpty()
            withContext(Dispatchers.IO) {
                try {
                    wrapper.getStaticToken()

                    if (needsLoading) {
                        viewModel.fetchSettings(userRepository, context, wrapper)
                        viewModel.fetchUserDisplay(userRepository)
                    }
                    viewModel.fetchImages2(context, wrapper, gamesRepository)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(context, "Brak połączenia z internetem", Toast.LENGTH_SHORT).show()
        }
    }

    // Główny kontener
    Box(modifier = modifier.fillMaxSize()) {

        // WARSTWA 1: Cały interfejs aplikacji
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(bottom = 25.dp)
        ) {
            Column {
                // Usunąłem stąd AnimatedVisibility, żeby nie przesuwało ekranu

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)
                ) {
                    val screens = listOf(1,2,3,4)
                    screens.forEach { index ->
                        val targetOffset by animateDpAsState(
                            targetValue = screenWidth * (index - current),
                            animationSpec = spring(2f),
                            label = "screenOffset"
                        )

                        Box(
                            modifier = Modifier
                                .width(screenWidth)
                                .fillMaxHeight()
                                .offset(x = targetOffset)
                        ) {
                            when (index) {
                                1 -> SwipingScreen(Modifier, context, wrapper, viewModel, gamesRepository, isActive = (current == 1), userRepository)
                                2 -> LibraryScreen(context, viewModel, gamesRepository, wrapper, isActive = (current == 2))
                                3 -> SocialScreen(viewModel, userRepository, isActive = (current == 3))
                                4 -> ProfileScreen(viewModel, userRepository, context, isActive = (current == 4))
                            }
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(2f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top=5.dp)) {
                        val buttons = listOf(
                            Triple(1, R.drawable.browsers_svgrepo_com, "Swiping"),
                            Triple(2, R.drawable.folder_svgrepo_com, "Library"),
                            Triple(3, R.drawable.community_group_leader_svgrepo_com, "Settings"),
                            Triple(4, R.drawable.user_svgrepo_com, "Profile")
                        )

                        buttons.forEach { (index, icon, description) ->
                            val color by animateDpAsState(
                                targetValue = if (current == index) 1.dp else 0.dp,
                                animationSpec = tween(300),
                                label = "dummyColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(6.dp)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .shadow(
                                            elevation = 20.dp,
                                            shape = RoundedCornerShape(15.dp),
                                            ambientColor = if (current == index) DETAILS_COLOR else Color.Transparent,
                                            spotColor = if (current == index) DETAILS_COLOR else Color.Transparent
                                        )
                                        .background(if (current == index) DETAILS_COLOR else Color.Transparent, shape = RoundedCornerShape(15.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { current = index }
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(icon),
                                        contentDescription = description,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // WARSTWA 2: Pływający komunikat o braku internetu (Overlay)
        // Umieszczony w głównym Boxie, więc będzie NA WIERZCHU interfejsu
        AnimatedVisibility(
            visible = !isConnected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
            // Pozycjonujemy go na górze, z odstępem
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp) // Odstęp od samej "sufitu" ekranu
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFB00020).copy(alpha = 0.9f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Brak internetu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tryb offline. Sprawdź połączenie.",
                        color = Color.White,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
