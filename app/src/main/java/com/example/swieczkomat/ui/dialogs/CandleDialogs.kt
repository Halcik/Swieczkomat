package com.example.swieczkomat.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.swieczkomat.data.Candle
import com.example.swieczkomat.ui.util.MaterialUtils
import com.example.swieczkomat.data.Material
import java.util.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveCandlesDialog(
    count: Int,
    totalPrice: Double,
    container: Material?,
    wax: Material?,
    fragrance: Material?,
    wick: Material?,
    dye: Material?,
    concentration: Double,
    darkMode: Boolean,
    onDismiss: () -> Unit,
    onSave: (List<Candle>) -> Unit
) {
    var forWhom by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Zapisz świeczki", fontSize = 20.sp, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = forWhom,
                    onValueChange = { forWhom = it },
                    label = { Text("Dla kogo?") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                val singleCost = if (count > 0) totalPrice / count else 0.0
                Text("Koszt / świeczkę: ${String.format(Locale.getDefault(),"%.2f zł", singleCost)}")
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val capacity = container?.let { MaterialUtils.extractCapacity(it.name) } ?: 0.0
                        val now = System.currentTimeMillis()
                        val dateToLight = now + 14L * 24 * 60 * 60 * 1000
                        val list = (1..count).map {
                            Candle(
                                containerName = container?.name,
                                waxName = wax?.name,
                                fragranceName = fragrance?.name,
                                wickName = wick?.name,
                                dyeName = dye?.name,
                                concentration = concentration,
                                capacity = capacity,
                                cost = singleCost,
                                forWhom = forWhom.ifBlank { "" },
                                dateMade = now,
                                dateToLight = dateToLight,
                                burnTimeMinutes = 0
                            )
                        }
                        onSave(list)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { Text("Zapisz") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCandleDialog(
    darkMode: Boolean,
    materials: List<Material>,
    candleViewModel: com.example.swieczkomat.vievmodels.CandleViewModel,
    onDismiss: () -> Unit
) {
    // Stan wyboru materiałów
    var concentration by remember { mutableStateOf("5.0") }
    var count by remember { mutableStateOf("1") }
    var selectedContainer by remember { mutableStateOf<Material?>(null) }
    var selectedWax by remember { mutableStateOf<Material?>(null) }
    var selectedFragrance by remember { mutableStateOf<Material?>(null) }
    var selectedWick by remember { mutableStateOf<Material?>(null) }
    var selectedDye by remember { mutableStateOf<Material?>(null) }
    var wickLength by remember { mutableStateOf("0.15") }
    var forWhom by remember { mutableStateOf("") }

    // Filtrowanie kategorii
    val containers = materials.filter { it.category == "Pojemnik" }
    val waxes = materials.filter { it.category == "Wosk" }
    val fragrances = materials.filter { it.category == "Olejek" }
    val wicks = materials.filter { it.category == "Knot" }
    val dyes = materials.filter { it.category == "Barwnik" }

    // Podpowiedzi z preferencji materiałów
    LaunchedEffect(selectedContainer) {
        selectedContainer?.preferredWickName?.let { wickName ->
            wicks.find { it.name == wickName }?.let { selectedWick = it }
        }
    }
    LaunchedEffect(selectedFragrance) {
        selectedFragrance?.preferredConcentration?.let { concentration = it.toString() }
    }

    // Obliczenia kosztu (analogicznie jak w kalkulatorze)
    var totalPrice by remember { mutableStateOf(0.0) }
    LaunchedEffect(concentration, count, selectedContainer, selectedWax, selectedFragrance, selectedWick, wickLength, selectedDye) {
        val containerCapacity = selectedContainer?.let { MaterialUtils.extractCapacity(it.name) } ?: 0.0
        val concentrationValue = concentration.replace(',', '.').toDoubleOrNull() ?: 0.0
        val candleCount = count.toIntOrNull() ?: 1
        val wickLengthValue = wickLength.replace(',', '.').toDoubleOrNull() ?: 0.0
        if (containerCapacity > 0 && selectedWax != null && candleCount > 0) {
            val singleWaxAndFragrance = containerCapacity * 0.875
            val singleFragranceAmount = singleWaxAndFragrance * (concentrationValue / 100.0)
            val singleWaxAmount = singleWaxAndFragrance - singleFragranceAmount
            val containerPrice = MaterialUtils.getPricePerUnit(selectedContainer)
            val fragrancePrice = MaterialUtils.getPricePerUnit(selectedFragrance) * singleFragranceAmount
            val wickPrice = when (selectedWick?.unit) {
                "m" -> MaterialUtils.getPricePerUnit(selectedWick) * wickLengthValue
                "szt" -> MaterialUtils.getPricePerUnit(selectedWick)
                else -> 0.0
            }
            val waxPrice = MaterialUtils.getPricePerUnit(selectedWax) * singleWaxAmount
            val dyePrice = MaterialUtils.getPricePerUnit(selectedDye)
            val singleCandlePrice = containerPrice + fragrancePrice + wickPrice + waxPrice + dyePrice
            totalPrice = singleCandlePrice * candleCount
        } else totalPrice = 0.0
    }

    val textColor = if (darkMode) Color.White else Color.Black

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Dodaj świeczki", fontSize = 20.sp, color = textColor)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = forWhom,
                    onValueChange = { forWhom = it },
                    label = { Text("Dla kogo?") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                // Dropdowny wyboru materiałów (używamy wzorca jak w kalkulatorze)
                var containerExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = containerExpanded, onExpandedChange = { containerExpanded = !containerExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedContainer?.name ?: "",
                        onValueChange = {},
                        label = { Text("Pojemnik") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = containerExpanded) }
                    )
                    ExposedDropdownMenu(expanded = containerExpanded, onDismissRequest = { containerExpanded = false }) {
                        containers.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedContainer = c; containerExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                var waxExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = waxExpanded, onExpandedChange = { waxExpanded = !waxExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedWax?.name ?: "",
                        onValueChange = {},
                        label = { Text("Wosk") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waxExpanded) }
                    )
                    ExposedDropdownMenu(expanded = waxExpanded, onDismissRequest = { waxExpanded = false }) {
                        waxes.forEach { w -> DropdownMenuItem(text = { Text(w.name) }, onClick = { selectedWax = w; waxExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                var fragranceExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = fragranceExpanded, onExpandedChange = { fragranceExpanded = !fragranceExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedFragrance?.name ?: "",
                        onValueChange = {},
                        label = { Text("Olejek") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fragranceExpanded) }
                    )
                    ExposedDropdownMenu(expanded = fragranceExpanded, onDismissRequest = { fragranceExpanded = false }) {
                        fragrances.forEach { f -> DropdownMenuItem(text = { Text(f.name) }, onClick = { selectedFragrance = f; fragranceExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                var wickExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = wickExpanded, onExpandedChange = { wickExpanded = !wickExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedWick?.name ?: "",
                        onValueChange = {},
                        label = { Text("Knot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wickExpanded) }
                    )
                    ExposedDropdownMenu(expanded = wickExpanded, onDismissRequest = { wickExpanded = false }) {
                        wicks.forEach { wk -> DropdownMenuItem(text = { Text(wk.name) }, onClick = { selectedWick = wk; wickExpanded = false }) }
                    }
                }
                if (selectedWick?.unit == "m") {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wickLength,
                        onValueChange = { wickLength = it },
                        label = { Text("Długość knota (m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(8.dp))
                var dyeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = dyeExpanded, onExpandedChange = { dyeExpanded = !dyeExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedDye?.name ?: "",
                        onValueChange = {},
                        label = { Text("Barwnik (opcjonalnie)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dyeExpanded) }
                    )
                    ExposedDropdownMenu(expanded = dyeExpanded, onDismissRequest = { dyeExpanded = false }) {
                        dyes.forEach { dy -> DropdownMenuItem(text = { Text(dy.name) }, onClick = { selectedDye = dy; dyeExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = concentration,
                    onValueChange = { concentration = it },
                    label = { Text("Stężenie zapachu (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it },
                    label = { Text("Ilość świeczek") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                val candleCount = count.toIntOrNull() ?: 1
                val singleCost = if (candleCount > 0) totalPrice / candleCount else 0.0
                Text("Koszt / świeczkę: ${String.format(Locale.getDefault(),"%.2f zł", singleCost)}", color = textColor)
                Spacer(Modifier.height(8.dp))
                Text("Łączny koszt: ${String.format(Locale.getDefault(),"%.2f zł", totalPrice)}", fontWeight = FontWeight.Bold, color = textColor)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = selectedContainer != null && selectedWax != null && selectedFragrance != null && selectedWick != null && (count.toIntOrNull() ?: 0) > 0 && totalPrice > 0.0,
                        onClick = {
                            val capacity = selectedContainer?.let { MaterialUtils.extractCapacity(it.name) } ?: 0.0
                            val now = System.currentTimeMillis()
                            val dateToLight = now + 14L * 24 * 60 * 60 * 1000
                            val list = (1..candleCount).map {
                                Candle(
                                    containerName = selectedContainer?.name,
                                    waxName = selectedWax?.name,
                                    fragranceName = selectedFragrance?.name,
                                    wickName = selectedWick?.name,
                                    dyeName = selectedDye?.name,
                                    concentration = concentration.replace(',', '.').toDoubleOrNull() ?: 0.0,
                                    capacity = capacity,
                                    cost = singleCost,
                                    forWhom = forWhom.ifBlank { "" },
                                    dateMade = now,
                                    dateToLight = dateToLight,
                                    burnTimeMinutes = 0
                                )
                            }
                            candleViewModel.addCandles(list)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                    ) { Text("Zapisz") }
                }
            }
        }
    }
}
