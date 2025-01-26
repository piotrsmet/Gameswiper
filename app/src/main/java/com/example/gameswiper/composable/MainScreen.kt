package com.example.gameswiper.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
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
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.gameswiper.R
import com.example.gameswiper.network.GamesWrapper



@Composable
fun MainScreen(modifier: Modifier){
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val maxRotationAngle = 15f
    val wrapper = GamesWrapper()
    val imageUrl = wrapper.getImage()
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        Row(
            modifier = Modifier.padding(horizontal = 10.dp).weight(10f)){
            Button(onClick = {wrapper.getToken()}){ Text("Lorem ipsum")}
            Button(onClick = {wrapper.wrap()}){ Text("Lorem ipsum")}
        }
        Row(
            modifier = Modifier.fillMaxSize().weight(90f)){
            Column(
                modifier =  Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Card(
                    colors = CardColors(
                        Color(0xFF3F51B5), Color(0xFF9C27B0),
                        Color(0xff9933ff), Color(0xFF816BA8)
                    ),
                    modifier = Modifier
                        .height(600.dp)
                        .width(300.dp)
                        .offset{IntOffset(offsetX.value.roundToInt(), 0)}
                        .graphicsLayer {
                            rotationZ = (offsetX.value / 1000f) * maxRotationAngle
                        }
                        .pointerInput(Unit){
                            detectDragGestures (
                                onDrag = {change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch{
                                        offsetX.snapTo(offsetX.value + dragAmount.x)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch{
                                        if(offsetX.value < -100f){
                                            offsetX.animateTo(
                                                targetValue = -1500f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            delay(200)
                                            offsetX.snapTo(0f)
                                        }
                                        else if(offsetX.value > 100f){
                                            offsetX.animateTo(
                                                targetValue = 1500f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                            delay(200)
                                            offsetX.snapTo(0f)
                                        }
                                        else {
                                            offsetX.animateTo(
                                                targetValue = 0f,
                                                animationSpec = tween(durationMillis = 300)
                                            )
                                        }
                                    }
                                }
                            )
                        }

                ) {
                    AsyncImage(imageUrl, contentDescription = "Image")
                    if(offsetX.value>100){
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Add",
                            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                    else if(offsetX.value < -100){
                        Icon(
                            painter = painterResource(R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
    
}