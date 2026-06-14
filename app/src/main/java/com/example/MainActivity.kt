package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.MyApplicationTheme

import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.LocationPermissionGate
import com.example.ui.screens.DriverRegistrationScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        LocationPermissionGate {
          val navController = rememberNavController()
          val context = this
          val sharedPrefs = context.getSharedPreferences("baharagora_travels_pref", Context.MODE_PRIVATE)

          Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
              navController = navController,
              startDestination = "auth",
              modifier = Modifier.padding(innerPadding)
            ) {
              composable("auth") {
                AuthScreen(onAuthSuccess = {
                  navController.navigate("role_selection") {
                    popUpTo("auth") { inclusive = true }
                  }
                })
              }
              composable("role_selection") {
                RoleSelectionScreen(onRoleSelected = { role ->
                  if (role == "driver") {
                    val isRegistered = sharedPrefs.getBoolean("driver_registered", false)
                    if (isRegistered) {
                      navController.navigate("map_screen/driver")
                    } else {
                      navController.navigate("driver_registration")
                    }
                  } else {
                    navController.navigate("map_screen/passenger")
                  }
                })
              }
              composable("driver_registration") {
                DriverRegistrationScreen(
                  onRegistrationSuccess = {
                    navController.navigate("map_screen/driver") {
                      popUpTo("role_selection")
                    }
                  },
                  onBackClick = {
                    navController.popBackStack()
                  }
                )
              }
              composable(
                route = "map_screen/{role}",
                arguments = listOf(navArgument("role") { type = NavType.StringType })
              ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "passenger"
                MapScreen(
                  role = role,
                  onBackClick = { navController.popBackStack() }
                )
              }
            }
          }
        }
      }
    }
  }
}
