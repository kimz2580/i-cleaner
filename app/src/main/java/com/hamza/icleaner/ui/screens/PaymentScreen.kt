package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.ui.PaymentState
import com.hamza.icleaner.ui.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    navController: NavController,
    orderId: String,
    amount: Double,
    viewModel: PaymentViewModel = viewModel()
) {
    var selectedMethod by remember { mutableStateOf("Cash") }
    val paymentMethods = listOf("Cash", "M-Pesa", "Airtel Money", "Tigo Pesa", "Bank Card")
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is PaymentState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Process Payment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (uiState is PaymentState.Error) {
                Text(
                    text = (uiState as PaymentState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Amount Due", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "TZS ${amount.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(text = "Order ID: $orderId", style = MaterialTheme.typography.bodySmall)
                }
            }

            Text(text = "Select Payment Method", fontWeight = FontWeight.Bold)

            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                    Text(text = method, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.processPayment(orderId, amount, selectedMethod)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PaymentState.Loading,
                shape = MaterialTheme.shapes.medium
            ) {
                if (uiState is PaymentState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirm Payment", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}
