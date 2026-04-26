package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.icleaner.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EmployeesState {
    object Loading : EmployeesState()
    data class Success(val employees: List<User>) : EmployeesState()
    data class Error(val message: String) : EmployeesState()
}

class EmployeesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<EmployeesState>(EmployeesState.Loading)
    val uiState: StateFlow<EmployeesState> = _uiState.asStateFlow()
    private var employeesListener: ListenerRegistration? = null

    fun loadEmployees() {
        employeesListener?.remove()
        _uiState.value = EmployeesState.Loading
        
        employeesListener = db.collection("users")
            .whereIn("role", listOf("admin", "employee"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = EmployeesState.Error("Firebase error: ${error.message}")
                    return@addSnapshotListener
                }

                val employees = snapshot?.toObjects(User::class.java) ?: emptyList()
                _uiState.value = EmployeesState.Success(employees)
            }
    }

    fun addEmployee(user: User, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                // Generate a unique ID if not provided
                val id = if (user.userId.isEmpty()) db.collection("users").document().id else user.userId
                val newUser = user.copy(userId = id, role = "employee")
                
                db.collection("users").document(id)
                    .set(newUser)
                    .await()
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        employeesListener?.remove()
    }
}
