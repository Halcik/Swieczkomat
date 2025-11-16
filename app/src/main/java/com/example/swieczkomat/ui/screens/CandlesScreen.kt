package com.example.swieczkomat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swieczkomat.data.Candle
import com.example.swieczkomat.vievmodels.CandleViewModel
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.ui.dialogs.AddCandleDialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CandlesTab(darkMode: Boolean, candleViewModel: CandleViewModel, materialsViewModel: MaterialsViewModel) {
    val candles by candleViewModel.getAllCandles().collectAsState(initial = emptyList())
    val materials by materialsViewModel.getAllMaterials().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text("Świeczki", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (darkMode) Color.White else Color.Black)
            Spacer(Modifier.height(12.dp))
            LazyColumn { items(candles) { candle -> CandleRow(candle, darkMode, dateFormat) } }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF7C3AED),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Text("+") }
    }
    if (showAddDialog) {
        AddCandleDialog(
            darkMode = darkMode,
            materials = materials,
            candleViewModel = candleViewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun CandleRow(candle: Candle, darkMode: Boolean, dateFormat: SimpleDateFormat) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Dla: ${candle.forWhom}", fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
            Text("Pojemnik: ${candle.containerName ?: "-"}")
            Text("Zapach: ${candle.fragranceName ?: "-"} (${candle.concentration}%)")
            Text("Knot: ${candle.wickName ?: "-"}")
            candle.dyeName?.let { Text("Barwnik: $it") }
            Text("Koszt: ${String.format(Locale.getDefault(),"%.2f zł", candle.cost)}")
            Text("Zrobiona: ${dateFormat.format(Date(candle.dateMade))}")
            Text("Odpalić po: ${dateFormat.format(Date(candle.dateToLight))}")
            Text("Czas palenia: ${candle.burnTimeMinutes} min")
        }
    }
}
