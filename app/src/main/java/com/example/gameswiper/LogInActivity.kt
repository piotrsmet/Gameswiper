package com.example.gameswiper

import AuthScreen
import ImageBackgroundAuth
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.gameswiper.composable.ImageBackground
import com.example.gameswiper.ui.theme.GameswiperTheme
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : ComponentActivity(){
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null)
            loggedIn()
        setContent {
            GameswiperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageBackgroundAuth(Modifier.padding(innerPadding), { loggedIn() }, this)

                }
            }
        }
    }
    private fun loggedIn(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


}