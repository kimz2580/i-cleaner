package com.hamza.icleaner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RollingBasketLoading(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "basket")
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Bounce animation
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = bounce.dp)
                .graphicsLayer(rotationZ = rotation)
        ) {
            Text(text = "🧺", fontSize = 60.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Processing Laundry...",
            fontSize = 14.sp,
            color = androidx.compose.ui.graphics.Color.Gray
        )
    }
}
