package com.example.swieczkomat.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.vievmodels.CandleViewModel
import com.example.swieczkomat.ui.dialogs.SaveCandlesDialog
import com.example.swieczkomat.ui.util.MaterialUtils
import java.util.Locale
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTab(darkMode: Boolean, viewModel: MaterialsViewModel, candleViewModel: CandleViewModel) {
    val materials by viewModel.getAllMaterials().collectAsState(initial = emptyList())
    val textColor = if (darkMode) Color.White else Color.Black

    var concentration by remember { mutableStateOf("5.0") }
    var candleCount by remember { mutableStateOf("1") }
    var selectedContainer by remember { mutableStateOf<Material?>(null) }
    var selectedWax by remember { mutableStateOf<Material?>(null) }
    var selectedFragrance by remember { mutableStateOf<Material?>(null) }
    var selectedWick by remember { mutableStateOf<Material?>(null) }
    var wickLength by remember { mutableStateOf("0.15") }
    var selectedDye by remember { mutableStateOf<Material?>(null) }

    val containers = materials.filter { it.category == "Pojemnik" }
    val waxes = materials.filter { it.category == "Wosk" }
    val fragrances = materials.filter { it.category == "Olejek" }
    val wicks = materials.filter { it.category == "Knot" }
    val dyes = materials.filter { it.category == "Barwnik" }

    var fragranceAmount by remember { mutableStateOf(0.0) }
    var waxAmount by remember { mutableStateOf(0.0) }
    var totalPrice by remember { mutableStateOf(0.0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Koszty per świeczkę (cząstkowe)
    var containerCostPerCandle by remember { mutableStateOf(0.0) }
    var waxCostPerCandle by remember { mutableStateOf(0.0) }
    var fragranceCostPerCandle by remember { mutableStateOf(0.0) }
    var wickCostPerCandle by remember { mutableStateOf(0.0) }
    var dyeCostPerCandle by remember { mutableStateOf(0.0) }

    // Zużycie (obliczane dynamicznie)
    var containerConsumption by remember { mutableStateOf(0.0) }
    var waxConsumption by remember { mutableStateOf(0.0) }
    var fragranceConsumption by remember { mutableStateOf(0.0) }
    var wickConsumption by remember { mutableStateOf(0.0) }
    var dyeConsumption by remember { mutableStateOf(0.0) }

    LaunchedEffect(selectedContainer) {
        selectedContainer?.preferredWickName?.let { wickName ->
            wicks.find { it.name == wickName }?.let { selectedWick = it }
        }
    }
    LaunchedEffect(selectedFragrance) { selectedFragrance?.preferredConcentration?.let { concentration = it.toString() } }

    LaunchedEffect(concentration, candleCount, selectedContainer, selectedWax, selectedFragrance, selectedWick, wickLength, selectedDye) {
        val count = candleCount.toIntOrNull() ?: 1
        val concentrationValue = concentration.replace(',', '.').toDoubleOrNull() ?: 0.0
        val wickLengthValue = wickLength.replace(',', '.').toDoubleOrNull() ?: 0.0
        val dyeAmountPerCandle = if (selectedDye != null) 0.05 else 0.0

        // Reset zużyć
        containerConsumption = 0.0
        waxConsumption = 0.0
        fragranceConsumption = 0.0
        wickConsumption = 0.0
        dyeConsumption = 0.0
        fragranceAmount = 0.0
        waxAmount = 0.0

        var singleCandlePrice = 0.0

        // Pojemnik – dostępny od razu
        if (selectedContainer != null) {
            val unitCost = MaterialUtils.getPricePerUnit(selectedContainer)
            singleCandlePrice += unitCost
            containerCostPerCandle = unitCost
            containerConsumption = count.toDouble()
        }

        // Wosk + zapach – wymagają pojemnika i wosku (zapach opcjonalny, jeśli brak to 0)
        val containerCapacity = selectedContainer?.let { MaterialUtils.extractCapacity(it.name) } ?: 0.0
        if (selectedContainer != null && selectedWax != null && containerCapacity > 0) {
            // FORMUŁA: potrzebna ilość wosku = pojemność*0.875 - ilość olejku
            // gdzie ilość olejku = pojemność*0.875*stężenie zapachu (stężenie traktowane jako ułamek lub % jeśli >1)
            val fillBase = containerCapacity * 0.875
            val fragranceFraction = if (selectedFragrance != null) {
                // Jeśli użytkownik wpisuje np. 5.0 mamy 5%, więc dzielimy przez 100; jeśli np. 0.05 traktujemy jako ułamek już.
                if (concentrationValue > 1.0) concentrationValue / 100.0 else concentrationValue
            } else 0.0
            val fragranceNeeded = fillBase * fragranceFraction
            val waxNeeded = fillBase - fragranceNeeded

            // Ilości całkowite dla wszystkich świeczek
            fragranceAmount = fragranceNeeded * count
            waxAmount = waxNeeded * count

            // Zużycia
            waxConsumption = waxAmount
            fragranceConsumption = fragranceAmount

            // Koszt wosku: (cena całkowita / ilość całkowita) * potrzebna ilość wosku
            val waxUnitCost = if (selectedWax!!.quantity > 0) selectedWax!!.price / selectedWax!!.quantity else 0.0
            val waxPricePart = waxUnitCost * waxNeeded
            singleCandlePrice += waxPricePart
            waxCostPerCandle = waxPricePart

            // Koszt olejku jeśli jest wybrany
            if (selectedFragrance != null) {
                val fragranceUnitCost = if (selectedFragrance!!.quantity > 0) selectedFragrance!!.price / selectedFragrance!!.quantity else 0.0
                val fragrancePricePart = fragranceUnitCost * fragranceNeeded
                singleCandlePrice += fragrancePricePart
                fragranceCostPerCandle = fragrancePricePart
            }
        }

        // Knot – jeśli wybrany
        if (selectedWick != null) {
            wickConsumption = if (selectedWick!!.unit == "m") wickLengthValue * count else count.toDouble()
            val wickUnitCost = when (selectedWick!!.unit) {
                "m" -> MaterialUtils.getPricePerUnit(selectedWick) * wickLengthValue
                "szt" -> MaterialUtils.getPricePerUnit(selectedWick)
                else -> 0.0
            }
            val wickSurcharge = 0.10 // stała dopłata za knot per świeczka
            singleCandlePrice += (wickUnitCost + wickSurcharge)
            wickCostPerCandle = wickUnitCost + wickSurcharge
        }

        // Barwnik – stała ilość na świeczkę jeśli wybrany
        if (selectedDye != null) {
            dyeConsumption = dyeAmountPerCandle * count
            val dyeCostPart = MaterialUtils.getPricePerUnit(selectedDye) * dyeAmountPerCandle
            singleCandlePrice += dyeCostPart
            dyeCostPerCandle = dyeCostPart
        }

        totalPrice = singleCandlePrice * count
    }

    val fieldContainerColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val fieldTextColor = if (darkMode) Color.White else Color.Black
    val fieldLabelColor = if (darkMode) Color(0xFFCBD5E1) else Color(0xFF374151)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            "Kalkulator Świeczek",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = concentration,
            onValueChange = { concentration = it },
            label = { Text("Stężenie zapachu (%)", color = fieldLabelColor) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
        Spacer(Modifier.height(8.dp))

        var containerExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = containerExpanded,
            onExpandedChange = { containerExpanded = !containerExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedContainer?.name ?: "",
                onValueChange = {},
                label = { Text("Pojemnik", color = fieldLabelColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = containerExpanded) },
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
            ExposedDropdownMenu(
                expanded = containerExpanded,
                onDismissRequest = { containerExpanded = false }
            ) {
                containers.forEach { container ->
                    DropdownMenuItem(
                        text = { Text(container.name) },
                        onClick = {
                            selectedContainer = container
                            containerExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        var waxExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = waxExpanded,
            onExpandedChange = { waxExpanded = !waxExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedWax?.name ?: "",
                onValueChange = {},
                label = { Text("Wosk", color = fieldLabelColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waxExpanded) },
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
            ExposedDropdownMenu(
                expanded = waxExpanded,
                onDismissRequest = { waxExpanded = false }
            ) {
                waxes.forEach { wax ->
                    DropdownMenuItem(
                        text = { Text(wax.name) },
                        onClick = {
                            selectedWax = wax
                            waxExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        var fragranceExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = fragranceExpanded,
            onExpandedChange = { fragranceExpanded = !fragranceExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedFragrance?.name ?: "",
                onValueChange = {},
                label = { Text("Olejek", color = fieldLabelColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fragranceExpanded) },
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
            ExposedDropdownMenu(
                expanded = fragranceExpanded,
                onDismissRequest = { fragranceExpanded = false }
            ) {
                fragrances.forEach { fr ->
                    DropdownMenuItem(
                        text = { Text(fr.name) },
                        onClick = {
                            selectedFragrance = fr
                            fragranceExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        var wickExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = wickExpanded,
            onExpandedChange = { wickExpanded = !wickExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedWick?.name ?: "",
                onValueChange = {},
                label = { Text("Knot", color = fieldLabelColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wickExpanded) },
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
            ExposedDropdownMenu(
                expanded = wickExpanded,
                onDismissRequest = { wickExpanded = false }
            ) {
                wicks.forEach { wk ->
                    DropdownMenuItem(
                        text = { Text(wk.name) },
                        onClick = {
                            selectedWick = wk
                            wickExpanded = false
                        }
                    )
                }
            }
        }
        if (selectedWick?.unit == "m") {
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = wickLength,
                onValueChange = { wickLength = it },
                label = { Text("Długość knota (m)", color = fieldLabelColor) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
        }
        Spacer(Modifier.height(8.dp))

        var dyeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = dyeExpanded,
            onExpandedChange = { dyeExpanded = !dyeExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedDye?.name ?: "",
                onValueChange = {},
                label = { Text("Barwnik (opcjonalnie)", color = fieldLabelColor) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dyeExpanded) },
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
            ExposedDropdownMenu(
                expanded = dyeExpanded,
                onDismissRequest = { dyeExpanded = false }
            ) {
                dyes.forEach { dy ->
                    DropdownMenuItem(
                        text = { Text(dy.name) },
                        onClick = {
                            selectedDye = dy
                            dyeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = candleCount,
            onValueChange = { candleCount = it },
            label = { Text("Ilość świeczek", color = fieldLabelColor) },
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
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.align(Alignment.Start)) {
            Text(
                "Ilość zapachu: ${String.format(Locale.getDefault(), "%.2f", fragranceAmount)} g/ml",
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                "Ilość wosku: ${String.format(Locale.getDefault(), "%.2f", waxAmount)} g/ml",
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Spacer(Modifier.height(4.dp))
            val count = candleCount.toIntOrNull() ?: 1
            val priceText = if (count > 1) "Szacowana cena za $count świeczek" else "Szacowana cena świeczki"
            Text(
                "$priceText: ${String.format(Locale.getDefault(), "%.2f", totalPrice)} zł",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            // Walidacja dostępnych ilości materiałów
            val c = selectedContainer
            val w = selectedWax
            val f = selectedFragrance
            val k = selectedWick
            val d = selectedDye
            val insufficientMessages = buildList {
                if (c != null && c.quantity < containerConsumption) add("Brak wystarczającej ilości pojemników (${String.format(Locale.getDefault(),"%.0f", c.quantity)} < ${containerConsumption.toInt()})")
                if (w != null && w.quantity < waxConsumption) add("Za mało wosku (${String.format(Locale.getDefault(),"%.2f", w.quantity)} < ${String.format(Locale.getDefault(),"%.2f", waxConsumption)})")
                if (f != null && f.quantity < fragranceConsumption) add("Za mało olejku (${String.format(Locale.getDefault(),"%.2f", f.quantity)} < ${String.format(Locale.getDefault(),"%.2f", fragranceConsumption)})")
                if (k != null && k.quantity < wickConsumption) add("Za mało knotów${if (k.unit == "m") " (m)" else " (szt.)"}")
                if (d != null && d.quantity < dyeConsumption) add("Za mało barwnika (potrzeba ${String.format(Locale.getDefault(),"%.2f", dyeConsumption)} ${d.unit})")
            }
            if (insufficientMessages.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                insufficientMessages.forEach { msg -> Text(msg, color = Color.Red, fontSize = 12.sp) }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                concentration = "5.0"
                candleCount = "1"
                selectedContainer = null
                selectedWax = null
                selectedFragrance = null
                selectedWick = null
                wickLength = "0.15"
                selectedDye = null
            }) { Text("Wyczyść") }
            Button(
                enabled = selectedContainer != null && selectedWax != null && selectedFragrance != null && selectedWick != null && (candleCount.toIntOrNull() ?: 0) > 0 && totalPrice > 0.0 &&
                        (selectedContainer?.quantity ?: 0.0) >= containerConsumption &&
                        (selectedWax?.quantity ?: 0.0) >= waxConsumption &&
                        (selectedFragrance?.quantity ?: 0.0) >= fragranceConsumption &&
                        (selectedWick?.quantity ?: 0.0) >= wickConsumption &&
                        (selectedDye?.quantity ?: Double.MAX_VALUE) >= dyeConsumption,
                onClick = { showSaveDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
            ) { Text("Zapisz świeczki") }
            OutlinedButton(
                enabled = (candleCount.toIntOrNull() ?: 0) > 0 && (containerCostPerCandle + waxCostPerCandle + fragranceCostPerCandle + wickCostPerCandle + dyeCostPerCandle) > 0.0,
                onClick = { showDetailsDialog = true }
            ) { Text("Szczegóły") }
        }
        Spacer(Modifier.height(16.dp))
    }
    if (showSaveDialog) {
        SaveCandlesDialog(
            count = candleCount.toIntOrNull() ?: 1,
            totalPrice = totalPrice,
            container = selectedContainer,
            wax = selectedWax,
            fragrance = selectedFragrance,
            wick = selectedWick,
            dye = selectedDye,
            concentration = concentration.replace(',', '.').toDoubleOrNull() ?: 0.0,
            darkMode = darkMode,
            onDismiss = { showSaveDialog = false },
            onSave = { list ->
                // Odjęcie zużytych materiałów z magazynu
                selectedContainer?.let { viewModel.removeMaterial(it, containerConsumption) }
                selectedWax?.let { viewModel.removeMaterial(it, waxConsumption) }
                selectedFragrance?.let { viewModel.removeMaterial(it, fragranceConsumption) }
                selectedWick?.let { viewModel.removeMaterial(it, wickConsumption) }
                if (selectedDye != null && dyeConsumption > 0) viewModel.removeMaterial(selectedDye!!, dyeConsumption)
                candleViewModel.addCandles(list)
                showSaveDialog = false
            }
        )
    }
    if (showDetailsDialog) {
        DetailsCostDialog(
            darkMode = darkMode,
            count = candleCount.toIntOrNull() ?: 1,
            container = selectedContainer?.name,
            wax = selectedWax?.name,
            fragrance = selectedFragrance?.name,
            wick = selectedWick?.name,
            dye = selectedDye?.name,
            containerCost = containerCostPerCandle,
            waxCost = waxCostPerCandle,
            fragranceCost = fragranceCostPerCandle,
            wickCost = wickCostPerCandle,
            dyeCost = dyeCostPerCandle,
            totalSingle = containerCostPerCandle + waxCostPerCandle + fragranceCostPerCandle + wickCostPerCandle + dyeCostPerCandle,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
private fun DetailsCostDialog(
    darkMode: Boolean,
    count: Int,
    container: String?,
    wax: String?,
    fragrance: String?,
    wick: String?,
    dye: String?,
    containerCost: Double,
    waxCost: Double,
    fragranceCost: Double,
    wickCost: Double,
    dyeCost: Double,
    totalSingle: Double,
    onDismiss: () -> Unit
) {
    val bgColor = if (darkMode) Color(0xFF3B3B44) else Color.White
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = bgColor)) {
            Column(modifier = Modifier.padding(16.dp).widthIn(min = 260.dp)) {
                Text("Rozbicie kosztów", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(12.dp))
                CostLine("Pojemnik", container, containerCost, count, darkMode)
                CostLine("Wosk", wax, waxCost, count, darkMode)
                CostLine("Olejek", fragrance, fragranceCost, count, darkMode)
                CostLine("Knot", wick, wickCost, count, darkMode)
                CostLine("Barwnik", dye, dyeCost, count, darkMode)
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("Suma / świeczkę: ${String.format(Locale.getDefault(), "%.2f zł", totalSingle)}", fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
                Text("Suma / ${count} świeczek: ${String.format(Locale.getDefault(), "%.2f zł", totalSingle * count)}", fontWeight = FontWeight.Bold, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Zamknij") }
                }
            }
        }
    }
}

@Composable
private fun CostLine(label: String, name: String?, perCandle: Double, count: Int, darkMode: Boolean) {
    val textColor = if (darkMode) Color.White else Color.Black
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.Medium, color = textColor)
            Text(name ?: "-", fontSize = 12.sp, color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(String.format(Locale.getDefault(), "%.2f zł", perCandle), fontSize = 12.sp, color = textColor)
            Text(String.format(Locale.getDefault(), "%.2f zł", perCandle * count), fontSize = 12.sp, color = textColor)
        }
    }
    Spacer(Modifier.height(6.dp))
}
