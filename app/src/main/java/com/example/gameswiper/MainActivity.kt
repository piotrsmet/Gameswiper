package com.example.gameswiper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.composable.ImageBackground
import com.example.gameswiper.composable.MainScreen
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.service.Scheduler
import com.example.gameswiper.ui.theme.GameswiperTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: GamesViewModel by viewModels()
    val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        Scheduler.scheduleNotification(this, 17, 0)
        viewModel.fetchUserPreferences(userRepository)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameswiperTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding), this, viewModel) { loggedOut() }
                }
            }
        }


    }
    override fun onStop() {
        super.onStop()
        val userDisplay = viewModel.userDisplay.value
        val userPreferences = viewModel.userPreferences.value

        viewModel.saveUserPreferences(userRepository)
        userRepository.setUserDisplay(userDisplay)
        viewModel.saveCardsToDataStore(this)
    }





    private fun loggedOut(){
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GameswiperTheme {
        Greeting("Android")
    }
}