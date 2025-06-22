package com.example.gameswiper.model

data class Settings (
    val genres: List<Int> = mutableListOf(),
    val platforms: List<Int> = mutableListOf()
){
    constructor() : this(mutableListOf(), mutableListOf())
}