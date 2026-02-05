package com.example.gameswiper.model

data class GameWithMedia(
    val id: Int,
    var game: Game,
    val cover: String,
    val video: String
) {
}