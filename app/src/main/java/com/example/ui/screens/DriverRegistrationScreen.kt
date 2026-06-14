package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverRegistrationScreen(
    onRegistrationSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("baharagora_travels_pref", Context.MODE_PRIVATE) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var vehicleName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    
    // Choose vehicle type: Car (🚗), Bus (🚌), Auto (🛺), Bike (🏍️)
    val vehicleTypes = listOf(
        VehicleTypeItem("Car", "🚗", "Car / Cab"),
        VehicleTypeItem("Bus", "🚌", "Bus"),
        VehicleTypeItem("Auto", "🛺", "Auto Rickshaw"),
        VehicleTypeItem("Bike", "🏍️", "Motorcycle")
    )
    var selectedVehicleType by remember { mutableStateOf(vehicleTypes[0]) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var animateIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animateIn = true
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
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
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Partner Registration",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                AnimatedVisibility(
                    visible = animateIn,
                    enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = tween(600)
                    )
                ) {
                    Column {
                        Text(
                            text = "Register to Drive & Earn 🤝",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Text(
                            text = "Provide your correct vehicle and contact details to join Baharagora Travells.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }

                // Error Message block
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    errorMessage?.let { msg ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Form Fields Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Personal Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Driver Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mobile Number
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number (10 digits)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Vehicle Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Vehicle Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "Select Vehicle Type",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Grid of Vehicle Types
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 2
                        ) {
                            vehicleTypes.forEach { type ->
                                val isSelected = selectedVehicleType.id == type.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable { selectedVehicleType = type }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = type.emoji,
                                            fontSize = 28.sp,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Text(
                                            text = type.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Vehicle Name
                        OutlinedTextField(
                            value = vehicleName,
                            onValueChange = { vehicleName = it },
                            label = { Text("Vehicle Name (e.g. Maruti Swift)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Vehicle Number
                        OutlinedTextField(
                            value = vehicleNumber,
                            onValueChange = { vehicleNumber = it },
                            label = { Text("Vehicle Number (e.g. JH-05-CD-1234)") },
                            placeholder = { Text("JH-05-XY-1234") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Vehicle Color
                        OutlinedTextField(
                            value = vehicleColor,
                            onValueChange = { vehicleColor = it },
                            label = { Text("Vehicle Colour (e.g. Royal Blue / White)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Register Button
                Button(
                    onClick = {
                        errorMessage = null
                        if (name.isBlank()) {
                            errorMessage = "Please enter your Name"
                            return@Button
                        }
                        if (phone.isBlank() || phone.length < 10) {
                            errorMessage = "Please enter a valid 10-digit mobile number"
                            return@Button
                        }
                        if (vehicleName.isBlank()) {
                            errorMessage = "Please enter your vehicle brand & name"
                            return@Button
                        }
                        if (vehicleNumber.isBlank()) {
                            errorMessage = "Please enter your vehicle registration plate number"
                            return@Button
                        }
                        if (vehicleColor.isBlank()) {
                            errorMessage = "Please enter your vehicle color"
                            return@Button
                        }

                        // Save details in preference
                        isSubmitting = true
                        sharedPrefs.edit().apply {
                            putBoolean("driver_registered", true)
                            putString("driver_name", name.trim())
                            putString("driver_phone", phone.trim())
                            putString("driver_vehicle_type", selectedVehicleType.id)
                            putString("driver_vehicle_emoji", selectedVehicleType.emoji)
                            putString("driver_vehicle_name", vehicleName.trim())
                            putString("driver_vehicle_number", vehicleNumber.trim().uppercase())
                            putString("driver_vehicle_color", vehicleColor.trim())
                            apply()
                        }

                        isSubmitting = false
                        onRegistrationSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 8.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Register & Go Online 🚀",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

data class VehicleTypeItem(
    val id: String, // "Car", "Bus", "Auto", "Bike"
    val emoji: String,
    val label: String
)
