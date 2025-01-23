package com.example.gameswiper.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@Composable
fun MainScreen(modifier: Modifier){
    var offsetX = remember { Animatable(0f) }
    var coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        Row(
            modifier = Modifier.padding(horizontal = 10.dp).weight(10f)){
            Button(onClick = {}){ Text("Lorem ipsum")}
            Button(onClick = {}){ Text("Lorem ipsum")}
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
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 300)
                                        )
                                    }
                                }
                            )
                        }

                ) {

                }
            }
        }
    }
    
}