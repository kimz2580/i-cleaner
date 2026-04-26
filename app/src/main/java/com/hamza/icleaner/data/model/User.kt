package com.hamza.icleaner.data.model

import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id") @get:PropertyName("user_id") @set:PropertyName("user_id") var userId: String = "",
    @SerializedName("full_name") @get:PropertyName("full_name") @set:PropertyName("full_name") var fullName: String = "",
    @SerializedName("username") @get:PropertyName("username") @set:PropertyName("username") var username: String = "",
    @SerializedName("email") @get:PropertyName("email") @set:PropertyName("email") var email: String? = null,
    @SerializedName("phone") @get:PropertyName("phone") @set:PropertyName("phone") var phone: String? = null,
    @SerializedName("role") @get:PropertyName("role") @set:PropertyName("role") var role: String = "employee"
) {
    // Required empty constructor for Firestore
    constructor() : this("", "", "", null, null, "employee")
}

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val token: String?,
    val role: String?,
    val user: User?
)
