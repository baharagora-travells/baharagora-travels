package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Helper to draw clean circular pins for map overlays
fun createEmojiMarkerDrawable(context: Context, emoji: String): BitmapDrawable {
    val size = 110 // px
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw white circular background pin
    val bgPaint = Paint().apply {
        color = AndroidColor.WHITE
        isAntiAlias = true
        setShadowLayer(8f, 0f, 4f, AndroidColor.DKGRAY)
    }
    canvas.drawCircle(size / 2f, size / 2.2f, size / 2.6f, bgPaint)
    
    // Draw bottom anchor triangle
    val path = android.graphics.Path().apply {
        moveTo(size / 2f - 16f, size / 1.5f)
        lineTo(size / 2f + 16f, size / 1.5f)
        lineTo(size / 2f, size - 8f)
        close()
    }
    val arrowPaint = Paint().apply {
        color = AndroidColor.WHITE
        isAntiAlias = true
    }
    canvas.drawPath(path, arrowPaint)
    
    // Print emoji inside the bubble
    val paint = Paint().apply {
        textSize = 52f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    val yPos = (size / 2.2f) - ((paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(emoji, size / 2f, yPos, paint)
    
    return BitmapDrawable(context.resources, bitmap)
}

data class SimulatedDriver(
    val id: String,
    val name: String,
    val emoji: String,
    val vehicle: String,
    val vehicleNo: String,
    val phone: String,
    val point: GeoPoint
)

data class VehicleOption(
    val type: String,
    val label: String,
    val emoji: String,
    val price: String,
    val estimate: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(role: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Load registration details for Driver
    val sharedPrefs = remember { context.getSharedPreferences("baharagora_travels_pref", Context.MODE_PRIVATE) }
    val driverName = remember { sharedPrefs.getString("driver_name", "Driver Partner") ?: "Driver Partner" }
    val driverPhone = remember { sharedPrefs.getString("driver_phone", "9876543210") ?: "9876543210" }
    val driverVehicleType = remember { sharedPrefs.getString("driver_vehicle_type", "Car") ?: "Car" }
    val driverVehicleEmoji = remember { sharedPrefs.getString("driver_vehicle_emoji", "🚗") ?: "🚗" }
    val driverVehicleName = remember { sharedPrefs.getString("driver_vehicle_name", "Cab") ?: "Cab" }
    val driverVehicleNumber = remember { sharedPrefs.getString("driver_vehicle_number", "JH-05-CD-9988") ?: "JH-05-CD-9988" }
    val driverVehicleColor = remember { sharedPrefs.getString("driver_vehicle_color", "White") ?: "White" }

    // Initialize osmDroid
    LaunchedEffect(Unit) {
        val osmdroidPrefs = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        Configuration.getInstance().load(context, osmdroidPrefs)
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Coordinates variables
    var currentGeoPoint by remember { mutableStateOf(GeoPoint(22.2778, 86.7208)) } // Default Baharagora center
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    // Read real location if available
    LaunchedEffect(Unit) {
        try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }
            bestLocation?.let {
                currentGeoPoint = GeoPoint(it.latitude, it.longitude)
            }
        } catch (e: SecurityException) {
            // Handled gracefully
        }
    }

    // Nearby Simulated Drivers (For Passenger)
    val nearbyDrivers = remember(currentGeoPoint) {
        listOf(
            SimulatedDriver("d1", "Sunil Soren", "🚗", "Swift Dzire", "JH-05-AB-1234", "9933887711", GeoPoint(currentGeoPoint.latitude + 0.0035, currentGeoPoint.longitude - 0.0025)),
            SimulatedDriver("d2", "Ankit Mahato", "🛺", "Bajaj Auto", "JH-05-BC-5678", "7008123456", GeoPoint(currentGeoPoint.latitude - 0.0025, currentGeoPoint.longitude + 0.004)),
            SimulatedDriver("d3", "Rajesh Patra", "🏍️", "Hero Splendor", "JH-05-XY-9024", "9122557788", GeoPoint(currentGeoPoint.latitude + 0.0015, currentGeoPoint.longitude + 0.0025)),
            SimulatedDriver("d4", "Baharagora Express", "🚌", "Traveler Bus", "JH-05-TR-9999", "9431102931", GeoPoint(currentGeoPoint.latitude - 0.004, currentGeoPoint.longitude - 0.003))
        )
    }

    // Passenger States
    var destinationSelectionExpanded by remember { mutableStateOf(false) }
    val destinations = listOf(
        "Baharagora Bus Stand 🚌",
        "Netaji Subhash Chowk 🚩",
        "Baharagora College 🏫",
        "Netaji Mahavidyalaya 🏢",
        "Khandamouda Crossing 🛣️",
        "Jamola Xing 📍"
    )
    var selectedDestination by remember { mutableStateOf(destinations[0]) }

    val vehicleOptions = listOf(
        VehicleOption("Car", "Cab/Car", "🚗", "₹120", "5 min"),
        VehicleOption("Auto", "Auto Rickshaw", "🛺", "₹45", "3 min"),
        VehicleOption("Bike", "Bike Cab", "🏍️", "₹25", "2 min"),
        VehicleOption("Bus", "Local Bus", "🚌", "₹15", "10 min")
    )
    var selectedVehicleOption by remember { mutableStateOf(vehicleOptions[1]) }

    // Passenger booking journey states: "idle", "requesting", "booked"
    var passengerJourneyState by remember { mutableStateOf("idle") }
    var activeBookedDriver by remember { mutableStateOf<SimulatedDriver?>(null) }

    // Driver States
    var isDriverOnline by remember { mutableStateOf(true) }
    // Driver mock requests status: "idle", "incoming", "accepted", "completed"
    var driverJourneyState by remember { mutableStateOf("idle") }
    
    // Simulate incoming passenger offer for Driver when online
    LaunchedEffect(isDriverOnline, driverJourneyState) {
        if (isDriverOnline && driverJourneyState == "idle") {
            delay(5000) // passenger books after 5 seconds of idle online
            driverJourneyState = "incoming"
        }
    }

    // Keep map reference for easy manual focus
    val mapView = remember { MapView(context) }

    DisposableEffect(mapView) {
        onDispose {
            mapView.onDetach()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = if (role == "driver") "Baharagora Driver Partner" else "Baharagora Travells",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (role == "driver") "Earn on the Go | Online" else "Discover the beautiful world",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (role == "driver") {
                        // Switcher button for Driver Online/Offline
                        Button(
                            onClick = { 
                                isDriverOnline = !isDriverOnline
                                if(!isDriverOnline) driverJourneyState = "idle"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDriverOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (isDriverOnline) "ONLINE" else "OFFLINE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // User Profile Emoji icon for passenger
                        IconButton(onClick = {}) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 18.sp)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // OpenStreet Map Native Component using AndroidView
            AndroidView(
                factory = {
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.2)
                        controller.setCenter(currentGeoPoint)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.overlays.clear()

                    // 1. Add current user marker
                    val userMarker = Marker(view).apply {
                        position = currentGeoPoint
                        icon = createEmojiMarkerDrawable(context, if (role == "driver") driverVehicleEmoji else "👤")
                        title = if (role == "driver") "$driverName (You)" else "You (Passenger)"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    view.overlays.add(userMarker)

                    // 2. Add role-dependent details
                    if (role == "passenger") {
                        // Add simulated nearby drivers on map
                        nearbyDrivers.forEach { driver ->
                            val driverMarker = Marker(view).apply {
                                position = driver.point
                                icon = createEmojiMarkerDrawable(context, driver.emoji)
                                title = "${driver.name} | ${driver.vehicle}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            view.overlays.add(driverMarker)
                        }
                    } else {
                        // If driver accepts dynamic booking, draw passenger icon on map
                        if (driverJourneyState == "accepted") {
                            val custMark = Marker(view).apply {
                                position = GeoPoint(currentGeoPoint.latitude + 0.002, currentGeoPoint.longitude - 0.003)
                                icon = createEmojiMarkerDrawable(context, "👤")
                                title = "Customer: Chandan"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            view.overlays.add(custMark)
                        }
                    }

                    view.invalidate()
                }
            )

            // Overlap control interfaces based on journeys
            if (role == "passenger") {
                // ========================== PASSENGER JOURNEY UI ==========================
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = passengerJourneyState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "passenger_ui"
                    ) { state ->
                        when (state) {
                            "idle" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "Where are you heading? 🗺️",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        // Beautiful Dropdown selector for destination
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                                .clickable { destinationSelectionExpanded = true }
                                                .padding(14.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.padding(end = 8.dp)
                                                    )
                                                    Text(
                                                        text = selectedDestination,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = destinationSelectionExpanded,
                                                onDismissRequest = { destinationSelectionExpanded = false },
                                                modifier = Modifier.fillMaxWidth(0.85f)
                                            ) {
                                                destinations.forEach { dest ->
                                                    DropdownMenuItem(
                                                        text = { Text(dest, fontWeight = FontWeight.Medium) },
                                                        onClick = {
                                                            selectedDestination = dest
                                                            destinationSelectionExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Horizontal vehicle selector row
                                        Text(
                                            text = "Select Vehicle Class",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(vehicleOptions) { opt ->
                                                val isSel = opt.type == selectedVehicleOption.type
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer 
                                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    ),
                                                    shape = RoundedCornerShape(16.dp),
                                                    modifier = Modifier
                                                        .width(105.dp)
                                                        .clickable { selectedVehicleOption = opt }
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(opt.emoji, fontSize = 28.sp)
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(opt.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                        Text(opt.price, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Request Button
                                        Button(
                                            onClick = {
                                                passengerJourneyState = "requesting"
                                                scope.launch {
                                                    delay(3000) // Finding driver takes 3 seconds
                                                    // Map the driver based on chosen vehicle type
                                                    activeBookedDriver = nearbyDrivers.firstOrNull { 
                                                        it.emoji == selectedVehicleOption.emoji 
                                                    } ?: nearbyDrivers[1] // Fallback to Auto
                                                    passengerJourneyState = "booked"
                                                }
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(54.dp)
                                        ) {
                                            Text(
                                                text = "Book ${selectedVehicleOption.label} now 🚀",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                            "requesting" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(56.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 5.dp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Finding your matching partner...",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Contacting nearby ${selectedVehicleOption.label} drivers around Baharagora...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(20.dp))
                                        
                                        TextButton(onClick = { passengerJourneyState = "idle" }) {
                                            Text("Cancel Request", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            "booked" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(56.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(activeBookedDriver?.emoji ?: "🛺", fontSize = 32.sp)
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = activeBookedDriver?.name ?: "Ramesh Kumar",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Baharagora Travel Partner ✅",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF2E7D32),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = activeBookedDriver?.vehicleNo ?: "JH-05-AB-0000",
                                                    modifier = Modifier.padding(8.dp),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column {
                                                Text("Vehice Class", fontSize = 11.sp, color = Color.Gray)
                                                Text(activeBookedDriver?.vehicle ?: "Auto", fontWeight = FontWeight.Bold)
                                            }
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Estimated Fare", fontSize = 11.sp, color = Color.Gray)
                                                Text(selectedVehicleOption.price, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Arriving in", fontSize = 11.sp, color = Color.Gray)
                                                Text("2 mins", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    // Simulate calling driver
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Call Partner", fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    passengerJourneyState = "idle"
                                                    activeBookedDriver = null
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Cancel Ride")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ========================== DRIVER JOURNEY UI ==========================
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = driverJourneyState,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "driver_ui"
                    ) { state ->
                        when (state) {
                            "idle" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(driverVehicleEmoji, fontSize = 28.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Welcome Back, $driverName!",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "$driverVehicleColor $driverVehicleName | $driverVehicleNumber",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        if (isDriverOnline) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Waiting for customer requests...",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32),
                                                    fontSize = 13.sp
                                                )
                                            }
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Go ONLINE from the top bar to start earning!",
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "incoming" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                    border = CardDefaults.outlinedCardBorder()
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("👤", fontSize = 24.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "New Ride Offer! 🔔",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 16.sp
                                                )
                                                Text(
                                                    text = "Customer: Chandan",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Text(
                                                text = "₹55",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 26.sp,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "To: Baharagora Bus Stand",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Distance: 1.2 km | Time: 4 mins walk", fontSize = 12.sp, color = Color.Gray)
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    driverJourneyState = "accepted"
                                                },
                                                modifier = Modifier.weight(1.2f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Accept Ride ✅", fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    driverJourneyState = "idle"
                                                },
                                                modifier = Modifier.weight(0.8f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                            ) {
                                                Text("Decline")
                                            }
                                        }
                                    }
                                }
                            }
                            "accepted" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = "On Active Ride 🚕",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "Pickup passenger and drop to Baharagora Bus Stand.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("👤", fontSize = 20.sp)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Customer: Chandan",
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Phone: +91 75459 23891",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            Text(
                                                text = "Fare: ₹55",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = {
                                                    driverJourneyState = "completed"
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Complete Trip", fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    // Simulate calling customer
                                                },
                                                modifier = Modifier.weight(0.8f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Call")
                                            }
                                        }
                                    }
                                }
                            }
                            "completed" -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(Color(0xFFE8F5E9), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🎉", fontSize = 36.sp)
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Trip Completed Successfully!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "You earned and collected ₹55 cash.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.height(20.dp))

                                        Button(
                                            onClick = {
                                                driverJourneyState = "idle"
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Go Offline / Wait for Next", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
