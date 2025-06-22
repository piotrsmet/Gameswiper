package com.example.gameswiper.repository

import com.example.gameswiper.model.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class SettingsRepository {
    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    fun setSettings(genres: List<Int>, platforms: List<Int>){
        if(user != null){
            firestore
                .collection("users")
                .document(user.uid)
                .collection("settings")
                .document(user.uid)
                .set(Settings(genres, platforms))
                .addOnSuccessListener {  }
                .addOnFailureListener {  }

        }
    }

    suspend fun getSettings(): Settings{
        var settings = Settings()
        if(user != null){
            val snapshot = firestore
                .collection("users")
                .document(user.uid)
                .collection("settings")
                .document(user.uid)
                .get()
                .await()
            settings = snapshot.toObject(Settings::class.java)!!
        }
        return settings
    }
}