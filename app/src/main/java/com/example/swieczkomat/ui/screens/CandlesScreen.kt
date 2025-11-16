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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CandlesTab(darkMode: Boolean, candleViewModel: CandleViewModel, materialsViewModel: MaterialsViewModel) {
    val candles by candleViewModel.getAllCandles().collectAsState(initial = emptyList())
    val materials by materialsViewModel.getAllMaterials().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var editTargetId by remember { mutableStateOf<Int?>(null) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text("Świeczki", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (darkMode) Color.White else Color.Black)
            Spacer(Modifier.height(12.dp))
            LazyColumn { items(candles) { candle -> CandleRow(candle, darkMode, dateFormat,
                onEdit = { editTargetId = candle.id },
                onDelete = { candleViewModel.deleteCandle(candle.id) },
                onAddBurn = { candleViewModel.addBurnTime(candle.id, 15) }
            ) } }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF2563EB),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Dodaj świeczki", tint = Color.White) }
    }
    if (showAddDialog) {
        AddCandleDialog(
            darkMode = darkMode,
            materials = materials,
            candleViewModel = candleViewModel,
            onDismiss = { showAddDialog = false }
        )
    }
    editTargetId?.let { id ->
        val candleToEdit = candles.find { it.id == id }
        if (candleToEdit != null) {
            EditCandleDialog(
                darkMode = darkMode,
                candle = candleToEdit,
                onDismiss = { editTargetId = null },
                onSave = { updated -> candleViewModel.updateCandle(updated); editTargetId = null }
            )
        }
    }
}

@Composable
private fun CandleRow(
    candle: Candle,
    darkMode: Boolean,
    dateFormat: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddBurn: () -> Unit
) {
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
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onAddBurn) { Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("+15 min") }
                OutlinedButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Edytuj") }
                OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) { Icon(Icons.Default.Delete, contentDescription = null); Spacer(Modifier.width(4.dp)); Text("Usuń") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCandleDialog(
    darkMode: Boolean,
    candle: Candle,
    onDismiss: () -> Unit,
    onSave: (Candle) -> Unit
) {
    var forWhom by remember { mutableStateOf(candle.forWhom) }
    var burnTime by remember { mutableStateOf(candle.burnTimeMinutes.toString()) }
    val fieldContainerColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val fieldTextColor = if (darkMode) Color.White else Color.Black
    val fieldLabelColor = if (darkMode) Color(0xFFCBD5E1) else Color(0xFF374151)
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)) {
            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Edytuj świeczkę", fontSize = 20.sp, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = forWhom,
                    onValueChange = { forWhom = it },
                    label = { Text("Dla kogo?", color = fieldLabelColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = fieldContainerColor,
                        unfocusedContainerColor = fieldContainerColor,
                        disabledContainerColor = fieldContainerColor,
                        focusedTextColor = fieldTextColor,
                        unfocusedTextColor = fieldTextColor,
                        focusedLabelColor = fieldLabelColor,
                        unfocusedLabelColor = fieldLabelColor,
                        cursorColor = fieldTextColor
                    )
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = burnTime,
                    onValueChange = { burnTime = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Czas palenia (min)", color = fieldLabelColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = fieldContainerColor,
                        unfocusedContainerColor = fieldContainerColor,
                        disabledContainerColor = fieldContainerColor,
                        focusedTextColor = fieldTextColor,
                        unfocusedTextColor = fieldTextColor,
                        focusedLabelColor = fieldLabelColor,
                        unfocusedLabelColor = fieldLabelColor,
                        cursorColor = fieldTextColor
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val updated = candle.copy(forWhom = forWhom.ifBlank { "" }, burnTimeMinutes = burnTime.toIntOrNull() ?: candle.burnTimeMinutes)
                        onSave(updated)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { Text("Zapisz") }
                }
            }
        }
    }
}
