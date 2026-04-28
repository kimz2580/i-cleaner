package com.hamza.icleaner.data.model

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

/**
 * Data model for Order. 
 * Supports both JSON (Retrofit) and Firestore mapping.
 */
data class Order(
    @SerializedName("orderId") @get:PropertyName("orderId") @set:PropertyName("orderId") var orderId: String = "",
    @SerializedName("userId") @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @SerializedName("orderNumber") @get:PropertyName("orderNumber") @set:PropertyName("orderNumber") var orderNumber: String = "",
    @SerializedName("customerName") @get:PropertyName("customerName") @set:PropertyName("customerName") var customerName: String = "",
    @SerializedName("customerPhone") @get:PropertyName("customerPhone") @set:PropertyName("customerPhone") var customerPhone: String = "",
    @SerializedName("serviceType") @get:PropertyName("serviceType") @set:PropertyName("serviceType") var serviceType: String = "",
    @SerializedName("garmentCount") @get:PropertyName("garmentCount") @set:PropertyName("garmentCount") var garmentCount: Int = 0,
    @SerializedName("garments") @get:PropertyName("garments") @set:PropertyName("garments") var garments: Map<String, Int> = emptyMap(),
    @SerializedName("subtotal") @get:PropertyName("subtotal") @set:PropertyName("subtotal") var subtotal: Double = 0.0,
    @SerializedName("totalAmount") @get:PropertyName("totalAmount") @set:PropertyName("totalAmount") var totalAmount: Double = 0.0,
    @SerializedName("finalAmount") @get:PropertyName("finalAmount") @set:PropertyName("finalAmount") var finalAmount: Double = 0.0,
    @SerializedName("status") @get:PropertyName("status") @set:PropertyName("status") var status: String = "Pending",
    @SerializedName("paymentStatus") @get:PropertyName("paymentStatus") @set:PropertyName("paymentStatus") var paymentStatus: String = "Unpaid",
    @SerializedName("paymentMethod") @get:PropertyName("paymentMethod") @set:PropertyName("paymentMethod") var paymentMethod: String = "Cash",
    @SerializedName("pickupAddress") @get:PropertyName("pickupAddress") @set:PropertyName("pickupAddress") var pickupAddress: String = "",
    @SerializedName("deliveryAddress") @get:PropertyName("deliveryAddress") @set:PropertyName("deliveryAddress") var deliveryAddress: String = "",
    @SerializedName("specialInstructions") @get:PropertyName("specialInstructions") @set:PropertyName("specialInstructions") var specialInstructions: String = "",
    @SerializedName("createdAt") @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Any? = null,
    @SerializedName("updatedAt") @get:PropertyName("updatedAt") @set:PropertyName("updatedAt") var updatedAt: Any? = null
) {
    // Required empty constructor for Firestore
    constructor() : this("", "", "", "", "", "", 0, emptyMap(), 0.0, 0.0, 0.0, "Pending", "Unpaid", "Cash", "", "", "", null, null)
}

data class Customer(
    var userId: String = "",
    var fullName: String = "",
    var phone: String = "",
    var email: String? = null,
    var address: String? = null
)

data class DashboardStats(
    val todayOrders: Int = 0,
    val totalBill: Double = 0.0,
    val totalCustomers: Int = 0,
    val activeEmployees: Int = 0,
    val pendingPayments: Int = 0,
    val activeOrders: Int = 0
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
