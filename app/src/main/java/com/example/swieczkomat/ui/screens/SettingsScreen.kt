package com.example.swieczkomat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SettingsTab(darkMode: Boolean, onToggleDarkMode: () -> Unit) {
    val topSpacing = 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = topSpacing),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Ustawienia",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (darkMode) Color(0xFFE5E7EB) else Color(0xFF374151)
        )
        Spacer(Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Tryb ciemny",
                        fontWeight = FontWeight.Medium,
                        color = if (darkMode) Color(0xFFF3F4F6) else Color(0xFF1F2937)
                    )
                    Text(
                        if (darkMode) "Aktualnie: Ciemny" else "Aktualnie: Jasny",
                        fontSize = 12.sp,
                        color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                    )
                }
                Switch(
                    checked = darkMode,
                    onCheckedChange = { onToggleDarkMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF9333EA),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF6B7280)
                    )
                )
            }
        }
    }
}
