package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.data.model.Customer
import com.hamza.icleaner.ui.CustomersState
import com.hamza.icleaner.ui.CustomersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(navController: NavController, viewModel: CustomersViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Add Customer */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.loadCustomers(it)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Search customers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is CustomersState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is CustomersState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.customers) { customer ->
                                CustomerCard(customer)
                            }
                        }
                    }
                    is CustomersState.Error -> {
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
}

@Composable
fun CustomerCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = customer.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(text = customer.phone, color = MaterialTheme.colorScheme.primary)
            customer.email?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
