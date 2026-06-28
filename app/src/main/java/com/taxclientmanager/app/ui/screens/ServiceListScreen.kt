package com.taxclientmanager.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxclientmanager.app.data.model.ServiceType
import com.taxclientmanager.app.ui.theme.*
import com.taxclientmanager.app.utils.CurrencyUtils
import com.taxclientmanager.app.utils.DateUtils
import com.taxclientmanager.app.viewmodel.ServiceSortOrder
import com.taxclientmanager.app.viewmodel.ServiceViewModel
import com.taxclientmanager.app.viewmodel.ServiceWithClientInfo
import com.taxclientmanager.app.ui.components.SummaryMiniCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClientDetails: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToClientList: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAddService: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
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
                            icon = Icons.Default.Payments,
                            label = "Total Revenue",
                            value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalRevenue)}",
                            color = StatusNewTin
                        )
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                        SummaryMiniCard(
                            icon = Icons.Default.PendingActions,
                            label = "Pending Amount",
                            value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalPending)}",
                            color = StatusReReturn
                        )
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp))
                        SummaryMiniCard(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Total Collected",
                            value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalPaid)}",
                            color = StatusReturn
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
                        selected = false,
                        onClick = onNavigateToClientList,
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Clients") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
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
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddService,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Service") }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)) {
            
            // Background curve effect
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(MaterialTheme.colorScheme.primary))
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = MaterialTheme.colorScheme.background
                ) {}
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        ServiceTypeFilterRow(
                            selectedType = uiState.selectedServiceType,
                            onTypeSelected = viewModel::onServiceTypeChange
                        )
                    }

                    item {
                        ServiceStatsRow(uiState)
                    }

                    item {
                        ServiceSearchAndSortSection(
                            query = uiState.searchQuery,
                            onQueryChange = viewModel::onSearchQueryChange,
                            sortOrder = uiState.sortOrder,
                            onSortOrderChange = viewModel::onSortOrderChange
                        )
                    }

                    items(uiState.services) { serviceWithInfo ->
                        ServiceListItemRedesign(
                            serviceWithInfo = serviceWithInfo,
                            onClick = { onNavigateToClientDetails(serviceWithInfo.service.tinNumber) }
                        )
                    }
                    
                    item {
                        PaginationRow(uiState.services.size, uiState.totalServices)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceTypeFilterRow(
    selectedType: ServiceType?,
    onTypeSelected: (ServiceType?) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null
            )
        }
        items(ServiceType.entries.toTypedArray()) { type ->
            val color = when (type) {
                ServiceType.NEW_TIN_OPEN -> StatusNewTin
                ServiceType.TIN_EDIT -> StatusEdit
                ServiceType.RETURN_SUBMISSION -> StatusReturn
                ServiceType.RE_RETURN_SUBMISSION -> StatusReReturn
            }
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(type.displayName)
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null
            )
        }
    }
}

@Composable
fun ServiceSearchAndSortSection(
    query: String,
    onQueryChange: (String) -> Unit,
    sortOrder: ServiceSortOrder,
    onSortOrderChange: (ServiceSortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search by TIN, Name, or Mobile", fontSize = 14.sp) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        
        var expanded by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sort By", style = MaterialTheme.typography.labelLarge)
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Date (Newest)") }, onClick = { onSortOrderChange(ServiceSortOrder.DATE_DESC); expanded = false })
                DropdownMenuItem(text = { Text("Date (Oldest)") }, onClick = { onSortOrderChange(ServiceSortOrder.DATE_ASC); expanded = false })
                DropdownMenuItem(text = { Text("Amount (High-Low)") }, onClick = { onSortOrderChange(ServiceSortOrder.AMOUNT_DESC); expanded = false })
                DropdownMenuItem(text = { Text("Amount (Low-High)") }, onClick = { onSortOrderChange(ServiceSortOrder.AMOUNT_ASC); expanded = false })
                DropdownMenuItem(text = { Text("Client Name (A-Z)") }, onClick = { onSortOrderChange(ServiceSortOrder.CLIENT_NAME_ASC); expanded = false })
            }
        }
    }
}

@Composable
fun ServiceStatsRow(uiState: com.taxclientmanager.app.viewmodel.ServiceListUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceStatItem(label = "Total Services", value = uiState.totalServices.toString(), subLabel = "This Year", icon = Icons.Default.Description, color = StatusReturn, modifier = Modifier.weight(1f))
            ServiceStatItem(label = "Total Revenue", value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalRevenue)}", subLabel = "This Year", icon = Icons.Default.Payments, color = StatusNewTin, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceStatItem(label = "Pending Amount", value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.totalPending)}", subLabel = "From ${uiState.pendingClientsCount} Clients", icon = Icons.Default.PendingActions, color = StatusReReturn, modifier = Modifier.weight(1f))
            ServiceStatItem(label = "Today's Services", value = uiState.todaysServicesCount.toString(), subLabel = "Today", icon = Icons.Default.CalendarToday, color = StatusEdit, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ServiceStatItem(label: String, value: String, subLabel: String, icon: ImageVector, color: Color, modifier: Modifier) {
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
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(6.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subLabel, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun ServiceListItemRedesign(serviceWithInfo: ServiceWithClientInfo, onClick: () -> Unit) {
    val service = serviceWithInfo.service
    val color = when (service.serviceType) {
        ServiceType.RETURN_SUBMISSION -> StatusReturn
        ServiceType.NEW_TIN_OPEN -> StatusNewTin
        ServiceType.RE_RETURN_SUBMISSION -> StatusReReturn
        ServiceType.TIN_EDIT -> StatusEdit
    }
    
    val initials = serviceWithInfo.clientName.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = initials, fontWeight = FontWeight.Bold, color = Color.White)
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Column 2: Name and TIN
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(text = serviceWithInfo.clientName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "TIN: ${service.tinNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = service.serviceType.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                // Column 3: Income Year and Service Date
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                    Text("Income Year", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                    Text(service.incomeYear, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Service Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(DateUtils.formatDate(service.serviceDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    }
                }

                // Column 4: Amount
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                    Text(
                        text = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(service.serviceCharge)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
            HorizontalDivider(modifier = Modifier.padding(start = 60.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        }
    }
}


@Composable
fun PaginationRow(showing: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Showing 1 to $showing of $total services", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            PageButton("1", true)
            PageButton("2", false)
            PageButton("3", false)
            Text("...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            PageButton("54", false)
            IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun PageButton(text: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = Modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}
