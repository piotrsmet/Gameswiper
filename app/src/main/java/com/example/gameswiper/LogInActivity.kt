package com.example.gameswiper

import com.example.gameswiper.composable.ImageBackgroundAuth
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.gameswiper.ui.theme.GameswiperTheme
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : ComponentActivity(){
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GameswiperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    auth = FirebaseAuth.getInstance()
                    val prefs = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

                    if(auth.currentUser != null &&
                        prefs.getString("SETTINGS", null) != "choosing_preferences" &&
                        prefs.getString("SETTINGS", null) != "choosing_avatar"){
                        loggedIn()
                    }
                    ImageBackgroundAuth(Modifier.padding(innerPadding), { loggedIn() }, this)

                }
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
    fun loggedIn(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }


}