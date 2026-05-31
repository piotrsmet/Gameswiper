package com.example.gameswiper

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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
import androidx.core.view.WindowCompat
import com.example.gameswiper.composable.MainScreen
import com.example.gameswiper.viewmodel.LibraryViewModel
import com.example.gameswiper.viewmodel.SettingsViewModel
import com.example.gameswiper.viewmodel.SwipeViewModel
import com.example.gameswiper.viewmodel.UserViewModel
import com.example.gameswiper.network.GamesWrapper
import com.example.gameswiper.repository.GameRepository
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.service.Scheduler
import com.example.gameswiper.ui.theme.GameswiperTheme

class MainActivity : ComponentActivity() {
    private val swipeViewModel: SwipeViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    val userRepository = UserRepository()
    val gameRepository = GameRepository()
    val gamesWrapper = GamesWrapper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gamesWrapper.getStaticToken()
        Scheduler.scheduleNotification(this, 17, 0)
        userViewModel.fetchUserPreferences(userRepository)
        userViewModel.fetchFriends(userRepository)
        libraryViewModel.fetchSavedGames(this, gamesWrapper, gameRepository)

        enableEdgeToEdge()
        setContent {
            GameswiperTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        context = this,
                        swipeViewModel = swipeViewModel,
                        libraryViewModel = libraryViewModel,
                        userViewModel = userViewModel,
                        settingsViewModel = settingsViewModel
                    ) { loggedOut() }
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

    override fun onStop() {
        super.onStop()
        val userDisplay = userViewModel.userDisplay.value
        val userPreferences = userViewModel.userPreferences.value

        userViewModel.saveUserPreferences(userRepository)
        userRepository.setUserDisplay(userDisplay)
        swipeViewModel.saveCardsToDataStore(this)
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