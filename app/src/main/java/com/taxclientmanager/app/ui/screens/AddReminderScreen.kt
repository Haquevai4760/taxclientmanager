package com.taxclientmanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.taxclientmanager.app.data.model.ReminderCategory
import com.taxclientmanager.app.data.model.RepeatInterval
import com.taxclientmanager.app.viewmodel.ReminderViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    reminderId: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSelectClient: () -> Unit,
    navController: NavController,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val isEditMode = reminderId != null
    var title by remember { mutableStateOf("") }
    var selectedClientTin by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(ReminderCategory.RETURN_SUBMISSION) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedTime by remember { mutableStateOf("10:00 AM") }
    var selectedRepeat by remember { mutableStateOf(RepeatInterval.NONE) }
    var note by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }

    // এডিট মোডে ডেটা লোড করা
    LaunchedEffect(reminderId) {
        if (reminderId != null) {
            viewModel.getReminderById(reminderId) { reminder ->
                reminder?.let {
                    title = it.title
                    selectedClientTin = it.clientTin
                    selectedCategory = it.category
                    selectedDate = it.date
                    selectedTime = it.time
                    selectedRepeat = it.repeat
                    note = it.note
                    isCompleted = it.isCompleted
                }
            }
        }
    }

    // ক্লায়েন্ট সিলেক্ট করার পর রেজাল্ট হ্যান্ডেল করা
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val selectedTin = savedStateHandle?.getStateFlow<String?>("selectedClientTin", null)?.collectAsState()

    LaunchedEffect(selectedTin?.value) {
        selectedTin?.value?.let { tin ->
            selectedClientTin = tin
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Reminder" else "Add Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            if (isEditMode && reminderId != null) {
                                viewModel.updateReminder(
                                    id = reminderId,
                                    title = title,
                                    clientTin = selectedClientTin,
                                    category = selectedCategory,
                                    date = selectedDate,
                                    time = selectedTime,
                                    repeat = selectedRepeat,
                                    note = note,
                                    isCompleted = isCompleted
                                )
                            } else {
                                viewModel.addReminder(title, selectedClientTin, selectedCategory, selectedDate, selectedTime, selectedRepeat, note)
                            }
                            onNavigateBack()
                        }
                    }) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                placeholder = { Text("e.g., Return Submission Reminder") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ReminderFieldLabel("Client (Optional)")
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSelectClient() },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (selectedClientTin != null) "TIN: $selectedClientTin" else "Select Client",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedClientTin != null) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            ReminderFieldLabel("Category")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { 
                            Text(
                                category.name.replace("_", " ").lowercase()
                                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    ReminderFieldLabel("Reminder Date *")
                    OutlinedCard(
                        onClick = { /* Date Picker Implementation */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "20 Jul 2026") // Replace with formatted selectedDate
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    ReminderFieldLabel("Reminder Time *")
                    OutlinedCard(
                        onClick = { /* Time Picker Implementation */ },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = selectedTime)
                        }
                    }
                }
            }

            ReminderFieldLabel("Repeat")
            var repeatExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedCard(
                    onClick = { repeatExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedRepeat.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(expanded = repeatExpanded, onDismissRequest = { repeatExpanded = false }) {
                    RepeatInterval.values().forEach { interval ->
                        DropdownMenuItem(
                            text = { Text(interval.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                selectedRepeat = interval
                                repeatExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add any details...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )
            
            if (isEditMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mark as Completed", fontWeight = FontWeight.Medium)
                    Switch(checked = isCompleted, onCheckedChange = { isCompleted = it })
                }
            }
        }
    }
}

@Composable
fun ReminderFieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
