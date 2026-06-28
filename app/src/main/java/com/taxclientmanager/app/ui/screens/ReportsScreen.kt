package com.taxclientmanager.app.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taxclientmanager.app.data.model.ServiceType
import com.taxclientmanager.app.ui.theme.*
import com.taxclientmanager.app.utils.CurrencyUtils
import com.taxclientmanager.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToClientList: () -> Unit,
    onNavigateToServiceList: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Open Drawer or Menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
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
                    icon = { Icon(Icons.Default.PeopleOutline, contentDescription = null) },
                    label = { Text("Clients") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToServiceList,
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    label = { Text("Services") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Reports") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selectors and Download Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1.3f)) {
                        ReportSelector(label = "Income Year", value = uiState.selectedYear, icon = Icons.Default.CalendarToday)
                    }
                    Box(modifier = Modifier.weight(1.5f)) {
                        ReportSelector(label = "Date Range", value = "01 Jul 2026 - 30 Jun 2027", icon = Icons.Default.DateRange)
                    }
                    Button(
                        onClick = { /* Download */ },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(
                        title = "Total Clients",
                        value = uiState.totalTaxpayers.toString(),
                        subtitle = "All Time",
                        icon = Icons.Default.Groups,
                        iconColor = StatusReturn,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Total Services",
                        value = uiState.totalServices.toString(),
                        subtitle = "This Year",
                        icon = Icons.Default.Description,
                        iconColor = StatusEdit,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard(
                        title = "Total Revenue",
                        value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.thisYearRevenue)}",
                        subtitle = "This Year",
                        icon = Icons.Default.Paid,
                        iconColor = StatusNewTin,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Avg. Service Charge",
                        value = "${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.avgServiceCharge)}",
                        subtitle = "This Year",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = StatusReReturn,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Revenue Overview Chart
            item {
                RevenueOverviewCard(uiState)
            }

            // Services Breakdown & Top Services Row
            item {
                IntrinsicSizeCardRow(uiState)
            }

            // Summary by Income Year Table
            item {
                YearlySummaryTable(uiState.yearlySummaries)
            }

            // Bottom Action Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportActionCard("Clients Report", Icons.Default.Groups, StatusReturn, Modifier.weight(1f).fillMaxHeight())
                        ReportActionCard("Services Report", Icons.Default.Assignment, StatusNewTin, Modifier.weight(1f).fillMaxHeight())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ReportActionCard("Revenue Report", Icons.Default.Payments, StatusEdit, Modifier.weight(1f).fillMaxHeight())
                        ReportActionCard("Pending Report", Icons.Default.AccountBalanceWallet, StatusReReturn, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ReportSelector(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun ReportStatCard(title: String, value: String, subtitle: String, icon: ImageVector, iconColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, fontSize = 9.sp)
            }
        }
    }
}

@Composable
fun RevenueOverviewCard(uiState: com.taxclientmanager.app.viewmodel.DashboardUiState) {
    val chartColor = MaterialTheme.colorScheme.primary
    val monthlyData = uiState.monthlyRevenue
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Revenue Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Revenue", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    Text("${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(uiState.thisYearRevenue)}", fontWeight = FontWeight.Bold, color = chartColor, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            if (monthlyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    Text("No data available for this year", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                }
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val path = Path()
                    val maxRevenue = monthlyData.maxOfOrNull { it.revenue }?.coerceAtLeast(1.0) ?: 1.0
                    val width = size.width
                    val height = size.height
                    val stepX = width / (monthlyData.size - 1).coerceAtLeast(1)
                    
                    monthlyData.forEachIndexed { index, data ->
                        val x = index * stepX
                        val normalizedValue = (data.revenue / maxRevenue).toFloat()
                        val y = height * (1 - normalizedValue)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    
                    drawPath(path, chartColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    
                    // Fill Area
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(fillPath, Brush.verticalGradient(listOf(chartColor.copy(alpha = 0.2f), Color.Transparent)))
                    
                    // Dots
                    monthlyData.forEachIndexed { index, data ->
                        val normalizedValue = (data.revenue / maxRevenue).toFloat()
                        drawCircle(chartColor, radius = 4.dp.toPx(), center = Offset(index * stepX, height * (1 - normalizedValue)))
                        drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(index * stepX, height * (1 - normalizedValue)))
                    }
                }
            }
            
            // X-Axis Labels (Months from data)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                monthlyData.forEach {
                    val monthIdx = it.month.toIntOrNull()?.minus(1) ?: 0
                    val name = monthNames.getOrElse(monthIdx) { "" }
                    Text(name, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                }
                if (monthlyData.isEmpty()) {
                    listOf("Jul", "Sep", "Nov", "Jan", "Mar", "May").forEach {
                        Text(it, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun IntrinsicSizeCardRow(uiState: com.taxclientmanager.app.viewmodel.DashboardUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            ServicesBreakdownCard(uiState)
        }
        Box(modifier = Modifier.weight(0.8f).fillMaxHeight()) {
            TopServicesCard(uiState)
        }
    }
}

@Composable
fun ServicesBreakdownCard(uiState: com.taxclientmanager.app.viewmodel.DashboardUiState) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Services Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(20.dp))
            
            val total = uiState.totalServices
            Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val strokeWidth = 15.dp.toPx()
                    if (total == 0) {
                        drawCircle(Color.LightGray.copy(alpha = 0.3f), style = Stroke(strokeWidth))
                    } else {
                        var startAngle = -90f
                        val segments = listOf(
                            uiState.returnSubmitted to StatusReturn,
                            uiState.newTinOpened to StatusNewTin,
                            uiState.tinEditCount to StatusEdit,
                            uiState.reReturnSubmitted to StatusReReturn
                        )
                        
                        segments.forEach { (count, color) ->
                            val sweep = (count.toFloat() / total) * 360f
                            if (sweep > 0) {
                                drawArc(color, startAngle, sweep, false, style = Stroke(strokeWidth), size = Size(size.width, size.height))
                                startAngle += sweep
                            }
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(total.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Total", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BreakdownLegend("Return Submission", uiState.returnSubmitted, StatusReturn, total)
                BreakdownLegend("New TIN Open", uiState.newTinOpened, StatusNewTin, total)
                BreakdownLegend("TIN Edit", uiState.tinEditCount, StatusEdit, total)
                BreakdownLegend("Re-Return Submission", uiState.reReturnSubmitted, StatusReReturn, total)
            }
        }
    }
}

@Composable
fun BreakdownLegend(label: String, count: Int, color: Color, total: Int) {
    val percentage = if (total > 0) (count * 100 / total) else 0
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
        Text("$count ($percentage%)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun TopServicesCard(uiState: com.taxclientmanager.app.viewmodel.DashboardUiState) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top Services", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(20.dp))
            
            val services = listOf(
                "Return Submission" to uiState.returnSubmitted to StatusReturn,
                "Re-Return Submission" to uiState.reReturnSubmitted to StatusReReturn,
                "TIN Edit" to uiState.tinEditCount to StatusEdit,
                "New TIN Open" to uiState.newTinOpened to StatusNewTin
            ).sortedByDescending { it.first.second }

            services.forEachIndexed { index, data ->
                TopServiceItem("${index + 1}.", data.first.first, data.first.second, data.second, uiState.totalServices)
            }
        }
    }
}

@Composable
fun TopServiceItem(index: String, name: String, count: Int, color: Color, total: Int) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$index $name", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(count.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun YearlySummaryTable(summaries: List<com.taxclientmanager.app.data.dao.YearlySummary>) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Summary by Income Year", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(500.dp)) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Income Year", modifier = Modifier.weight(1.2f), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                        Text("Total Clients", modifier = Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Total Services", modifier = Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Total Revenue", modifier = Modifier.weight(1.3f), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("Pending", modifier = Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }
                    
                    summaries.forEach { summary ->
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(summary.incomeYear, modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(summary.totalClients.toString(), modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                            Text(summary.totalServices.toString(), modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                            Text("${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(summary.totalRevenue)}", modifier = Modifier.weight(1.3f), fontSize = 11.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text("${CurrencyUtils.getCurrencySymbol()} ${CurrencyUtils.formatCurrency(summary.totalPending)}", modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.End, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun ReportActionCard(label: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier.clickable { },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("View all report", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        }
    }
}
