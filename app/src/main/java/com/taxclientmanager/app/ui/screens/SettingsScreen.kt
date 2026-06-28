package com.taxclientmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxclientmanager.app.utils.BackupUtils
import com.taxclientmanager.app.viewmodel.AuthViewModel
import com.taxclientmanager.app.viewmodel.AuthState
import com.taxclientmanager.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val currencyCode by settingsViewModel.currencyCode.collectAsState()
    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val exportState by settingsViewModel.exportState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val success = BackupUtils.exportDatabase(context, outputStream)
                    scope.launch {
                        snackbarHostState.showSnackbar(if (success) "Backup successful" else "Backup failed")
                    }
                }
            }
        }
    )

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val success = BackupUtils.importDatabase(context, inputStream)
                    scope.launch {
                        snackbarHostState.showSnackbar(if (success) "Restore successful. Please restart the app." else "Restore failed")
                    }
                }
            }
        }
    )

    val exportClientsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    settingsViewModel.exportClients(outputStream, com.taxclientmanager.app.viewmodel.ExportFormat.CSV)
                }
            }
        }
    )

    val exportClientsExcelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    settingsViewModel.exportClients(outputStream, com.taxclientmanager.app.viewmodel.ExportFormat.EXCEL)
                }
            }
        }
    )

    val exportServicesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    settingsViewModel.exportServices(outputStream, com.taxclientmanager.app.viewmodel.ExportFormat.CSV)
                }
            }
        }
    )

    LaunchedEffect(exportState) {
        when (exportState) {
            is com.taxclientmanager.app.viewmodel.ExportState.Success -> {
                snackbarHostState.showSnackbar("Export successful")
                settingsViewModel.resetExportState()
            }
            is com.taxclientmanager.app.viewmodel.ExportState.Error -> {
                snackbarHostState.showSnackbar((exportState as com.taxclientmanager.app.viewmodel.ExportState.Error).message)
                settingsViewModel.resetExportState()
            }
            else -> {}
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            snackbarHostState.showSnackbar((authState as AuthState.Error).message)
            viewModel.resetState()
        } else if (authState is AuthState.Success) {
            snackbarHostState.showSnackbar("Operation successful")
            viewModel.resetState()
            showEditProfileDialog = false
            showChangePasswordDialog = false
        }
    }

    if (showEditProfileDialog) {
        val currentName = currentUser?.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: ""
        var name by remember { mutableStateOf(currentName) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.updateProfile(name) }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                        supportingText = {
                            if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                                Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword == confirmPassword) {
                            viewModel.changePassword(currentPassword, newPassword)
                        }
                    },
                    enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCurrencyDialog) {
        val currencies = listOf(
            "BDT" to "৳",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "INR" to "₹"
        )
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column {
                    currencies.forEach { (code, symbol) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.updateCurrency(code, symbol)
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "$code ($symbol)", modifier = Modifier.weight(1f))
                            if (currencyCode == code) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showBackupRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            title = { Text("Backup & Restore") },
            text = { Text("Backup your database to a file or restore it from a previously saved backup.") },
            confirmButton = {
                Button(onClick = {
                    showBackupRestoreDialog = false
                    backupLauncher.launch("tax_manager_backup_${System.currentTimeMillis()}.db")
                }) {
                    Text("Backup Now")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupRestoreDialog = false
                    restoreLauncher.launch(arrayOf("*/*"))
                }) {
                    Text("Restore")
                }
            }
        )
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Reports") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select report to export:")
                    Button(
                        onClick = {
                            showExportDialog = false
                            exportClientsLauncher.launch("clients_report_${System.currentTimeMillis()}.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Clients (CSV)")
                    }
                    Button(
                        onClick = {
                            showExportDialog = false
                            exportClientsExcelLauncher.launch("clients_report_${System.currentTimeMillis()}.xlsx")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Clients (Excel)")
                    }
                    Button(
                        onClick = {
                            showExportDialog = false
                            exportServicesLauncher.launch("services_report_${System.currentTimeMillis()}.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Export Services (CSV)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                UserProfileHeader(
                    name = currentUser?.userMetadata?.get("name")?.toString()?.replace("\"", "") ?: "User",
                    email = currentUser?.email ?: "",
                    onEditClick = { showEditProfileDialog = true }
                )
            }

            item {
                SettingsSectionTitle(title = "Account")
                SettingsItem(
                    icon = Icons.Default.Person, 
                    title = "Profile Information", 
                    subtitle = "View and edit your profile",
                    onClick = { showEditProfileDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.Lock, 
                    title = "Change Password", 
                    subtitle = "Update your security credentials",
                    onClick = { showChangePasswordDialog = true }
                )
            }

            item {
                SettingsSectionTitle(title = "App Settings")
                SettingsItem(
                    icon = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    title = if (isDarkMode) "Light Mode" else "Dark Mode",
                    subtitle = "Switch between light and dark themes",
                    onClick = onToggleTheme
                )
                SettingsItem(
                    icon = Icons.Default.Payments, 
                    title = "Currency Format", 
                    subtitle = "Default currency: $currencyCode ($currencySymbol)",
                    onClick = { showCurrencyDialog = true }
                )
            }

            item {
                SettingsSectionTitle(title = "Data Management")
                SettingsItem(
                    icon = Icons.Default.Backup, 
                    title = "Backup & Restore", 
                    subtitle = "Manage your database backups",
                    onClick = { showBackupRestoreDialog = true }
                )
                SettingsItem(
                    icon = Icons.Default.PictureAsPdf, 
                    title = "Export Reports", 
                    subtitle = "Export as PDF or Excel",
                    onClick = { showExportDialog = true }
                )
            }

            item {
                SettingsSectionTitle(title = "About")
                SettingsItem(
                    icon = Icons.Default.Info, 
                    title = "About App", 
                    subtitle = "Tax Client Manager v1.0.0",
                    onClick = onNavigateToAbout
                )
                SettingsItem(
                    icon = Icons.Default.Security, 
                    title = "Privacy Policy", 
                    subtitle = "How we protect your data",
                    onClick = onNavigateToPrivacy
                )
                SettingsItem(
                    icon = Icons.Default.Gavel, 
                    title = "Terms & Conditions", 
                    subtitle = "View terms and conditions",
                    onClick = onNavigateToTerms
                )
                SettingsItem(
                    icon = Icons.Default.Code, 
                    title = "Developer Info", 
                    subtitle = "Developed by Nurul Haque"
                )
                SettingsItem(
                    icon = Icons.Default.Email, 
                    title = "Contact & Support", 
                    subtitle = "support@taxclientmanager.com"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showLogoutConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (MaterialTheme.colorScheme.surface == Color.White) Color(0xFFFEE2E2) else Color(0xFF451F1F),
                        contentColor = if (MaterialTheme.colorScheme.surface == Color.White) Color(0xFFDC2626) else Color(0xFFFCA5A5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun UserProfileHeader(
    name: String,
    email: String,
    onEditClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onEditClick() }
            ) {
                Text(
                    "Edit Profile",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
