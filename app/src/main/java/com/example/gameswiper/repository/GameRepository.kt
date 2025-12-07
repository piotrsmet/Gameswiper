package com.example.gameswiper.repository

import android.util.Log
import com.example.gameswiper.model.Game
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GameRepository{
    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    fun addGame(game: Game){
        if (user != null){
            firestore
                .collection("users")
                .document(user.uid)
                .collection("games")
                .add(game)
                .addOnSuccessListener { println("successs") }
                .addOnFailureListener { println("failure") }
        }
    }

    suspend fun getGames(): List<Game>{
        val games = mutableListOf<Game>()

        if(user != null){
            val snapshot = firestore
                .collection("users")
                .document(user.uid)
                .collection("games")
                .get()
                .await()
            games.addAll(snapshot.toObjects(Game::class.java))
        }
        return games
    }

    fun deleteGame(id: Int, onSuccess: () -> Unit){
        if(user != null){
            firestore
                .collection("users")
                .document(user.uid)
                .collection("games")
                .whereEqualTo("cover", id)
                .get()
                .addOnSuccessListener { document ->
                    for(i in document){
                        i.reference.delete()
                            .addOnSuccessListener {
                                onSuccess()
                            }
                    }
                }
                .addOnFailureListener { Log.i("Porażka", "faioldsa") }
        }
    }
}