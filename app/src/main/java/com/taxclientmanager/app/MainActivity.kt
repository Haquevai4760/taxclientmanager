package com.taxclientmanager.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taxclientmanager.app.navigation.NavGraph
import com.taxclientmanager.app.navigation.Screen
import com.taxclientmanager.app.ui.theme.TaxClientManagerTheme
import com.taxclientmanager.app.viewmodel.AuthViewModel
import com.taxclientmanager.app.viewmodel.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Handle deep link when app is created
        intent?.let { supabaseClient.handleDeeplinks(it) }
        
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val currentUser by authViewModel.currentUser.collectAsState()
            
            val startDestination = remember(currentUser) {
                if (currentUser != null) Screen.Dashboard.route else Screen.Login.route
            }
            
            TaxClientManagerTheme(darkTheme = isDarkMode) {
                NavGraph(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { themeViewModel.toggleTheme() },
                    startDestination = startDestination
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when app is already in background
        supabaseClient.handleDeeplinks(intent)
    }
}
