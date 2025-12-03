package com.example.gameswiper.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.gameswiper.model.Genre
import com.example.gameswiper.model.Platform

val CLIENT_ID = "p974jcjhdift50t7313a0qaopxe37x"
val CLIENT_SECRET =  "is0rxe8d3fab4oyt16o5fvlsc92wmi"
var ACCESS_TOKEN: String? = "r3vh3ilrb4wbhme7sbdumjsv8i3cwq"
var EXPIRES_IN: Long = 4832829
val MAX_GAME_ID = 377600
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "userData"
)


val PLATFORMS: List<Platform> = listOf(
    Platform(6, "PC"),
    Platform(167, "PlayStation 5"),
    Platform(169, "Xbox Series X|S"),
    Platform(130, "Nintendo Switch"),
    Platform(48, "Playstation 4"),
    Platform(12, "Xbox 360"),
    Platform(9, "Playstation 3")
)

val GENRES: List<Genre> = listOf(
    Genre(2, "Point-and-click"),
    Genre(4, "Fighting"),
    Genre(5, "Shooter"),
    Genre(7, "Music"),
    Genre(8, "Platform"),
    Genre(9, "Puzzle"),
    Genre(10, "Racing"),
    Genre(11, "RTS"),
    Genre(12, "RPG"),
    Genre(13, "Simulator"),
    Genre(14, "Sport"),
    Genre(15, "Strategy"),
    Genre(16, "TBS"),
    Genre(24, "Tactical"),
    Genre(25, "Hack and slash"),
    Genre(26, "Quiz"),
    Genre(31, "Adventure"),
    Genre(32, "Indie"),
    Genre(33, "Arcade"),
    Genre(34, "Visual novel"),
    Genre(36, "MOBA")
)
