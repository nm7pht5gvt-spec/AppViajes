package com.tuapp.tripadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuapp.tripadvisor.data.preferences.PreferencesRepository
import com.tuapp.tripadvisor.ui.config.ConfigScreen
import com.tuapp.tripadvisor.ui.config.ConfigViewModel
import com.tuapp.tripadvisor.ui.config.ConfigViewModelFactory
import com.tuapp.tripadvisor.ui.theme.TripAdvisorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = PreferencesRepository(applicationContext)

        setContent {
            TripAdvisorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val configViewModel: ConfigViewModel = viewModel(
                        factory = ConfigViewModelFactory(repository)
                    )
                    ConfigScreen(
                        viewModel = configViewModel,
                        onServiceActivated = { }
                    )
                }
            }
        }
    }
}
