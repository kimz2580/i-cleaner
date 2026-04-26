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

sealed class OrdersState {
    object Loading : OrdersState()
    data class Success(val orders: List<Order>) : OrdersState()
    data class Error(val message: String) : OrdersState()
}

class OrdersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<OrdersState>(OrdersState.Loading)
    val uiState: StateFlow<OrdersState> = _uiState.asStateFlow()
    private var ordersListener: ListenerRegistration? = null

    fun loadOrders(status: String = "all") {
        ordersListener?.remove()
        _uiState.value = OrdersState.Loading
        
        val collection = db.collection("orders")
        
        // Use a simpler query first to avoid immediate Index errors
        val query = if (status == "all") {
            collection // Removed orderBy for now to prevent crash
        } else {
            collection.whereEqualTo("status", status)
        }

        ordersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // If index is missing, we still want to show SOMETHING
                _uiState.value = OrdersState.Error("Database indexing... please wait. ${error.message}")
                
                // Fallback: just get the collection without sorting
                db.collection("orders").limit(50).addSnapshotListener { fallbackSnapshot, _ ->
                    val orders = fallbackSnapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.apply { if (orderId.isEmpty()) orderId = doc.id }
                    } ?: emptyList()
                    _uiState.value = OrdersState.Success(orders)
                }
                return@addSnapshotListener
            }

            val orders = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.apply { if (orderId.isEmpty()) orderId = doc.id }
            } ?: emptyList()
            
            // Sort in memory for now to avoid requiring a Firestore Index
            val sortedOrders = orders.sortedByDescending { it.createdAt }
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
