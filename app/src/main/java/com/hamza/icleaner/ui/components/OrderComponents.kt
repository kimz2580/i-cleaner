package com.hamza.icleaner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamza.icleaner.data.model.Order

@Composable
fun OrderCard(order: Order, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = order.customerName, style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(order.status)
                    Text(
                        text = "KSH ${order.finalAmount.toInt()}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Service", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(order.serviceType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Items", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(order.garmentCount.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payment", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(order.paymentMethod, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }

            if (order.pickupAddress.isNotEmpty() || order.deliveryAddress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null, 
                        modifier = Modifier.size(14.dp), 
                        tint = Color.Gray
                    )
                    Text(
                        text = order.pickupAddress.ifEmpty { order.deliveryAddress },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Pending" -> Color(0xFFF59E0B)
        "Processing" -> Color(0xFF06B6D4)
        "Ready" -> Color(0xFF10B981)
        "Completed" -> Color(0xFF4361EE)
        else -> Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
