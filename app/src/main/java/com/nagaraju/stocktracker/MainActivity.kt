package com.nagaraju.stocktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nagaraju.stocktracker.core.ui.theme.StockTrackerTheme
import com.nagaraju.stocktracker.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity hosting the entire Compose UI tree. [AndroidEntryPoint]
 * allows Hilt to inject into this activity and, transitively, into every
 * [androidx.lifecycle.ViewModel] created within its Compose hierarchy via
 * `hiltViewModel()`.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StockTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}
