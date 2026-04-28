package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hamza.icleaner.ui.DashboardState
import com.hamza.icleaner.ui.DashboardViewModel
import com.hamza.icleaner.ui.MainViewModel
import com.hamza.icleaner.ui.components.OrderCard

import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    role: String,
    mainViewModel: MainViewModel,
    navController: NavController,
    dashboardViewModel: DashboardViewModel = viewModel(),
    onNewOrderClick: () -> Unit = { navController.navigate("new_order") }
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        dashboardViewModel.loadDashboard(role)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "i-Cleaner", 
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { mainViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewOrderClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Order")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("orders") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Orders") }
                )
                // Only Admin sees Customers and Staff in bottom bar
                if (role == "admin") {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("customers") },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text("Customers") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("employees") },
                        icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        label = { Text("Staff") }
                    )
                } else {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("profile") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        label = { Text("Profile") }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Welcome to i-Cleaner!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "What would you like to do today?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        item {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                QuickActionCard(
                                    title = "New Order",
                                    icon = Icons.Default.AddCircle,
                                    onClick = onNewOrderClick,
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                QuickActionCard(
                                    title = "All Orders",
                                    icon = Icons.AutoMirrored.Filled.List,
                                    onClick = { navController.navigate("orders") },
                                    modifier = Modifier.weight(1f),
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            if (role == "admin") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    QuickActionCard(
                                        title = "Customers",
                                        icon = Icons.Default.People,
                                        onClick = { navController.navigate("customers") },
                                        modifier = Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    QuickActionCard(
                                        title = "Staff",
                                        icon = Icons.Default.Badge,
                                        onClick = { navController.navigate("employees") },
                                        modifier = Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Performance Stats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    title = "Today's Orders",
                                    value = state.stats.todayOrders.toString(),
                                    icon = Icons.Default.ShoppingCart,
                                    color = Color(0xFF4CAF50), // Vibrant Green
                                    containerColor = Color(0xFFE8F5E9), // Light Green background
                                    modifier = Modifier.weight(1f),
                                    onClick = { 
                                        if (state.todaysOrdersList.isNotEmpty()) {
                                            scope.launch { listState.animateScrollToItem(index = 4) }
                                        }
                                    }
                                )
                                StatCard(
                                    title = "Total Bill",
                                    value = "TSH ${"%,.2f".format(state.stats.totalBill)}",
                                    icon = Icons.Default.Payments,
                                    color = Color(0xFF2196F3), // Bright Blue
                                    containerColor = Color(0xFFE3F2FD), // Light Blue background
                                    modifier = Modifier.weight(1f),
                                    onClick = { 
                                        if (state.todaysOrdersList.isNotEmpty()) {
                                            scope.launch { listState.animateScrollToItem(index = 4) }
                                        }
                                    }
                                )
                            }
                        }

                        if (state.todaysOrdersList.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Today's Orders Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(state.todaysOrdersList) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        navController.navigate("order_detail/${order.orderId}")
                                    }
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Orders (Unpaid)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                TextButton(onClick = { navController.navigate("orders") }) {
                                    Text("See All")
                                }
                            }
                        }

                        items(state.activeOrders.take(10)) { order ->
                            OrderCard(
                                order = order,
                                onClick = {
                                    navController.navigate("order_detail/${order.orderId}")
                                }
                            )
                        }

                        if (state.activeOrders.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = "No active unpaid orders",
                                        modifier = Modifier.padding(32.dp).align(Alignment.CenterHorizontally),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // --- NEW HISTORY SECTION ---
                        if (state.pastOrders.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Past Orders (Paid/Completed)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(state.pastOrders.take(5)) { order ->
                                OrderCard(
                                    order = order,
                                    onClick = {
                                        navController.navigate("order_detail/${order.orderId}")
                                    }
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
                is DashboardState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { dashboardViewModel.loadDashboard(role) }) {
                            Text("Retry Connection")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
        }
    }
}
