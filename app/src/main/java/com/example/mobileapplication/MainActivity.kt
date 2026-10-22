package com.example.mobileapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobileapplication.ui.navigation.BristolCityCompanionApp
import com.example.mobileapplication.ui.theme.MobileApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileApplicationTheme {
                BristolCityCompanionApp()
            }
        }
    }
}
