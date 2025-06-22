package com.example.gameswiper

import AuthScreen
import ImageBackgroundAuth
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.composable.ImageBackground
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.SettingsRepository
import com.example.gameswiper.ui.theme.GameswiperTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.remote.Datastore
import java.util.prefs.Preferences

class LogInActivity : ComponentActivity(){
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameswiperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    auth = FirebaseAuth.getInstance()
                    val prefs = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

                    if(auth.currentUser != null && prefs.getString("SETTINGS", null) != "not_done") {
                        loggedIn()
                    }
                    ImageBackgroundAuth(Modifier.padding(innerPadding), { loggedIn() }, this)

                }
            }
        }
    }
    fun loggedIn(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}