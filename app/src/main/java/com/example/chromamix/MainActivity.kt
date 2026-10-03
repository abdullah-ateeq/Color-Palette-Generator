package com.example.chromamix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chromamix.data.repository.PalettePreferencesRepository
import com.example.chromamix.ui.navigation.AppNavigation
import com.example.chromamix.ui.theme.ChromaMixTheme
import com.example.chromamix.ui.viewmodel.ChromaMixViewModel
import com.example.chromamix.ui.viewmodel.ChromaMixViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = remember { PalettePreferencesRepository(applicationContext) }
            val viewModel: ChromaMixViewModel = viewModel(
                factory = ChromaMixViewModelFactory(repository)
            )
            val settings by viewModel.settings.collectAsState()

            ChromaMixTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}