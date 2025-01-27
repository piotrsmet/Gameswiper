package com.example.gameswiper.model

import com.google.gson.annotations.SerializedName

data class GameResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("cover") val cover: Int,
    @SerializedName("genres") val genres: List<Int>,
    @SerializedName("name") val name: String,
    @SerializedName("platforms") val platforms: List<Int>,
    @SerializedName("storyline") val description: String,
    @SerializedName("themes") val themes: List<Int>
)

data class CoverViewModel(
    @SerializedName("id") val id: Int,
    @SerializedName("image_id") val imageId: String
)

data class GenreViewModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class ThemeViewModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)
