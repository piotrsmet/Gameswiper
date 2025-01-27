package com.example.gameswiper.model

data class Game(
    val id: Int = 0,
    val cover: Int = 0,
    val genres: List<Int>,
    val name: String = "",
    val platforms: List<Int>,
    val themes: List<Int>,
    val summary: String = ""
)
