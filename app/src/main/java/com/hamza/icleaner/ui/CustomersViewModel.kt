package com.hamza.icleaner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.hamza.icleaner.data.model.Customer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CustomersState {
    object Loading : CustomersState()
    data class Success(val customers: List<Customer>) : CustomersState()
    data class Error(val message: String) : CustomersState()
}

class CustomersViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _uiState = MutableStateFlow<CustomersState>(CustomersState.Loading)
    val uiState: StateFlow<CustomersState> = _uiState.asStateFlow()
    private var customersListener: ListenerRegistration? = null

    fun loadCustomers(search: String = "") {
        customersListener?.remove()
        _uiState.value = CustomersState.Loading
        
        val collection = db.collection("customers")
        val query = if (search.isEmpty()) {
            collection
        } else {
            collection.whereGreaterThanOrEqualTo("full_name", search)
                .whereLessThanOrEqualTo("full_name", search + "\uf8ff")
        }

        customersListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.value = CustomersState.Error("Firebase error: ${error.message}")
                return@addSnapshotListener
            }

            val customers = snapshot?.toObjects(Customer::class.java) ?: emptyList()
            _uiState.value = CustomersState.Success(customers)
        }
    }

    override fun onCleared() {
        super.onCleared()
        customersListener?.remove()
    }
}
