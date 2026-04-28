package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.hamza.icleaner.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.firebase.auth.FirebaseAuth

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<Order>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}

class OrdersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val uiState: StateFlow<OrdersState> = _uiState.asStateFlow()
    private var ordersListener: ListenerRegistration? = null

    fun loadOrders(status: String = "all") {
        ordersListener?.remove()
        _uiState.value = OrdersState.Loading
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = OrdersState.Error("Please log in to see your orders")
            return
        }
        
        // STRICT PRIVACY: Only fetch orders where userId matches the logged-in user
        val query = db.collection("orders").whereEqualTo("userId", currentUser.uid)

        ordersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.value = OrdersState.Error("Accessing your orders... ${error.message}")
                return@addSnapshotListener
            }

            val myOrders = snapshot?.documents?.mapNotNull { doc ->
                val order = doc.toObject(Order::class.java)
                order?.apply { 
                    if (orderId.isEmpty()) orderId = doc.id 
                }
            } ?: emptyList()
            
            // Sort by creation date string (newest first)
            val sortedOrders = myOrders.sortedByDescending { it.createdAt.toString() }
            _uiState.value = OrdersState.Success(sortedOrders)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
    }

    fun getOrderById(orderId: String): Order? {
        val state = _uiState.value
        return if (state is OrdersState.Success) {
            state.orders.find { it.orderId == orderId }
        } else {
            null
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Update Firestore
                db.collection("orders").document(orderId)
                    .update("status", newStatus)
                    .await()
                
                // 2. Sync with Backend
                try {
                    com.hamza.icleaner.data.api.RetrofitClient.apiService.updateOrderStatus(
                        mapOf("order_id" to orderId, "status" to newStatus)
                    )
                } catch (e: Exception) {
                    // Log error but Firestore is the source of truth for now
                }
                
                // Refresh local state
                val currentState = _uiState.value
                if (currentState is OrdersState.Success) {
                    val updatedOrders = currentState.orders.map {
                        if (it.orderId == orderId) it.copy(status = newStatus) else it
                    }
                    _uiState.value = OrdersState.Success(updatedOrders)
                }
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun cancelOrder(orderId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("orders").document(orderId)
                    .update("status", "Cancelled")
                    .await()
                
                // Also update local state
                val currentState = _uiState.value
                if (currentState is OrdersState.Success) {
                    val updatedOrders = currentState.orders.map {
                        if (it.orderId == orderId) it.copy(status = "Cancelled") else it
                    }
                    _uiState.value = OrdersState.Success(updatedOrders)
                }
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }
}
