package com.example.myapplication.Module6.Task3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun AuthApp() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val repository = remember { AuthRepositoryImpl(tokenManager) }
    val viewModel = remember { AuthViewModel(repository) }

    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9FB)
    ) {
        when (val s = state) {
            is AuthUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF3C4D84))
                }
            }
            is AuthUiState.Login -> {
                LoginScreen(
                    errorMessage = null,
                    onLogin = { user, pass -> viewModel.login(user, pass) }
                )
            }
            is AuthUiState.Error -> {
                if (s.isLoginError) {
                    LoginScreen(
                        errorMessage = s.message,
                        onLogin = { user, pass -> viewModel.login(user, pass) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(s.message, color = Color(0xFFD32F2F), fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.loadUsers() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4D84))
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }
            }
            is AuthUiState.UsersList -> {
                UsersListScreen(
                    users = s.users,
                    onUserClick = { viewModel.openUserDetails(it) },
                    onLogout = { viewModel.logout() }
                )
            }
            is AuthUiState.UserDetail -> {
                UserDetailScreen(
                    user = s.user,
                    onBack = { viewModel.backToUsersList() },
                    onLogout = { viewModel.logout() }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    errorMessage: String?,
    onLogin: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("emilys") }
    var password by remember { mutableStateOf("emilyspass") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Вход",
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF1F1F1F)
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = Color(0xFFD32F2F), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { onLogin(username, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4D84)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Войти", fontSize = 16.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListScreen(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Пользователи",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1F1F1F)
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Text("⎋", fontSize = 24.sp, color = Color(0xFF1F1F1F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(users, key = { it.id }) { user ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE5E7EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUserClick(user) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = user.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = user.email,
                                fontSize = 13.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    user: User,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали пользователя", fontSize = 20.sp, color = Color(0xFF1F1F1F)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF1F1F1F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            AsyncImage(
                model = user.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = user.fullName,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F1F1F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = user.email,
                fontSize = 15.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ID: ${user.id}",
                fontSize = 15.sp,
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4D84)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Выйти", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}