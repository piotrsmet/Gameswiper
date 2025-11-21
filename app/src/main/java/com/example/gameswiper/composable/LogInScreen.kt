import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gameswiper.R
import com.example.gameswiper.composable.FirstLogin
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.White
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonColors(Color.White, Color.Black, Color.White, Color.White)
        ) {
            Text("Log in")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Don't have an account? Register", color = Color.White)
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.White
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onRegister(email, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonColors(Color.White, Color.Black, Color.White, Color.White)
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Already have an account? Log in", color = Color.White)
        }
    }
}

@Composable
fun AuthScreen(modifier: Modifier, onLoginSuccess: () -> Unit, context: Context, onRegisterSuccess: () -> Unit) {

    var currentScreen by remember { mutableStateOf("login") }

    val auth = FirebaseAuth.getInstance()
    val validEmail by remember { mutableStateOf("") }
    val validPassword by remember { mutableStateOf("") }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLogin = { email, password ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful) onLoginSuccess()
                        else Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                println("Log in with $email and $password")
            },
            onNavigateToRegister = { currentScreen = "register" },
            modifier
        )

        "register" -> RegisterScreen(
            onRegister = { email, password ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                        if(task.isSuccessful) onRegisterSuccess()
                        else Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
                println("Register with $email and $password")
            },
            onNavigateToLogin = { currentScreen = "login" },
            modifier
        )
    }
}

@Composable
fun ImageBackgroundAuth(modifier: Modifier, onLoginSuccess: () -> Unit, context: Context) {
    var currentScreen by remember { mutableStateOf("auth_screen") }
    var current by remember { mutableIntStateOf(1) }
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    if(prefs.getString("SETTINGS", null) == "not_done"){
        currentScreen = "first_settings"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background2),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize(),

        )

        AnimatedVisibility(
            visible = currentScreen == "auth_screen",
            exit = slideOutHorizontally(
                targetOffsetX = { it * current },
                animationSpec = tween(500)
            )
        ) {
            AuthScreen(modifier, onLoginSuccess, context, onRegisterSuccess =  {currentScreen = "first_settings"})
        }
        AnimatedVisibility(
            visible = currentScreen == "first_settings",
            enter = slideInHorizontally(
                initialOffsetX = { -it * current },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it * current },
                animationSpec = tween(500)
            )
        ) {
            FirstLogin(onLoginSuccess, context)
        }


    }
}