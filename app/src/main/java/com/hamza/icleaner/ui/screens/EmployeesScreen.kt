package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.data.model.User
import com.hamza.icleaner.ui.EmployeesState
import com.hamza.icleaner.ui.EmployeesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(navController: NavController, viewModel: EmployeesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, username, phone ->
                viewModel.addEmployee(User(fullName = name, username = username, phone = phone, role = "employee")) {
                    showAddDialog = false
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff Management") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is EmployeesState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is EmployeesState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.employees) { employee ->
                            EmployeeCard(employee)
                        }
                    }
                }
                is EmployeesState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Staff") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && username.isNotBlank()) onConfirm(name, username, phone) },
                enabled = name.isNotBlank() && username.isNotBlank()
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmployeeCard(employee: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = employee.fullName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = employee.fullName, fontWeight = FontWeight.Bold)
                Text(text = employee.role.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
