package com.example.swieczkomat.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.swieczkomat.data.Material
import java.util.Locale

private fun parseNumber(raw: String): Double? = raw.replace(',', '.').trim().toDoubleOrNull()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAddMaterial: (Material) -> Unit,
    darkMode: Boolean,
    existingMaterials: List<Material>
) {
    var materialName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var capacityValue by remember { mutableStateOf("") }
    val capacityUnits = listOf("ml", "g")
    var capacityUnit by remember { mutableStateOf(capacityUnits.first()) }
    var preferredWickName by remember { mutableStateOf("") }
    var preferredConcentration by remember { mutableStateOf("") }

    val unitsBase = listOf("g", "ml", "szt", "m")
    var unit by remember { mutableStateOf("g") }
    val categories = listOf("Wosk", "Olejek", "Knot", "Pojemnik", "Barwnik", "Inne")
    var category by remember { mutableStateOf(categories.first()) }

    var nameExpanded by remember { mutableStateOf(false) }
    val allMaterialNames = existingMaterials.map { it.name }.distinct()
    val filteredMaterialNames = if (materialName.isBlank()) allMaterialNames else allMaterialNames.filter { it.contains(materialName, ignoreCase = true) }

    val wicks = existingMaterials.filter { it.category == "Knot" }.map { it.name }

    LaunchedEffect(category) {
        unit = when (category) {
            "Pojemnik" -> "szt"
            "Knot" -> "szt"
            "Olejek" -> "ml"
            else -> "g"
        }
    }

    val fieldContainerColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val fieldTextColor = if (darkMode) Color.White else Color.Black
    val fieldLabelColor = if (darkMode) Color(0xFFCBD5E1) else Color(0xFF374151)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Dodaj nowy materiał", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(16.dp))

                // Nazwa + podpowiedzi
                ExposedDropdownMenuBox(expanded = nameExpanded, onExpandedChange = { nameExpanded = !nameExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = materialName,
                        onValueChange = {
                            materialName = it
                            nameExpanded = true // otwórz menu przy wpisywaniu
                        },
                        label = { Text("Nazwa materiału", color = fieldLabelColor) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameExpanded) },
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
                    if (nameExpanded && filteredMaterialNames.isNotEmpty()) {
                        ExposedDropdownMenu(expanded = nameExpanded, onDismissRequest = { nameExpanded = false }) {
                            filteredMaterialNames.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    val selected = existingMaterials.find { it.name == option }
                                    if (selected != null) {
                                        category = selected.category
                                        unit = selected.unit
                                        preferredWickName = selected.preferredWickName ?: ""
                                        preferredConcentration = selected.preferredConcentration?.toString() ?: ""
                                        if (selected.category == "Pojemnik") {
                                            // wyodrębnij pojemność i jednostkę
                                            val lastSpace = selected.name.lastIndexOf(' ')
                                            if (lastSpace != -1) {
                                                materialName = selected.name.substring(0, lastSpace)
                                                val tail = selected.name.substring(lastSpace + 1)
                                                val value = tail.filter { it.isDigit() }
                                                val unitStr = tail.filter { it.isLetter() }
                                                capacityValue = value
                                                if (unitStr in capacityUnits) capacityUnit = unitStr
                                            } else materialName = selected.name
                                        } else materialName = selected.name
                                    } else materialName = option
                                    nameExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Kategoria
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false }) }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Dynamiczne pola zależne od kategorii
                when (category) {
                    "Pojemnik" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = capacityValue,
                                onValueChange = { capacityValue = it },
                                label = { Text("Pojemność", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            var capUnitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = capUnitExpanded, onExpandedChange = { capUnitExpanded = !capUnitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = capacityUnit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = capUnitExpanded) },
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
                                ExposedDropdownMenu(expanded = capUnitExpanded, onDismissRequest = { capUnitExpanded = false }) {
                                    capacityUnits.forEach { cu -> DropdownMenuItem(text = { Text(cu) }, onClick = { capacityUnit = cu; capUnitExpanded = false }) }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Ilość (szt.)", color = fieldLabelColor) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        // Preferowany knot
                        var wickExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = wickExpanded, onExpandedChange = { wickExpanded = !wickExpanded }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                readOnly = true,
                                value = preferredWickName,
                                onValueChange = {},
                                label = { Text("Preferowany knot", color = fieldLabelColor) },
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
                            ExposedDropdownMenu(expanded = wickExpanded, onDismissRequest = { wickExpanded = false }) {
                                wicks.forEach { w -> DropdownMenuItem(text = { Text(w) }, onClick = { preferredWickName = w; wickExpanded = false }) }
                            }
                        }
                    }
                    "Olejek" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Ilość", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            // Jednostka dla olejku (ml/g) uproszczenie
                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = unit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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
                                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                    listOf("ml", "g").forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = preferredConcentration,
                            onValueChange = { preferredConcentration = it },
                            label = { Text("Preferowane stężenie (%)", color = fieldLabelColor) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Ilość", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = unit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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
                                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                    (if (category == "Knot") listOf("szt", "m") else unitsBase).forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                var price by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena całkowita (zł)", color = fieldLabelColor) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val finalName = if (category == "Pojemnik") {
                            "${materialName.trim()} ${capacityValue.trim()}${capacityUnit}".trim()
                        } else materialName.trim()
                        val finalUnit = if (category == "Pojemnik") "szt" else unit
                        val newMaterial = Material(
                            name = finalName,
                            quantity = parseNumber(quantity) ?: 0.0,
                            unit = finalUnit,
                            price = parseNumber(price) ?: 0.0,
                            category = category,
                            preferredWickName = if (category == "Pojemnik") preferredWickName.takeIf { it.isNotBlank() } else null,
                            preferredConcentration = if (category == "Olejek") parseNumber(preferredConcentration) else null
                        )
                        onAddMaterial(newMaterial)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { Text("Dodaj") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialDialog(
    material: Material,
    onDismiss: () -> Unit,
    onEditMaterial: (Material) -> Unit,
    darkMode: Boolean,
    existingMaterials: List<Material>
) {
    var materialName by remember { mutableStateOf(material.name) }
    var quantity by remember { mutableStateOf(material.quantity.toString()) }
    var capacityValue by remember { mutableStateOf("") }
    val capacityUnits = listOf("ml", "g")
    var capacityUnit by remember { mutableStateOf(capacityUnits.first()) }
    var preferredWickName by remember { mutableStateOf(material.preferredWickName ?: "") }
    var preferredConcentration by remember { mutableStateOf(material.preferredConcentration?.toString() ?: "") }
    var unit by remember { mutableStateOf(material.unit) }
    val categories = listOf("Wosk", "Olejek", "Knot", "Pojemnik", "Barwnik", "Inne")
    var category by remember { mutableStateOf(material.category) }

    LaunchedEffect(material) {
        if (material.category == "Pojemnik") {
            val lastSpace = material.name.lastIndexOf(' ')
            if (lastSpace != -1) {
                materialName = material.name.substring(0, lastSpace)
                val tail = material.name.substring(lastSpace + 1)
                val value = tail.filter { it.isDigit() }
                val unitStr = tail.filter { it.isLetter() }
                capacityValue = value
                if (unitStr in capacityUnits) capacityUnit = unitStr
            }
        }
    }

    val unitsBase = listOf("g", "ml", "szt", "m")

    val fieldContainerColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val fieldTextColor = if (darkMode) Color.White else Color.Black
    val fieldLabelColor = if (darkMode) Color(0xFFCBD5E1) else Color(0xFF374151)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("Edytuj materiał", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = materialName,
                    onValueChange = { materialName = it },
                    label = { Text("Nazwa materiału", color = fieldLabelColor) },
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

                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { category = option; categoryExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))

                when (category) {
                    "Pojemnik" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = capacityValue,
                                onValueChange = { capacityValue = it },
                                label = { Text("Pojemność", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            var capUnitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = capUnitExpanded, onExpandedChange = { capUnitExpanded = !capUnitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = capacityUnit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = capUnitExpanded) },
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
                                ExposedDropdownMenu(expanded = capUnitExpanded, onDismissRequest = { capUnitExpanded = false }) {
                                    capacityUnits.forEach { cu -> DropdownMenuItem(text = { Text(cu) }, onClick = { capacityUnit = cu; capUnitExpanded = false }) }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Ilość (szt.)", color = fieldLabelColor) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        var wickExpanded by remember { mutableStateOf(false) }
                        val wicks = existingMaterials.filter { it.category == "Knot" }.map { it.name }
                        ExposedDropdownMenuBox(expanded = wickExpanded, onExpandedChange = { wickExpanded = !wickExpanded }) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                readOnly = true,
                                value = preferredWickName,
                                onValueChange = {},
                                label = { Text("Preferowany knot", color = fieldLabelColor) },
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
                            ExposedDropdownMenu(expanded = wickExpanded, onDismissRequest = { wickExpanded = false }) {
                                wicks.forEach { w -> DropdownMenuItem(text = { Text(w) }, onClick = { preferredWickName = w; wickExpanded = false }) }
                            }
                        }
                    }
                    "Olejek" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Ilość", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = unit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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
                                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                    listOf("ml", "g").forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = preferredConcentration,
                            onValueChange = { preferredConcentration = it },
                            label = { Text("Preferowane stężenie (%)", color = fieldLabelColor) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                    else -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Ilość", color = fieldLabelColor) },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            Spacer(Modifier.width(8.dp))
                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                                OutlinedTextField(
                                    modifier = Modifier.menuAnchor().width(90.dp),
                                    readOnly = true,
                                    value = unit,
                                    onValueChange = {},
                                    label = { Text("Jedn.", color = fieldLabelColor) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
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
                                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                    (if (category == "Knot") listOf("szt", "m") else unitsBase).forEach { u -> DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false }) }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                var price by remember { mutableStateOf(material.price.toString()) }
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena całkowita (zł)", color = fieldLabelColor) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val finalName = if (category == "Pojemnik") {
                            "${materialName.trim()} ${capacityValue.trim()}${capacityUnit}".trim()
                        } else materialName.trim()
                        val finalUnit = if (category == "Pojemnik") "szt" else unit
                        val updated = material.copy(
                            name = finalName,
                            quantity = parseNumber(quantity) ?: 0.0,
                            unit = finalUnit,
                            price = parseNumber(price) ?: 0.0,
                            category = category,
                            preferredWickName = if (category == "Pojemnik") preferredWickName.takeIf { it.isNotBlank() } else null,
                            preferredConcentration = if (category == "Olejek") parseNumber(preferredConcentration) else null
                        )
                        onEditMaterial(updated)
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) { Text("Zapisz") }
                }
            }
        }
    }
}

@Composable
fun RemoveMaterialDialog(
    material: Material,
    onDismiss: () -> Unit,
    onRemove: (Material, Double) -> Unit,
    darkMode: Boolean
) {
    var quantityToRemove by remember { mutableStateOf("") }
    val fieldContainerColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val fieldTextColor = if (darkMode) Color.White else Color.Black
    val fieldLabelColor = if (darkMode) Color(0xFFCBD5E1) else Color(0xFF374151)
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                Text("Usuń materiał", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
                Spacer(Modifier.height(16.dp))
                Text(String.format(Locale.getDefault(), "Dostępna ilość: %.2f %s", material.quantity, material.unit), fontSize = 14.sp, color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityToRemove,
                    onValueChange = { quantityToRemove = it },
                    label = { Text("Ilość do usunięcia", color = fieldLabelColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                TextButton(onClick = { quantityToRemove = String.format(Locale.getDefault(), "%.2f", material.quantity) }) { Text("Wszystko") }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) { Text("Anuluj") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onRemove(material, parseNumber(quantityToRemove) ?: 0.0) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))) { Text("Usuń") }
                }
            }
        }
    }
}
