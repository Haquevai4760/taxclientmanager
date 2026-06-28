package com.taxclientmanager.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taxclientmanager.app.ui.screens.*

@Composable
fun NavGraph(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    startDestination: String = Screen.Login.route
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Login.route,
            arguments = listOf(
                navArgument("email") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("password") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            val password = backStackEntry.arguments?.getString("password")
            LoginScreen(
                initialEmail = email ?: "",
                initialPassword = password ?: "",
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate(Screen.Signup.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToLogin = { email: String, password: String ->
                    navController.navigate(Screen.Login.passCredentials(email, password)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignupSuccess = { email: String, password: String ->
                    navController.navigate(Screen.Login.passCredentials(email, password)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ResetPassword.route,
            deepLinks = listOf(
                androidx.navigation.navDeepLink {
                    uriPattern = "taxclientmanager://reset-password"
                }
            )
        ) {
            ResetPasswordScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToClientList = { navController.navigate(Screen.ClientList.route) },
                onNavigateToServiceList = { navController.navigate(Screen.ServiceList.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToAddClient = { navController.navigate(Screen.AddClient.route) },
                onNavigateToAddService = { navController.navigate(Screen.SelectClient.route) },
                onNavigateToClientDetails = { tinNumber -> 
                    navController.navigate(Screen.ClientDetails.passClientId(tinNumber))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onToggleTheme = onToggleTheme,
                isDarkMode = isDarkMode
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                onNavigateToPrivacy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onNavigateToTerms = { navController.navigate(Screen.TermsConditions.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme
            )
        }
        composable(Screen.Notifications.route) {
            NotificationListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddReminder = { navController.navigate(Screen.AddReminder.route) }
            )
        }
        composable(Screen.AboutApp.route) {
            AboutAppScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.PrivacyPolicy.route) {
            // You can create a similar screen for Privacy Policy
            // For now, let's just use a simple placeholder or reuse AboutApp logic
            AboutAppScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.TermsConditions.route) {
            // You can create a similar screen for Terms
            AboutAppScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AddReminder.route,
            arguments = listOf(navArgument("reminderId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getString("reminderId")?.toIntOrNull()
            AddReminderScreen(
                reminderId = reminderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectClient = { navController.navigate(Screen.SelectClient.route) },
                navController = navController
            )
        }
        composable(Screen.SelectClient.route) {
            SelectClientScreen(
                onNavigateBack = { navController.popBackStack() },
                onClientSelected = { clientId ->
                    val previousRoute = navController.previousBackStackEntry?.destination?.route
                    if (previousRoute?.contains("add_reminder") == true) {
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedClientTin", clientId)
                        navController.popBackStack()
                    } else {
                        navController.navigate(Screen.AddService.passClientIdAndServiceId(clientId)) {
                            popUpTo(Screen.SelectClient.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.ClientList.route) {
            ClientListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToClientDetails = { clientId -> 
                    navController.navigate(Screen.ClientDetails.passClientId(clientId)) 
                },
                onNavigateToAddClient = {
                    navController.navigate(Screen.AddClient.passClientId())
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToServiceList = { navController.navigate(Screen.ServiceList.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) }
            )
        }
        composable(Screen.ServiceList.route) {
            ServiceListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToClientDetails = { tinNumber ->
                    navController.navigate(Screen.ClientDetails.passClientId(tinNumber))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToClientList = { navController.navigate(Screen.ClientList.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToAddService = { navController.navigate(Screen.SelectClient.route) }
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToClientList = { navController.navigate(Screen.ClientList.route) },
                onNavigateToServiceList = { navController.navigate(Screen.ServiceList.route) }
            )
        }
        composable(
            route = Screen.AddClient.route,
            arguments = listOf(navArgument("clientId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            AddClientScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.ClientDetails.route,
            arguments = listOf(navArgument("clientId") { type = NavType.StringType })
        ) {
            ClientDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddService = { clientId ->
                    navController.navigate(Screen.AddService.passClientIdAndServiceId(clientId))
                },
                onEditService = { clientId, serviceId ->
                    navController.navigate(Screen.AddService.passClientIdAndServiceId(clientId, serviceId))
                },
                onEditClient = { clientId ->
                    navController.navigate(Screen.AddClient.passClientId(clientId))
                }
            )
        }
        composable(
            route = Screen.AddService.route,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
                navArgument("serviceId") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            AddServiceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

