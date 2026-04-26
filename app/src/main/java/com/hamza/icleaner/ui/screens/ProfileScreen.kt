package com.hamza.icleaner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.hamza.icleaner.data.repository.SessionManager
import com.hamza.icleaner.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    sessionManager: SessionManager
) {
    val userName by sessionManager.userName.collectAsState(initial = "")
    val userRole by sessionManager.userRole.collectAsState(initial = "")
    val userEmail by sessionManager.userEmail.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName?.take(1)?.uppercase() ?: "?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = userName ?: "Unknown User", style = MaterialTheme.typography.headlineMedium)
            Text(text = userRole?.replaceFirstChar { it.uppercase() } ?: "", color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileItem(
                        label = "Full Name",
                        value = userName ?: "Not Set",
                        icon = Icons.Default.Person
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileItem(
                        label = "Role",
                        value = userRole?.replaceFirstChar { it.uppercase() } ?: "User",
                        icon = Icons.Default.Badge
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileItem(
                        label = "Email",
                        value = userEmail ?: "No email set",
                        icon = Icons.Default.Email
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { /* Navigate to edit profile */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { mainViewModel.logout() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }
}
