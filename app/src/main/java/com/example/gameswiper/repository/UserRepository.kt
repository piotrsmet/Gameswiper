package com.example.gameswiper.repository

import android.util.Log
import com.example.gameswiper.model.Friend
import com.example.gameswiper.model.Settings
import com.example.gameswiper.model.UserDisplay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class UserRepository {
    val firestore = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser


    fun setUserDisplay(userDisplay: UserDisplay){

        if(user != null){
            val userDisplay2 = UserDisplay(
                user.uid,
                userDisplay.name,
                userDisplay.profilePicture,
                userDisplay.swiped,
                userDisplay.liked,
                userDisplay.disliked
            )
            firestore
                .collection("users")
                .document(user.uid)
                .collection("userDisplay")
                .document(user.uid)
                .set(userDisplay2)
                .addOnSuccessListener { Log.i("suckess", "suckess") }
                .addOnFailureListener { Log.e("failureee", it.message.toString()) }

        }
    }

    suspend fun getUserDisplay(): UserDisplay{
        var userDisplay = UserDisplay()
        if(user != null){
            val snapshot = firestore
                .collection("users")
                .document(user.uid)
                .collection("userDisplay")
                .document(user.uid)
                .get()
                .await()
            userDisplay = snapshot.toObject(UserDisplay::class.java)!!
        }
        return userDisplay
    }

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

    fun setUserPreferences(prefs: HashMap<String, Int>){
        if(user != null){
            firestore
                .collection("users")
                .document(user.uid)
                .collection("preferences")
                .document(user.uid)
                .set(prefs)
                .addOnSuccessListener { Log.i("suckess", prefs.toString()) }
                .addOnFailureListener { Log.e("failureee", it.message.toString()) }
        }
    }


    suspend fun getUserPreferences(): HashMap<String, Int> {
        val prefs = hashMapOf<String, Int>()
        if (user != null) {
            val snapshot = firestore
                .collection("users")
                .document(user.uid)
                .collection("preferences")
                .document(user.uid)
                .get()
                .await()

            val raw = snapshot.data
            raw?.forEach { (k, v) ->
                val keyStr = k?.toString() ?: return@forEach
                val valueInt = when (v) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull()
                    else -> null
                }
                if (valueInt != null) {
                    prefs[keyStr] = valueInt
                }
            }
        }
        return prefs
    }

    suspend fun getFriends(): List<UserDisplay>{
        val friends = mutableListOf<UserDisplay>()
        if(user != null){
            val snapshot = firestore
                .collection("users")
                .document(user.uid)
                .collection("friends")
                .get()
                .await()

            if(!snapshot.isEmpty){
                val snapshot2 = firestore.collectionGroup("userDisplay")
                    .whereIn("name", snapshot.documents.map { it.getString("name") })
                    .get()
                    .await()
                friends.addAll(snapshot2.toObjects(UserDisplay::class.java))
            }
        }
        return friends
    }

    suspend fun addFriend(friendName: String): UserDisplay? {
        if (user == null) return null

        return try {
            val snapshot = firestore.collectionGroup("userDisplay")
                .whereEqualTo("name", friendName)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val friendDisplay = snapshot.documents[0].toObject(UserDisplay::class.java)
                if (friendDisplay != null) {
                    firestore
                        .collection("users")
                        .document(user.uid)
                        .collection("friends")
                        .document(friendDisplay.id)
                        .set(Friend(friendName))
                        .await()

                    Log.i("UserRepository", "Friend added: ${friendDisplay.name}")

                    return friendDisplay
                }
            } else {
                Log.i("UserRepository", "User not found")
            }
            null
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding friend: ${e.message}")
            null
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