package com.hamza.icleaner.data.api

import com.hamza.icleaner.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("mobile_login.php")
    suspend fun login(@Body credentials: Map<String, String>): Response<LoginResponse>

    @GET("dashboard_stats.php")
    suspend fun getDashboardStats(@Query("role") role: String): Response<ApiResponse<DashboardStats>>

    @GET("orders.php")
    suspend fun getOrders(@Query("status") status: String): Response<ApiResponse<List<Order>>>

    @POST("create_order.php")
    suspend fun createOrder(@Body orderData: Map<String, Any>): Response<ApiResponse<Order>>

    @POST("update_order.php")
    suspend fun updateOrderStatus(@Body data: Map<String, String>): Response<ApiResponse<Unit>>

    @GET("customers.php")
    suspend fun getCustomers(@Query("search") search: String): Response<ApiResponse<List<Customer>>>

    @POST("add_customer.php")
    suspend fun addCustomer(@Body customerData: Map<String, String>): Response<ApiResponse<Customer>>

    @GET("employees.php")
    suspend fun getEmployees(): Response<ApiResponse<List<User>>>

    @GET("activities.php")
    suspend fun getActivities(): Response<ApiResponse<List<ActivityLog>>>
}

data class ActivityLog(
    val action: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("created_at") val createdAt: String
)
