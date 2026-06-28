package com.taxclientmanager.app.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object ClientList : Screen("client_list")
    object AddClient : Screen("add_client?clientId={clientId}") {
        fun passClientId(clientId: String? = null) = "add_client?clientId=$clientId"
    }
    object ClientDetails : Screen("client_details/{clientId}") {
        fun passClientId(clientId: String) = "client_details/$clientId"
    }
    object AddService : Screen("add_service/{clientId}?serviceId={serviceId}") {
        fun passClientIdAndServiceId(clientId: String, serviceId: String? = null) =
            "add_service/$clientId?serviceId=$serviceId"
    }
    object ServiceList : Screen("service_list")
    object Reports : Screen("reports")
    object SelectClient : Screen("select_client")
    object Settings : Screen("settings")
    object Notifications : Screen("notifications")
    object AboutApp : Screen("about_app")
    object PrivacyPolicy : Screen("privacy_policy")
    object TermsConditions : Screen("terms_conditions")
    object Login : Screen("login?email={email}&password={password}") {
        fun passCredentials(email: String? = null, password: String? = null) =
            "login?email=$email&password=$password"
    }
    object Signup : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object AddReminder : Screen("add_reminder?reminderId={reminderId}") {
        fun passReminderId(reminderId: Int? = null) = "add_reminder?reminderId=$reminderId"
    }
}
