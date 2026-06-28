package com.taxclientmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.taxclientmanager.app.data.model.Client
import com.taxclientmanager.app.data.model.ServiceRecord
import com.taxclientmanager.app.data.model.ServiceType
import com.taxclientmanager.app.utils.CurrencyUtils
import com.taxclientmanager.app.utils.DateUtils
import com.taxclientmanager.app.viewmodel.ClientDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddService: (String) -> Unit,
    onEditService: (String, String) -> Unit,
    onEditClient: (String) -> Unit,
    viewModel: ClientDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var recordToDelete by remember { mutableStateOf<ServiceRecord?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(colorScheme.primary)) {
                TopAppBar(
                    title = { Text("Client Details", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            uiState.client?.let { onEditClient(it.tinNumber) }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Client")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primary,
                        titleContentColor = colorScheme.onPrimary,
                        navigationIconContentColor = colorScheme.onPrimary,
                        actionIconContentColor = colorScheme.onPrimary
                    )
                )

                // Curved header effect
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    drawArc(
                        color = colorScheme.background,
                        startAngle = 0f,
                        sweepAngle = -180f,
                        useCenter = true,
                        size = Size(size.width, size.height * 2),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        val client = uiState.client
        if (client == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ClientHeader(client = client)
                }

                item {
                    ClientInfoSection(client = client)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Service History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Services: ${uiState.serviceRecords.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Total Paid: ${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.serviceRecords.sumOf { it.serviceCharge })}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (uiState.serviceRecords.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                "No service records found",
                                modifier = Modifier.padding(24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(uiState.serviceRecords) { record ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            ServiceRecordItem(
                                record = record,
                                onEdit = { onEditService(client.tinNumber, record.id.toString()) },
                                onDelete = { recordToDelete = record }
                            )
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { onNavigateToAddService(client.tinNumber) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Service")
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Service Record") },
            text = { Text("Are you sure you want to delete this service record?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        recordToDelete?.let { viewModel.deleteServiceRecord(it) }
                        recordToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ClientHeader(client: Client) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val initials = client.fullName.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString("")
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = client.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "TIN: ${client.tinNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, size = 14.dp, tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = client.mobileNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun ClientInfoSection(client: Client) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(label = "Address", value = client.address)
            InfoRow(label = "Profession", value = client.profession)
            InfoRow(label = "Added On", value = DateUtils.formatDate(client.createdAt))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline, modifier = Modifier.width(100.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ServiceRecordItem(
    record: ServiceRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (initials, color) = when (record.serviceType) {
        ServiceType.RETURN_SUBMISSION -> "RS" to Color(0xFF8B5CF6)
        ServiceType.NEW_TIN_OPEN -> "NT" to Color(0xFF10B981)
        ServiceType.RE_RETURN_SUBMISSION -> "RR" to Color(0xFFF59E0B)
        ServiceType.TIN_EDIT -> "TE" to Color(0xFF3B82F6)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = initials, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.serviceType.displayName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = DateUtils.formatDate(record.serviceDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            if (record.incomeYear.isNotBlank()) {
                Text(text = record.incomeYear, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(record.serviceCharge)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", size = 16.dp, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", size = 16.dp, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp,
    tint: Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}
