package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class PaymentViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val uiState: StateFlow<PaymentState> = _uiState.asStateFlow()

    fun processPayment(orderId: String, amount: Double, method: String) {
        viewModelScope.launch {
            _uiState.value = PaymentState.Loading
            try {
                // Update Firestore
                db.collection("orders").document(orderId)
                    .update(
                        mapOf(
                            "payment_status" to "Paid",
                            "payment_method" to method,
                            "updated_at" to System.currentTimeMillis().toString()
                        )
                    ).await()
                
                // Optional: Sync with Backend
                try {
                    com.hamza.icleaner.data.api.RetrofitClient.apiService.updateOrderStatus(
                        mapOf("order_id" to orderId, "payment_status" to "Paid", "payment_method" to method)
                    )
                } catch (e: Exception) {
                    // Ignore backend failures if Firestore succeeded
                }
                
                _uiState.value = PaymentState.Success
            } catch (e: Exception) {
                _uiState.value = PaymentState.Error("Payment failed: ${e.message}")
            }
        }
    }
}
