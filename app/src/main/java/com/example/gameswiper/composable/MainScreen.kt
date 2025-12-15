package com.example.gameswiper.composable

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
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

    Box {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(bottom = 25.dp)
        ) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(15f)
                ) {
                    when (current) {
                        1 -> SwipingScreen(modifier, context, wrapper, viewModel, gamesRepository)
                        2 -> LibraryScreen(context, viewModel, gamesRepository, wrapper) {}
                        3 -> SocialScreen(viewModel, userRepository)
                        4 -> ProfileScreen(viewModel, userRepository, context)
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
                            val color by animateColorAsState(
                                targetValue = if (current == index) DETAILS_COLOR else Color.Transparent,
                                animationSpec = tween(300),
                                label = "iconColor"
                            )


                            val scale by animateFloatAsState(
                                targetValue = if (current == index) 1.2f else 1.0f,
                                animationSpec = tween(300),
                                label = "iconScale"
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
                                            ambientColor = color,
                                            spotColor = color
                                        )
                                        .background(color, shape = RoundedCornerShape(15.dp))
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
                                            .graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            }
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