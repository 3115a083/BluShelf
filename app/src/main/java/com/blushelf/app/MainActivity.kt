package com.blushelf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shelves
import androidx.compose.material.icons.outlined.Swipe
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BluShelfApp() }
    }
}

private enum class Destination { Shelf, Swipe, Settings }

@Composable
private fun BluShelfApp() {
    var destination by rememberSaveable { mutableStateOf(Destination.Shelf) }
    MaterialTheme {
        Scaffold(bottomBar = {
            NavigationBar {
                NavigationBarItem(destination == Destination.Shelf, { destination = Destination.Shelf }, { Icon(Icons.Outlined.Shelves, null) }, label = { Text("Regal") })
                NavigationBarItem(destination == Destination.Swipe, { destination = Destination.Swipe }, { Icon(Icons.Outlined.Swipe, null) }, label = { Text("Swipe") })
                NavigationBarItem(destination == Destination.Settings, { destination = Destination.Settings }, { Icon(Icons.Outlined.Settings, null) }, label = { Text("Einstellungen") })
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(when (destination) {
                    Destination.Shelf -> "Dein Regal ist leer"
                    Destination.Swipe -> "Swipe"
                    Destination.Settings -> "Einstellungen"
                }, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
