package com.hulampay.mobile.ui.activitys

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.hulampay.mobile.data.preferences.ThemePreferences
import com.hulampay.mobile.navigation.NavGraph
import com.hulampay.mobile.ui.theme.LocalThemePreference
import com.hulampay.mobile.ui.theme.ThemePreference
import com.hulampay.mobile.ui.theme.UniLostTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        enableEdgeToEdge()

        setContent {
            val themePreference = remember { mutableStateOf(ThemePreference.SYSTEM) }

            // Hydrate from DataStore once, then persist any subsequent change made via Settings.
            LaunchedEffect(Unit) {
                themePreference.value = themePreferences.theme.first()
                snapshotFlow { themePreference.value }
                    .drop(1)
                    .collect { themePreferences.setTheme(it) }
            }

            CompositionLocalProvider(LocalThemePreference provides themePreference) {
                UniLostTheme {
                    val navController = rememberNavController()
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
