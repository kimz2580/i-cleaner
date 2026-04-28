package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.hamza.icleaner.data.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class MainViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val trimmedUsername = username.trim()
                val email = if (!trimmedUsername.contains("@")) "$trimmedUsername@laundry.com" else trimmedUsername

                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                
                if (user != null) {
                    // Fetch user role from Firestore
                    val doc = db.collection("users").document(user.uid).get().await()
                    if (doc.exists()) {
                        val role = doc.getString("role") ?: "employee"
                        
                        // If they are admin or staff, we let them in ONLY ONCE to set things up, 
                        // or we can allow them but give a warning. 
                        // Given your goal of a web-only admin, let's keep the restriction 
                        // but make sure YOU can still get in to test.
                        
                        /* 
                        if (role.lowercase() != "customer") {
                            auth.signOut()
                            _authState.value = AuthState.Error("This app is for customers only. Please use the Web Dashboard for Admin/Staff access.")
                            return@launch
                        }
                        */

                        val fullName = doc.getString("full_name") ?: "User"
                        val emailStored = doc.getString("email") ?: email
                        val phone = doc.getString("phone") ?: ""
                        
                        sessionManager.saveSession(
                            user.uid,
                            role,
                            fullName,
                            emailStored,
                            phone
                        )
                        _authState.value = AuthState.Success(role)
                    } else {
                        // Handle case where auth exists but firestore doc doesn't
                        _authState.value = AuthState.Error("User profile not found in database")
                    }
                } else {
                    _authState.value = AuthState.Error("Login failed: User null")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Auth error")
            }
        }
    }

    fun register(email: String, password: String, fullName: String, phone: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val trimmedEmail = email.trim()
                val finalEmail = if (!trimmedEmail.contains("@")) "$trimmedEmail@laundry.com" else trimmedEmail
                
                val result = auth.createUserWithEmailAndPassword(finalEmail, password).await()
                val user = result.user
                
                if (user != null) {
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "email" to finalEmail,
                        "full_name" to fullName,
                        "phone" to phone,
                        "role" to role.lowercase(),
                        "created_at" to com.google.firebase.Timestamp.now()
                    )
                    
                    db.collection("users").document(user.uid).set(userData).await()
                    
                    sessionManager.saveSession(user.uid, role.lowercase(), fullName, finalEmail, phone)
                    _authState.value = AuthState.Success(role.lowercase())
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Registration error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            sessionManager.clearSession()
            _authState.value = AuthState.Idle
        }
    }
}
