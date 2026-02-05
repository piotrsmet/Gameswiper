package com.example.gameswiper.model

data class Game(
    val id: Int = 0,
    val cover: Int = 0,
    val video: Int = 0,
    val genres: MutableList<Int> = mutableListOf(),
    val name: String = "",
    val platforms: MutableList<Int> = mutableListOf(),
    val themes: MutableList<Int> = mutableListOf(),
    val summary: String = "",
    val similarGames: MutableList<Int> = mutableListOf(),
    val liked: Boolean = false,
    var dateOfAddition : Long = 0
){
    constructor() : this(0, 0, 0, mutableListOf(), "", mutableListOf(), mutableListOf(), "", mutableListOf(), false, 0)
}


