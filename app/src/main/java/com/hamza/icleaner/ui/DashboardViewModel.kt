package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.hamza.icleaner.data.model.DashboardStats
import com.hamza.icleaner.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val stats: DashboardStats, 
        val activeOrders: List<Order>, 
        val todaysOrdersList: List<Order>,
        val pastOrders: List<Order> = emptyList()
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    fun loadDashboard(role: String) {
        // Remove existing listener if any
        dashboardListener?.remove()
        
        _uiState.value = DashboardState.Loading
        
        // Use a SnapshotListener for real-time updates!
        dashboardListener = db.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.value = DashboardState.Error("Dashboard Error: ${error.localizedMessage}")
                return@addSnapshotListener
            }
            
            if (snapshot == null) return@addSnapshotListener

            viewModelScope.launch {
                try {
                    val allOrders = snapshot.documents.mapNotNull { doc ->
                        try {
                            val order = Order()
                            order.orderId = doc.id
                            // Robust field mapping: checks for various naming conventions
                            order.orderNumber = (doc.get("order_number") ?: doc.get("orderNumber") ?: "").toString()
                            order.customerName = (doc.get("customer_name") ?: doc.get("customerName") ?: "Unknown").toString()
                            order.status = (doc.get("status") ?: "Pending").toString()
                            order.paymentStatus = (doc.get("payment_status") ?: doc.get("paymentStatus") ?: "Pending").toString()
                            order.serviceType = (doc.get("service_type") ?: doc.get("serviceType") ?: "").toString()
                            
                            // Amount fallback: final_amount -> total_amount -> totalAmount
                            val finalAmountRaw = doc.get("final_amount") ?: doc.get("total_amount") ?: doc.get("totalAmount")
                            order.finalAmount = when (finalAmountRaw) {
                                is Number -> finalAmountRaw.toDouble()
                                is String -> finalAmountRaw.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }

                            // Date fallback: handles Timestamp objects, Strings, and Numbers
                            val createdAtRaw = doc.get("created_at") ?: doc.get("createdAt") ?: doc.get("date")
                            order.createdAt = when (createdAtRaw) {
                                is com.google.firebase.Timestamp -> createdAtRaw.toDate().time.toString()
                                is Number -> createdAtRaw.toLong().toString()
                                is String -> createdAtRaw
                                else -> ""
                            }
                            order
                        } catch (e: Exception) { null }
                    }
                    
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val todayStr = sdf.format(Date())
                    
                    val todaysOrders = allOrders.filter { order ->
                        if (order.createdAt.isEmpty()) return@filter false
                        try {
                            val timestamp = order.createdAt.toLongOrNull()
                            if (timestamp != null) {
                                sdf.format(Date(timestamp)) == todayStr
                            } else {
                                order.createdAt.startsWith(todayStr)
                            }
                        } catch (e: Exception) { false }
                    }

                    // 1. Active Orders: Those NOT yet Paid and NOT yet Completed
                    val activeOrders = allOrders.filter { 
                        it.status.lowercase() in listOf("pending", "processing", "ready") && 
                        it.paymentStatus.lowercase() != "paid"
                    }.sortedByDescending { it.createdAt }

                    // 2. Past Orders: Those that are Paid OR Completed OR Cancelled
                    val pastOrders = allOrders.filter {
                        it.paymentStatus.lowercase() == "paid" || 
                        it.status.lowercase() in listOf("completed", "cancelled", "delivered")
                    }.sortedByDescending { it.createdAt }
                    
                    // Total Bill: The full amount of all orders created TODAY
                    val totalBill = todaysOrders.sumOf { it.finalAmount }

                    val customersCount = db.collection("customers").get().await().size()
                    val staffCount = db.collection("users").whereEqualTo("role", "employee").get().await().size()

                    val stats = DashboardStats(
                        todayOrders = todaysOrders.size,
                        totalBill = totalBill,
                        totalCustomers = customersCount, 
                        activeEmployees = staffCount,
                        pendingPayments = allOrders.count { it.paymentStatus.lowercase() == "pending" },
                        activeOrders = activeOrders.size
                    )

                    _uiState.value = DashboardState.Success(stats, activeOrders, todaysOrders, pastOrders)
                } catch (e: Exception) {
                    _uiState.value = DashboardState.Error("Calculation error: ${e.localizedMessage}")
                }
            }
        }
    }

    private var dashboardListener: ListenerRegistration? = null
    
    override fun onCleared() {
        super.onCleared()
        dashboardListener?.remove()
    }
}
