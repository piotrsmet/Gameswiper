package com.example.gameswiper.composable

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.model.GamesViewModel
import com.example.gameswiper.model.UserDisplay
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(13.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.Gray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            cursorColor = Color.White,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.Gray
        )
    )
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome back", color = Color.White, fontSize = 28.sp)

            Spacer(Modifier.height(20.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Log in", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register", color = Color.White)
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier,
    viewModel: GamesViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val userDisplay by viewModel.userDisplay.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create account", color = Color.White, fontSize = 26.sp)

            Spacer(Modifier.height(16.dp))

            CustomTextField(
                value = username,
                onValueChange = { username = it },
                label = "Username",
                leadingIcon = Icons.Default.Person
            )

            Spacer(Modifier.height(8.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(Modifier.height(8.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(8.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) return@Button
                    viewModel.updateUserDisplay(
                        UserDisplay(
                            name = username,
                        )
                    )
                    onRegister(username, email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("Register", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Log in", color = Color.White)
            }
        }
    }
}

@Composable
fun AvatarSelection(
    modifier: Modifier = Modifier,
    context: Context,
    onAvatarSelected: () -> Unit,
    viewModel: GamesViewModel
) {
    val icons = listOf(
        Icons.Default.Person,
        Icons.Default.AccountCircle,
        Icons.Default.Face,
        Icons.Default.Check,
    )
    var selected by remember { mutableIntStateOf(0) }
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("SETTINGS", "choosing_avatar").apply()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2E2A7A))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Choose an avatar", color = Color.White, fontSize = 22.sp)
        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            icons.forEachIndexed { index, icon ->
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(interactionSource = remember { MutableInteractionSource() },
                            indication = null, onClick = {selected = index} ),
                    shape = CircleShape,
                    color = if (selected == index) Color(0xFF6F62E8) else Color(0xFF4635B1),
                    border = if (selected == index) BorderStroke(3.dp, Color.White) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                prefs.edit().putInt("AVATAR", selected).apply()
                viewModel.updateUserDisplay(
                    UserDisplay(
                        name = viewModel.userDisplay.value.name,
                        profilePicture = selected.toString()
                    )
                )
                onAvatarSelected()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("Next", color = Color.Black)
        }
    }
}

@Composable
fun AuthScreen(modifier: Modifier, onLoginSuccess: () -> Unit, context: Context, onRegisterSuccess: () -> Unit, viewModel: GamesViewModel) {

    var currentScreen by remember { mutableStateOf("login") }

    val auth = FirebaseAuth.getInstance()

    when (currentScreen) {
        "login" -> LoginScreen(
            onLogin = { email, password ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) onLoginSuccess()
                        else Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
                    }
            },
            onNavigateToRegister = { currentScreen = "register" },
            modifier
        )

        "register" -> RegisterScreen(
            onRegister = { username, email, password ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onRegisterSuccess()
                        } else {
                            Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            onNavigateToLogin = { currentScreen = "login" },
            modifier,
            viewModel
        )
    }
}

@Composable
fun ChoosingPreferences(onSettingsSet: () -> Unit, context: Context, viewModel: GamesViewModel) {
    val genres = GENRES
    val platforms = PLATFORMS
    val modelGenres by viewModel.selectedGenres.collectAsState()
    val modelPlatforms by viewModel.selectedPlatforms.collectAsState()
    val userRepository = UserRepository()
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0B0B))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Wybierz gatunki",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Surface(
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF13111F),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .heightIn(min = 120.dp, max = 260.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(genres) { genre ->
                        val isChecked = checkedGenres[genre.name] ?: false
                        val bgColor by androidx.compose.animation.animateColorAsState(
                            targetValue = if (isChecked) Color(0xFF4150B1) else Color(0x0013142A)
                        )
                        val scale by androidx.compose.animation.core.animateFloatAsState(if (isChecked) 1.03f else 1f)

                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        checkedGenres[genre.name] = !isChecked
                                        if (!isChecked) viewModel.addGenre(genre.id)
                                        else viewModel.removeGenre(genre.id)
                                    }
                                ),
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = if (isChecked) 6.dp else 0.dp,
                            color = bgColor,
                            border = if (isChecked) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF2A2A3A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = genre.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Wybierz platformy",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Surface(
                tonalElevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF13111F),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .heightIn(min = 120.dp, max = 260.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(platforms) { platform ->
                        val isChecked = checkedPlatforms[platform.name] ?: false
                        val bgColor by androidx.compose.animation.animateColorAsState(
                            targetValue = if (isChecked) Color(0xFF4150B1) else Color(0x0013142A)
                        )
                        val scale by androidx.compose.animation.core.animateFloatAsState(if (isChecked) 1.03f else 1f)

                        Surface(
                            modifier = Modifier
                                .padding(4.dp)
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable (
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        checkedPlatforms[platform.name] = !isChecked
                                        if (!isChecked) viewModel.addPlatform(platform.id)
                                        else viewModel.removePlatform(platform.id)
                                    }
                                ),
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = if (isChecked) 6.dp else 0.dp,
                            color = bgColor,
                            border = if (isChecked) BorderStroke(2.dp, Color.White) else BorderStroke(1.dp, Color(0xFF2A2A3A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = platform.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {


                val canProceed = modelGenres.isNotEmpty() && modelPlatforms.isNotEmpty()

                Button(
                    onClick = {
                        userRepository.setSettings(modelGenres.toList(), modelPlatforms.toList())
                        userRepository.setUserDisplay(viewModel.userDisplay.value)
                        prefs.edit().putString("SETTINGS", "done").apply()
                        onSettingsSet()
                    },
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4150B1),
                        disabledContainerColor = Color(0xFF2A2A3A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "Dalej", color = Color.White)
                }
            }
        }
    }
}


@Composable
fun ImageBackgroundAuth(modifier: Modifier, onLoginSuccess: () -> Unit, context: Context) {
    var currentScreen by remember { mutableStateOf("auth_screen") }
    val current by remember { mutableIntStateOf(1) }
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    val creatingAccountState = prefs.getString("SETTINGS", null)
    val viewModel: GamesViewModel = viewModel()
    when (creatingAccountState) {
        "choosing_avatar" -> currentScreen = "avatar_selection"
        "choosing_preferences" -> currentScreen = "first_settings"
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AnimatedVisibility(
            visible = currentScreen == "auth_screen",
            exit = slideOutHorizontally(
                targetOffsetX = { it * current },
                animationSpec = tween(500)
            )
        ) {
            AuthScreen(
                modifier,
                onLoginSuccess,
                context,
                onRegisterSuccess = { currentScreen = "avatar_selection" },
                viewModel = viewModel
            )
        }

        AnimatedVisibility(
            visible = currentScreen == "avatar_selection",
            enter = slideInHorizontally(
                initialOffsetX = { -it * current },
                animationSpec = tween(500)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { it * current },
                animationSpec = tween(500)
            )
        ) {
            AvatarSelection(modifier = modifier, context = context, onAvatarSelected = {
                currentScreen = "first_settings";
                prefs.edit().putString("SETTINGS", "choosing_preferences").apply()
            },
                viewModel = viewModel
            )
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
            ChoosingPreferences(onLoginSuccess, context, viewModel)
        }
    }
}
