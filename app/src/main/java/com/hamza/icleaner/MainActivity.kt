package com.hamza.icleaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hamza.icleaner.data.repository.SessionManager
import com.hamza.icleaner.ui.OrdersViewModel
import com.hamza.icleaner.ui.OrdersState
import com.hamza.icleaner.data.model.Order
import com.hamza.icleaner.ui.MainViewModel
import com.hamza.icleaner.ui.screens.*
import com.hamza.icleaner.ui.theme.ICleanerTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            ICleanerTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return MainViewModel(sessionManager) as T
                        }
                    }
                )
                
                val userRole by sessionManager.userRole.collectAsState(initial = null)
                val userToken by sessionManager.userToken.collectAsState(initial = null)

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = mainViewModel,
                            onLoginSuccess = { _ ->
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = {
                                navController.navigate("signup")
                            }
                        )
                    }
                    composable("signup") {
                        SignUpScreen(
                            viewModel = mainViewModel,
                            onSignUpSuccess = { _ ->
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onBackToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("dashboard") {
                        DashboardScreen(
                            role = userRole ?: "employee",
                            mainViewModel = mainViewModel,
                            navController = navController
                        )
                    }
                    composable("new_order") {
                        NewOrderScreen(
                            navController = navController,
                            sessionManager = sessionManager
                        )
                    }
                    composable("orders") {
                        OrdersScreen(
                            navController = navController,
                            onOrderClick = { orderId ->
                                navController.navigate("order_detail/$orderId")
                            }
                        )
                    }
                    composable(
                        "order_detail/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        val ordersViewModel: OrdersViewModel = viewModel()
                        val state by ordersViewModel.uiState.collectAsState()
                        
                        LaunchedEffect(Unit) {
                            if (state !is OrdersState.Success) {
                                ordersViewModel.loadOrders()
                            }
                        }

                        when (val s = state) {
                            is OrdersState.Success -> {
                                val order = s.orders.find { it.orderId == orderId }
                                if (order != null) {
                                    OrderDetailScreen(
                                        navController = navController, 
                                        order = order,
                                        viewModel = ordersViewModel
                                    )
                                } else {
                                    Text("Order not found")
                                }
                            }
                            is OrdersState.Loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                            is OrdersState.Error -> {
                                Text("Error: ${s.message}")
                            }
                        }
                    }
                    composable("customers") {
                        CustomersScreen(navController = navController)
                    }
                    composable("profile") {
                        ProfileScreen(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            sessionManager = sessionManager
                        )
                    }
                    composable("employees") {
                        EmployeesScreen(navController = navController)
                    }
                    composable(
                        "payment/{orderId}/{amount}",
                        arguments = listOf(
                            navArgument("orderId") { type = NavType.StringType },
                            navArgument("amount") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        val amountStr = backStackEntry.arguments?.getString("amount") ?: "0"
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        PaymentScreen(navController = navController, orderId = orderId, amount = amount)
                    }
                }

                // Handle Login/Logout redirection
                LaunchedEffect(userToken) {
                    if (userToken != null) {
                        if (navController.currentDestination?.route == "login") {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    } else {
                        if (navController.currentDestination?.route != "login") {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
}
