package com.taxclientmanager.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.ui.theme.*
import com.taxclientmanager.app.utils.CurrencyUtils
import com.taxclientmanager.app.viewmodel.ClientViewModel
import com.taxclientmanager.app.viewmodel.ClientWithStats
import com.taxclientmanager.app.viewmodel.SortOrder
import com.taxclientmanager.app.ui.components.SummaryMiniCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClientDetails: (String) -> Unit,
    onNavigateToAddClient: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToServiceList: () -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: ClientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Column {
                // Bottom Summary Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SummaryMiniCard(
                            icon = Icons.Default.Groups,
                            label = "Total Clients",
                            value = uiState.totalClients.toString(),
                            color = StatusReturn
                        )
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                        SummaryMiniCard(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Total Paid Amount",
                            value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalPaidAmount)}",
                            color = Color(0xFF10B981)
                        )
                    }
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToHome,
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Clients") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToServiceList,
                        icon = { Icon(Icons.Default.Description, contentDescription = null) },
                        label = { Text("Services") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToReports,
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text("Reports") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddClient,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Client")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)) {
            
            // Background curve effect
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.primary))
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.background
                ) {}
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    SearchAndSortSection(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChange,
                        sortOrder = uiState.sortOrder,
                        onSortOrderChange = viewModel::onSortOrderChange
                    )
                }

                item {
                    ClientStatsRow(uiState)
                }

                items(uiState.clients) { clientWithStats ->
                    var showContactSheet by remember { mutableStateOf(false) }
                    
                    ClientListItem(
                        clientWithStats = clientWithStats,
                        onClick = { onNavigateToClientDetails(clientWithStats.client.tinNumber) },
                        onContactClick = { showContactSheet = true }
                    )
                    
                    if (showContactSheet) {
                        ContactActionBottomSheet(
                            client = clientWithStats.client,
                            onDismiss = { showContactSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAndSortSection(
    query: String,
    onQueryChange: (String) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by TIN, Name, or Mobile", fontSize = 14.sp) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onPrimary
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        var expanded by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .height(56.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sort By", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Name (A-Z)") }, onClick = { onSortOrderChange(SortOrder.NAME_ASC); expanded = false })
                DropdownMenuItem(text = { Text("Name (Z-A)") }, onClick = { onSortOrderChange(SortOrder.NAME_DESC); expanded = false })
                DropdownMenuItem(text = { Text("Newest First") }, onClick = { onSortOrderChange(SortOrder.DATE_DESC); expanded = false })
                DropdownMenuItem(text = { Text("Oldest First") }, onClick = { onSortOrderChange(SortOrder.DATE_ASC); expanded = false })
            }
        }
    }
}

@Composable
fun ClientStatsRow(uiState: com.taxclientmanager.app.viewmodel.ClientListUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClientStatItem(label = "Total Clients", value = uiState.totalClients.toString(), icon = Icons.Default.Groups, color = StatusReturn, modifier = Modifier.weight(1f))
        ClientStatItem(label = "New This Year", value = uiState.newThisYear.toString(), icon = Icons.Default.PersonAdd, color = StatusNewTin, modifier = Modifier.weight(1f))
        ClientStatItem(label = "Active Clients", value = uiState.activeClients.toString(), icon = Icons.Default.VerifiedUser, color = StatusEdit, modifier = Modifier.weight(1f))
        ClientStatItem(label = "Inactive Clients", value = uiState.inactiveClients.toString(), icon = Icons.Default.PersonOff, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ClientStatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ClientListItem(clientWithStats: ClientWithStats, onClick: () -> Unit, onContactClick: () -> Unit) {
    val client = clientWithStats.client
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials
            val initials = client.fullName.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("")
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(getRandomPastelColor(initials), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                StatusBadge(isActive = client.isActive)
                Text(text = client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "TIN: ${client.tinNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onContactClick() }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = client.mobileNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Total Services: ${clientWithStats.totalServices}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(
                    text = "Total Paid: ${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(clientWithStats.totalPaid)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(onClick = onContactClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Contact Options", tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean) {
    Surface(
        color = if (isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) Color(0xFF059669) else Color(0xFFDC2626),
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactActionBottomSheet(
    client: Client,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = client.mobileNumber,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            
            BottomSheetItem(
                icon = Icons.Default.Phone,
                label = "Call",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${client.mobileNumber}")
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            )
            
            BottomSheetItem(
                icon = Icons.Default.Chat,
                label = "WhatsApp",
                onClick = {
                    val url = "https://wa.me/88${client.mobileNumber.replace("-", "").replace(" ", "")}"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(url)
                    }
                    context.startActivity(intent)
                    onDismiss()
                }
            )
            
            BottomSheetItem(
                icon = Icons.Default.ChatBubble,
                label = "IMO",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("imo://chat?phone=${client.mobileNumber}")
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // IMO not installed
                    }
                    onDismiss()
                }
            )
            
            BottomSheetItem(
                icon = Icons.Default.ContentCopy,
                label = "Copy Number",
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Client Phone", client.mobileNumber)
                    clipboard.setPrimaryClip(clip)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun BottomSheetItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}


fun getRandomPastelColor(seed: String): Color {
    val colors = listOf(
        Color(0xFF8B5CF6), Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B),
        Color(0xFFEC4899), Color(0xFF6366F1), Color(0xFF14B8A6), Color(0xFFF97316)
    )
    return colors[seed.hashCode().coerceAtLeast(0) % colors.size]
}

