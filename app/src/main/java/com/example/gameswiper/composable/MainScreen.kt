package com.example.gameswiper.composable

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.gameswiper.R
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.DETAILS_COLOR

enum class Screens{
    swipingScreen,
    libraryScreen,
    profileScreen,
    settingsScreen
}

@Composable
fun MainScreen(modifier: Modifier, context: Context, viewModel: GamesViewModel, logOut: () -> Unit){

    var current by remember { mutableIntStateOf(1) }
    val wrapper = GamesWrapper()
    wrapper.getStaticToken()
    val gamesRepository = GameRepository()
    val userRepository = UserRepository()

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val buttonWidth = screenWidth / 4
    val indicatorOffset by animateDpAsState(
        targetValue = buttonWidth * (current - 1),
        animationSpec = tween(300),
        label = "indicatorOffset"
    )

    LaunchedEffect (Unit){
        viewModel.fetchSettings(userRepository, context, wrapper)
        viewModel.fetchUserDisplay(userRepository)
    }

    Box(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)){
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(15f)) {
                when (current) {
                    1 -> SwipingScreen(modifier, context, wrapper, viewModel, gamesRepository)
                    2 -> SettingsScreen(onBackPressed = { current = 1 }, context, logOut, wrapper)
                    3 -> LibraryScreen(context, viewModel, gamesRepository, wrapper){}
                    4 -> ProfileScreen(viewModel, userRepository, context)
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                Box(
                    modifier = Modifier
                        .width(buttonWidth)
                        .offset(x = indicatorOffset)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = DETAILS_COLOR,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = strokeWidth
                            )
                        }
                )
                Row {
                    val buttons = listOf(
                        Triple(1, R.drawable.pages_svgrepo_com, "Swiping"),
                        Triple(2, R.drawable.delete, "Settings"),
                        Triple(3, R.drawable.server_svgrepo_com, "Library"),
                        Triple(4, R.drawable.user_male_svgrepo_com, "Profile")
                    )

                    buttons.forEach { (index, icon, description) ->
                        val color by animateColorAsState(
                            targetValue = if (current == index) DETAILS_COLOR else Color.White,
                            animationSpec = tween(300),
                            label = "iconColor"
                        )
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { current = index },
                            shape = RectangleShape,
                            colors = ButtonColors(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent)
                        ) {
                            Icon(painter = painterResource(icon), contentDescription = description, tint = color, modifier = Modifier.size(25.dp))
                        }
                    }
                }
            }
        }
    }
}