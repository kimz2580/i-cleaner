package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.ui.OrdersState
import com.hamza.icleaner.ui.OrdersViewModel
import com.hamza.icleaner.ui.components.OrderCard
import com.hamza.icleaner.ui.components.RollingBasketLoading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    navController: NavController, 
    viewModel: OrdersViewModel = viewModel(),
    onOrderClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Orders", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is OrdersState.Loading -> {
                    RollingBasketLoading(modifier = Modifier.align(Alignment.Center))
                }
                is OrdersState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.orders) { order ->
                            OrderCard(order) {
                                onOrderClick(order.orderId)
                            }
                        }
                    }
                }
                is OrdersState.Error -> {
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
