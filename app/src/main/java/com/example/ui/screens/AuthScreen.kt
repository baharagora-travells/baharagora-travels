package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class GoogleAccount(
    val name: String,
    val email: String,
    val color: Color
)

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    var showGooglePicker by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var selectedAccountEmail by remember { mutableStateOf("") }
    
    // User accounts list
    val accounts = remember {
        mutableStateListOf(
            GoogleAccount("Chandan", "chandan75as@gmail.com", Color(0xFFE65100)),
            GoogleAccount("Chandan Travels", "support.baharagora@gmail.com", Color(0xFF0277BD))
        )
    }

    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(300) // Small delay before animation starts
        visible = true
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = tween(1200)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome to",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 0.dp)
                    )
                    Text(
                        text = "Baharagora\nTravells",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        lineHeight = 44.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 400))
            ) {
                Text(
                    text = "Discover the beautiful world with us",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200, delayMillis = 800)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(1200, delayMillis = 800)
                )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        Button(
                            onClick = { showGooglePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Account Icon",
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(28.dp)
                            )
                            Text(
                                text = "Continue with Google",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Animated Google Account Picker overlay (Bottom Sheet style)
        AnimatedVisibility(
            visible = showGooglePicker,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { 
                        if (!isSigningIn) showGooglePicker = false 
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                // Prevent clicks inside the bottom sheet from dismissing it
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(36.dp, 4.dp)
                                .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                .padding(bottom = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Header with custom styled Google colors logo
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                            
                            if (!isSigningIn) {
                                IconButton(onClick = { showGooglePicker = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Picker")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Choose an account",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "to continue to Baharagora Travells",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isSigningIn) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Signing in as $selectedAccountEmail...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Google Accounts list
                            Column {
                                accounts.forEach { account ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                selectedAccountEmail = account.email
                                                isSigningIn = true
                                                scope.launch {
                                                    delay(1200) // Realistic loading delay
                                                    isSigningIn = false
                                                    showGooglePicker = false
                                                    onAuthSuccess()
                                                }
                                            }
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(account.color, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = account.name.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = account.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = account.email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Divider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 8.dp))
                                }

                                // Custom Account choice option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            showAddAccountDialog = true
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add account",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text(
                                        text = "Add another account",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "To continue, Google will share your name, email address, language preference, and profile picture with Baharagora Travells. Before using this app, you can review its privacy policy and terms of service.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = TextAlign.Justify,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        // Add account Dialog
        if (showAddAccountDialog) {
            AlertDialog(
                onDismissRequest = { showAddAccountDialog = false },
                title = { Text("Add Google Account") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newEmail,
                            onValueChange = { newEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newName.isNotBlank() && newEmail.isNotBlank()) {
                                val randomColor = listOf(
                                    Color(0xFFE53935), Color(0xFFD81B60), Color(0xFF8E24AA),
                                    Color(0xFF5E35B1), Color(0xFF3949AB), Color(0xFF1E88E5),
                                    Color(0xFF039BE5), Color(0xFF00ACC1), Color(0xFF00897B),
                                    Color(0xFF43A047)
                                ).random()
                                accounts.add(GoogleAccount(newName, newEmail, randomColor))
                                newName = ""
                                newEmail = ""
                                showAddAccountDialog = false
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddAccountDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
