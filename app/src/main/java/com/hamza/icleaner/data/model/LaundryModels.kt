package com.hamza.icleaner.data.model

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

/**
 * Data model for Order. 
 * Supports both JSON (Retrofit) and Firestore mapping.
 */
data class Order(
    @SerializedName("order_id") @get:PropertyName("order_id") @set:PropertyName("order_id") var orderId: String = "",
    @SerializedName("order_number") @get:PropertyName("order_number") @set:PropertyName("order_number") var orderNumber: String = "",
    @SerializedName("customer_id") @get:PropertyName("customer_id") @set:PropertyName("customer_id") var customerId: String = "",
    @SerializedName("customer_name") @get:PropertyName("customer_name") @set:PropertyName("customer_name") var customerName: String = "",
    @SerializedName("service_type") @get:PropertyName("service_type") @set:PropertyName("service_type") var serviceType: String = "",
    @SerializedName("garment_count") @get:PropertyName("garment_count") @set:PropertyName("garment_count") var garmentCount: Int = 0,
    @SerializedName("garments") @get:PropertyName("garments") @set:PropertyName("garments") var garments: Map<String, Int> = emptyMap(),
    @SerializedName("subtotal") @get:PropertyName("subtotal") @set:PropertyName("subtotal") var subtotal: Double = 0.0,
    @SerializedName("total_amount") @get:PropertyName("total_amount") @set:PropertyName("total_amount") var totalAmount: Double = 0.0,
    @SerializedName("final_amount") @get:PropertyName("final_amount") @set:PropertyName("final_amount") var finalAmount: Double = 0.0,
    @SerializedName("status") @get:PropertyName("status") @set:PropertyName("status") var status: String = "Pending",
    @SerializedName("payment_status") @get:PropertyName("payment_status") @set:PropertyName("payment_status") var paymentStatus: String = "Pending",
    @SerializedName("payment_method") @get:PropertyName("payment_method") @set:PropertyName("payment_method") var paymentMethod: String = "Cash",
    @SerializedName("pickup_address") @get:PropertyName("pickup_address") @set:PropertyName("pickup_address") var pickupAddress: String = "",
    @SerializedName("delivery_address") @get:PropertyName("delivery_address") @set:PropertyName("delivery_address") var deliveryAddress: String = "",
    @SerializedName("pickup_date") @get:PropertyName("pickup_date") @set:PropertyName("pickup_date") var pickupDate: String = "",
    @SerializedName("special_instructions") @get:PropertyName("special_instructions") @set:PropertyName("special_instructions") var specialInstructions: String = "",
    @SerializedName("created_at") @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: String = "",
    @SerializedName("updated_at") @get:PropertyName("updated_at") @set:PropertyName("updated_at") var updatedAt: String = ""
) {
    // Required empty constructor for Firestore
    constructor() : this("", "", "", "", "", 0, emptyMap(), 0.0, 0.0, 0.0, "Pending", "Pending", "Cash", "", "", "", "", "", "")
}

data class Customer(
    @SerializedName("customer_id") @get:PropertyName("customer_id") @set:PropertyName("customer_id") var customerId: String = "",
    @SerializedName("full_name") @get:PropertyName("full_name") @set:PropertyName("full_name") var fullName: String = "",
    @SerializedName("phone") @get:PropertyName("phone") @set:PropertyName("phone") var phone: String = "",
    @SerializedName("email") @get:PropertyName("email") @set:PropertyName("email") var email: String? = null,
    @SerializedName("address") @get:PropertyName("address") @set:PropertyName("address") var address: String? = null
) {
    constructor() : this("", "", "", null, null)
}

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
