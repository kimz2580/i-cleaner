package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.hamza.icleaner.data.api.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class NewOrderState {
    object Idle : NewOrderState()
    object Loading : NewOrderState()
    data class Success(val orderNumber: String) : NewOrderState()
    data class Error(val message: String) : NewOrderState()
}

class NewOrderViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<NewOrderState>(NewOrderState.Idle)
    val uiState: StateFlow<NewOrderState> = _uiState.asStateFlow()

    fun createOrder(
        customerName: String,
        customerPhone: String,
        serviceType: String,
        garmentCount: Int,
        garments: Map<String, Int> = emptyMap(),
        subtotal: Double = 0.0,
        totalAmount: Double = 0.0,
        paymentMethod: String = "Cash",
        pickupAddress: String = "",
        deliveryAddress: String = "",
        specialInstructions: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = NewOrderState.Loading
            try {
                val currentUser = auth.currentUser
                val orderId = db.collection("orders").document().id
                // Use customer name and a shorter suffix for the order number
                val namePart = customerName.filter { it.isLetter() }.take(4).uppercase()
                val orderNumber = "$namePart-${System.currentTimeMillis().toString().takeLast(4)}"
                
                val orderData = mapOf(
                    "userId" to (currentUser?.uid ?: ""),
                    "orderId" to orderId,
                    "orderNumber" to orderNumber,
                    "customerName" to customerName,
                    "customerPhone" to customerPhone,
                    "serviceType" to serviceType,
                    "garmentCount" to garmentCount,
                    "garments" to garments,
                    "subtotal" to subtotal.toDouble(),
                    "totalAmount" to totalAmount.toDouble(),
                    "finalAmount" to totalAmount.toDouble(),
                    "paymentMethod" to paymentMethod,
                    "pickupAddress" to pickupAddress,
                    "deliveryAddress" to deliveryAddress,
                    "specialInstructions" to specialInstructions,
                    "status" to "Pending",
                    "paymentStatus" to "Unpaid",
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                )
                
                // 1. Save to Firestore for real-time UI updates
                db.collection("orders").document(orderId).set(orderData).await()

                // 2. Optional: Sync with Retrofit/PHP backend if needed
                try {
                    RetrofitClient.apiService.createOrder(orderData)
                } catch (e: Exception) {
                    // Log error but don't fail since Firestore succeeded
                }

                _uiState.value = NewOrderState.Success(orderNumber)
            } catch (e: Exception) {
                _uiState.value = NewOrderState.Error("Failed to create order: ${e.message}")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = NewOrderState.Idle
    }
}
