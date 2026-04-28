package com.hamza.icleaner.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hamza.icleaner.data.model.Order
import com.hamza.icleaner.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    navController: NavController, 
    order: Order,
    viewModel: com.hamza.icleaner.ui.OrdersViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var currentStatus by remember { mutableStateOf(order.status) }
    var isUpdating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Details") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TrackingProgress(currentStatus = currentStatus)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = order.orderNumber, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        StatusBadge(currentStatus)
                    }
                    Text(text = order.customerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Service: ${order.serviceType}", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Created: ${order.createdAt}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            OrderSectionTitle("Status Update")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("Pending", "Processing", "Ready", "Delivered")
                statuses.forEach { status ->
                    val isSelected = currentStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (!isUpdating && currentStatus != status) {
                                isUpdating = true
                                viewModel.updateOrderStatus(order.orderId, status) { success ->
                                    if (success) currentStatus = status
                                    isUpdating = false
                                }
                            }
                        },
                        label = { Text(status) },
                        enabled = !isUpdating,
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            OrderSectionTitle("Items")
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    order.garments.forEach { (name, count) ->
                        if (count > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(name)
                                Text("x$count", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Items", fontWeight = FontWeight.Bold)
                        Text("${order.garmentCount}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            OrderSectionTitle("Addresses")
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddressItem(label = "Pickup", address = order.pickupAddress)
                    AddressItem(label = "Delivery", address = order.deliveryAddress)
                }
            }

            OrderSectionTitle("Payment")
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal")
                        Text("TSH ${"%,.2f".format(order.subtotal)}")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Method")
                        Text(order.paymentMethod, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Status")
                        Text(order.paymentStatus, color = if (order.paymentStatus == "Paid") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
                    }
                    if (order.paymentStatus != "Paid" && currentStatus != "Cancelled") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("payment/${order.orderId}/${order.finalAmount}") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Process Payment")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    isUpdating = true
                                    viewModel.cancelOrder(order.orderId) { success ->
                                        if (success) currentStatus = "Cancelled"
                                        isUpdating = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                            ) {
                                Text("Cancel Order")
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("TSH ${"%,.2f".format(order.finalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun AddressItem(label: String, address: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = address.ifEmpty { "Not provided" }, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TrackingProgress(currentStatus: String) {
    val statuses = listOf("Pending", "Processing", "Ready", "Delivered")
    val currentIndex = statuses.indexOf(currentStatus).coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            statuses.forEachIndexed { index, status ->
                val isCompleted = index <= currentIndex
                val color = if (isCompleted) MaterialTheme.colorScheme.primary else Color.LightGray

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (index < currentIndex) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(text = (index + 1).toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(text = status, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp), color = if (isCompleted) Color.Black else Color.Gray)
                }

                if (index < statuses.size - 1) {
                    val lineColor = if (index < currentIndex) MaterialTheme.colorScheme.primary else Color.LightGray
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(lineColor)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
