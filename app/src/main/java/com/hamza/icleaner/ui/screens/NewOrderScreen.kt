package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.ui.NewOrderState
import com.hamza.icleaner.ui.NewOrderViewModel
import java.util.Locale

data class GarmentType(val name: String, val icon: String, val basePrice: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(navController: NavController, viewModel: NewOrderViewModel = viewModel()) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf("Wash & Fold") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    
    val garmentTypes = listOf(
        GarmentType("Shirt", "👕", 2000),
        GarmentType("Trouser", "👖", 2500),
        GarmentType("Dress", "👗", 3000),
        GarmentType("Jacket", "🧥", 4000),
        GarmentType("Suit", "🤵", 5000),
        GarmentType("Bed Sheet", "🛏️", 3500)
    )
    
    val garmentQuantities = remember { mutableStateMapOf<String, Int>().apply { 
        garmentTypes.forEach { put(it.name, 0) }
    }}

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is NewOrderState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Order") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState is NewOrderState.Error) {
                    Text(
                        text = (uiState as NewOrderState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                SectionTitle("👤 Customer Information")
                
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )

                SectionTitle("🧺 Select Service")
                ServiceSelector(
                    selectedService = serviceType,
                    onServiceSelected = { serviceType = it }
                )

                SectionTitle("👕 Garment Details")
                garmentTypes.forEach { garment ->
                    GarmentItemRow(
                        garment = garment,
                        quantity = garmentQuantities[garment.name] ?: 0,
                        onQuantityChange = { garmentQuantities[garment.name] = it }
                    )
                }

                SectionTitle("📍 Pickup & Delivery")
                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Pickup Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                SectionTitle("💰 Payment Method")
                PaymentSelector(
                    selectedMethod = paymentMethod,
                    onMethodSelected = { paymentMethod = it }
                )

                PriceBreakdown(
                    subtotal = garmentTypes.sumOf { (garmentQuantities[it.name] ?: 0) * it.basePrice },
                    serviceType = serviceType
                )

                Button(
                    onClick = { 
                        val subtotal = garmentTypes.sumOf { (garmentQuantities[it.name] ?: 0) * it.basePrice }
                        val expressFee = if (serviceType == "Express") (subtotal * 0.5).toInt() else 0
                        val total = subtotal + expressFee

                        viewModel.createOrder(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            serviceType = serviceType,
                            garmentCount = garmentQuantities.values.sum(),
                            garments = garmentQuantities.toMap(),
                            subtotal = subtotal.toDouble(),
                            totalAmount = total.toDouble(),
                            paymentMethod = paymentMethod,
                            pickupAddress = pickupAddress,
                            deliveryAddress = deliveryAddress
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = uiState !is NewOrderState.Loading && garmentQuantities.values.sum() > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is NewOrderState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Place Order →", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun ServiceSelector(selectedService: String, onServiceSelected: (String) -> Unit) {
    val services = listOf("Wash & Fold", "Wash & Iron", "Dry Clean", "Iron Only", "Express")
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        services.forEach { service ->
            val isSelected = selectedService == service
            Surface(
                modifier = Modifier.clickable { onServiceSelected(service) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = service,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun GarmentItemRow(garment: GarmentType, quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${garment.icon} ${garment.name}", modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(
                text = quantity.toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onQuantityChange(quantity + 1) },
                modifier = Modifier.size(32.dp).background(Color.White, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PaymentSelector(selectedMethod: String, onMethodSelected: (String) -> Unit) {
    val methods = listOf("Cash", "M-Pesa", "Airtel", "Card")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        methods.forEach { method ->
            val isSelected = selectedMethod == method
            Surface(
                modifier = Modifier.weight(1f).clickable { onMethodSelected(method) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = method,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PriceBreakdown(
    subtotal: Int,
    serviceType: String
) {
    val expressFee = if (serviceType == "Express") (subtotal * 0.5).toInt() else 0
    val total = subtotal + expressFee

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Subtotal", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                Text("TZS ${subtotal.toLocaleString()}", color = Color.White)
            }
            if (expressFee > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Express Fee (+50%)", color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                    Text("TZS ${expressFee.toLocaleString()}", color = Color.White)
                }
            }
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text("TZS ${total.toLocaleString()}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
        }
    }
}

fun Int.toLocaleString(): String {
    return String.format(Locale.getDefault(), "%, d", this).trim()
}