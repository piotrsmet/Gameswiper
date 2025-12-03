package com.example.gameswiper.model

data class UserDisplay (
    val id: String = "0",
    val name: String = "Unknown",
    val profilePicture: String = "",
    val swiped: Int = 0,
    val liked: Int = 0,
    val disliked: Int = 0
){
    constructor() : this("0", "Unknown", "", 0, 0, 0)
}
