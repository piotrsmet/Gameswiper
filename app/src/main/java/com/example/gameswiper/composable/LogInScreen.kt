package com.example.gameswiper.composable

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gameswiper.model.SettingsViewModel
import com.example.gameswiper.model.UserViewModel
import com.example.gameswiper.model.UserDisplay
import com.example.gameswiper.repository.UserRepository
import com.example.gameswiper.utils.GENRES
import com.example.gameswiper.utils.PLATFORMS
import com.example.gameswiper.utils.AVATARS
import com.example.gameswiper.utils.DETAILS_COLOR
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
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color(0xFF2A2A2A),
            unfocusedContainerColor = Color(0xFF1A1A1A),
            focusedIndicatorColor = DETAILS_COLOR,
            unfocusedIndicatorColor = Color(0xFF3A3A3A),
            focusedLabelColor = DETAILS_COLOR,
            unfocusedLabelColor = Color.Gray,
            cursorColor = DETAILS_COLOR,
            focusedLeadingIconColor = DETAILS_COLOR,
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Witaj ponownie",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Zaloguj się do swojego konta",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Hasło",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DETAILS_COLOR)
            ) {
                Text(
                    text = "Zaloguj się",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "Nie masz konta? ",
                    color = Color.Gray
                )
                Text(
                    text = "Zarejestruj się",
                    color = DETAILS_COLOR,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier,
    userViewModel: UserViewModel
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Utwórz konto",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Dołącz do społeczności graczy",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            CustomTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nazwa użytkownika",
                leadingIcon = Icons.Default.Person
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Hasło",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(12.dp))

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Potwierdź hasło",
                leadingIcon = Icons.Default.Lock,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) return@Button
                    userViewModel.updateUserDisplay(
                        UserDisplay(name = username),
                        null
                    )
                    onRegister(username, email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DETAILS_COLOR)
            ) {
                Text(
                    text = "Zarejestruj się",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(text = "Masz już konto? ", color = Color.Gray)
                Text(
                    text = "Zaloguj się",
                    color = DETAILS_COLOR,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AvatarSelection(
    modifier: Modifier = Modifier,
    context: Context,
    onAvatarSelected: () -> Unit,
    userViewModel: UserViewModel
) {
    val icons = AVATARS
    var selected by remember { mutableIntStateOf(0) }
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("SETTINGS", "choosing_avatar").apply()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Wybierz avatar",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Jak chcesz wyglądać?",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            icons.forEachIndexed { index, icon ->
                val iconSize by animateDpAsState(
                    targetValue = if (selected == index) 72.dp else 56.dp,
                    animationSpec = tween(durationMillis = 300),
                    label = "avatar size"
                )

                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { selected = index }
                        ),
                    shape = CircleShape,
                    color = if (selected == index) Color(0xFF2A2A2A) else Color(0xFF1A1A1A),
                    border = if (selected == index) BorderStroke(3.dp, DETAILS_COLOR)
                    else BorderStroke(1.dp, Color(0xFF3A3A3A))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                prefs.edit().putInt("AVATAR", selected).apply()
                userViewModel.updateUserDisplay(
                    UserDisplay(
                        name = userViewModel.userDisplay.value.name,
                        profilePicture = selected.toString()
                    ),
                    null
                )
                onAvatarSelected()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DETAILS_COLOR)
        ) {
            Text(
                text = "Dalej",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AuthScreen(
    modifier: Modifier,
    onLoginSuccess: () -> Unit,
    context: Context,
    onRegisterSuccess: () -> Unit,
    userViewModel: UserViewModel
) {
    var currentScreen by remember { mutableStateOf("login") }
    val auth = FirebaseAuth.getInstance()

    when (currentScreen) {
        "login" -> LoginScreen(
            onLogin = { email, password ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) onLoginSuccess()
                        else Toast.makeText(context, "Nieprawidłowy email lub hasło", Toast.LENGTH_SHORT).show()
                    }
            },
            onNavigateToRegister = { currentScreen = "register" },
            modifier
        )

        "register" -> RegisterScreen(
            onRegister = { username, email, password ->
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) onRegisterSuccess()
                        else Toast.makeText(context, "Rejestracja nie powiodła się", Toast.LENGTH_SHORT).show()
                    }
            },
            onNavigateToLogin = { currentScreen = "login" },
            modifier,
            userViewModel
        )
    }
}

@Composable
fun ChoosingPreferences(onSettingsSet: () -> Unit, context: Context, settingsViewModel: SettingsViewModel, userViewModel: UserViewModel) {
    val genres = GENRES
    val platforms = PLATFORMS
    val modelGenres by settingsViewModel.selectedGenres.collectAsState()
    val modelPlatforms by settingsViewModel.selectedPlatforms.collectAsState()
    val userRepository = UserRepository()
    val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

    val checkedGenres = remember { mutableStateMapOf<String, Boolean>() }
    val checkedPlatforms = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Wybierz preferencje",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Pomóż nam dopasować gry do Twoich gustów",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Gatunki (min. 6)",
                color = DETAILS_COLOR,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .heightIn(min = 120.dp, max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(genres) { genre ->
                        val isChecked = checkedGenres[genre.name] ?: false
                        val bgColor by animateColorAsState(
                            targetValue = if (isChecked) DETAILS_COLOR.copy(alpha = 0.3f) else Color(0xFF2A2A2A),
                            label = "genre bg"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isChecked) 1.02f else 1f,
                            label = "genre scale"
                        )

                        Surface(
                            modifier = Modifier
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        checkedGenres[genre.name] = !isChecked
                                        if (!isChecked) settingsViewModel.addGenre(genre.id)
                                        else settingsViewModel.removeGenre(genre.id)
                                    }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = if (isChecked) BorderStroke(2.dp, DETAILS_COLOR)
                            else BorderStroke(1.dp, Color(0xFF3A3A3A))
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
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DETAILS_COLOR,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Platformy",
                color = DETAILS_COLOR,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                border = BorderStroke(1.dp, DETAILS_COLOR.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    modifier = Modifier
                        .padding(12.dp)
                        .heightIn(min = 100.dp, max = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(platforms) { platform ->
                        val isChecked = checkedPlatforms[platform.name] ?: false
                        val bgColor by animateColorAsState(
                            targetValue = if (isChecked) DETAILS_COLOR.copy(alpha = 0.3f) else Color(0xFF2A2A2A),
                            label = "platform bg"
                        )
                        val scale by animateFloatAsState(
                            targetValue = if (isChecked) 1.02f else 1f,
                            label = "platform scale"
                        )

                        Surface(
                            modifier = Modifier
                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        checkedPlatforms[platform.name] = !isChecked
                                        if (!isChecked) settingsViewModel.addPlatform(platform.id)
                                        else settingsViewModel.removePlatform(platform.id)
                                    }
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = bgColor,
                            border = if (isChecked) BorderStroke(2.dp, DETAILS_COLOR)
                            else BorderStroke(1.dp, Color(0xFF3A3A3A))
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
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = DETAILS_COLOR,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val canProceed = modelGenres.size > 5 && modelPlatforms.isNotEmpty()

            Button(
                onClick = {
                    userRepository.setSettings(modelGenres.toList(), modelPlatforms.toList())
                    userRepository.setUserDisplay(userViewModel.userDisplay.value)
                    prefs.edit().putString("SETTINGS", "done").apply()
                    onSettingsSet()
                },
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DETAILS_COLOR,
                    disabledContainerColor = Color(0xFF2A2A2A)
                )
            ) {
                Text(
                    text = "Rozpocznij",
                    color = if (canProceed) Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
    val settingsViewModel: SettingsViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

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
                userViewModel = userViewModel
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
            AvatarSelection(
                modifier = modifier,
                context = context,
                onAvatarSelected = {
                    currentScreen = "first_settings"
                    prefs.edit().putString("SETTINGS", "choosing_preferences").apply()
                },
                userViewModel = userViewModel
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
            ChoosingPreferences(onLoginSuccess, context, settingsViewModel, userViewModel)
        }
    }
}
