package com.example.swieczkomat

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.ui.theme.SwieczkomatTheme
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.vievmodels.MaterialsViewModelFactory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = LocalContext.current.applicationContext as Application
            val materialsViewModel: MaterialsViewModel = viewModel(
                factory = MaterialsViewModelFactory(application)
            )
            val systemDark = isSystemInDarkTheme()
            var darkModeEnabled by remember { mutableStateOf(systemDark) }
            SwieczkomatTheme(darkTheme = darkModeEnabled) {
                CandleManagerScreen(
                    darkMode = darkModeEnabled,
                    onToggleDarkMode = { darkModeEnabled = !darkModeEnabled },
                    materialsViewModel = materialsViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CandleManagerScreen(
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    materialsViewModel: MaterialsViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Materiały", "Świeczki", "Kalkulator", "Ustawienia")
    val tabIcons = listOf(
        Icons.Filled.ShoppingCart,
        Icons.Filled.Info,
        Icons.Filled.Delete,
        Icons.Filled.Settings
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size }, initialPage = selectedTab)
    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab) pagerState.scrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        if (selectedTab != pagerState.currentPage) selectedTab = pagerState.currentPage
    }

    val bgGradientColors = if (darkMode) listOf(Color(0xFF1F1B2E), Color(0xFF2A2438)) else listOf(Color(0xFFF3E8FF), Color(0xFFFDF2F8))
    val headerGradientColors = if (darkMode) listOf(Color(0xFF4B2E83), Color(0xFF7A1F52)) else listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
    val tabsBackgroundColor = if (darkMode) Color(0xFF2F2F35) else Color(0xFFF3F4F6)
    val inactiveTabColor = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val activeLightLabelColor = Color(0xFF1F2937) // ciemny tekst dla aktywnej zakładki w trybie jasnym
    val activeLightIconColor = Color(0xFF7C3AED)  // fioletowy dla ikony aktywnej w jasnym trybie

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = bgGradientColors))
    ) {
        // Header (bez przełącznika trybu)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(colors = headerGradientColors)
                )
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Świeczkomat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Zarządzaj swoimi świeczkami", fontSize = 14.sp, color = Color(0xFFE9D5FF))
            }
        }
        // Zawartość
        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> MaterialsTab(darkMode, materialsViewModel)
                    1 -> CandlesTab(darkMode)
                    2 -> CalculatorTab(darkMode, materialsViewModel)
                    3 -> SettingsTab(darkMode, onToggleDarkMode)
                }
            }
        }
        // Zastąpiono ręczny Row paskiem nawigacji NavigationBar
        NavigationBar(
            containerColor = tabsBackgroundColor,
            tonalElevation = 0.dp,
            modifier = Modifier
                .navigationBarsPadding()
        ) {
            tabTitles.forEachIndexed { i, title ->
                val isActive = selectedTab == i
                val labelColor = if (isActive) {
                    if (darkMode) Color.White else activeLightLabelColor
                } else inactiveTabColor
                val iconColor = if (isActive) {
                    if (darkMode) Color.White else activeLightIconColor
                } else inactiveTabColor
                NavigationBarItem(
                    selected = isActive,
                    onClick = { selectedTab = i },
                    icon = {
                        Icon(
                            tabIcons[i],
                            contentDescription = title,
                            tint = iconColor
                        )
                    },
                    label = {
                        Text(
                            title,
                            color = labelColor,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = iconColor,
                        selectedTextColor = labelColor,
                        indicatorColor = if (darkMode) Color(0xFF667EEA) else Color(0xFFDDE6FF),
                        unselectedIconColor = inactiveTabColor,
                        unselectedTextColor = inactiveTabColor
                    )
                )
            }
        }
    }
}

@Composable
fun MaterialsTab(darkMode: Boolean, viewModel: MaterialsViewModel) {
    val materials by viewModel.getAllMaterials().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf<Material?>(null) }
    val topSpacing = 12.dp

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj materiał")
            }
        }
    ) { /* usunięto .padding(it) aby nie różnicować górnego odstępu */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = topSpacing)
        ) {
            Text(
                "Lista materiałów",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (darkMode) Color.White else Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(materials) { material ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${material.name} (${material.category})")
                                Text(String.format("Ilość: %.2f %s", material.quantity, material.unit), fontSize = 12.sp)
                                material.preferredWickName?.let {
                                    Text("Knot: $it", fontSize = 10.sp)
                                }
                                material.preferredConcentration?.let {
                                    Text("Stężenie: $it%", fontSize = 10.sp)
                                }
                            }
                            val pricePerUnit = if (material.quantity > 0) material.price / material.quantity else 0.0
                            Text(String.format("%.2f zł / %s", pricePerUnit, material.unit))
                            IconButton(onClick = { showRemoveDialog = material }) {
                                Icon(Icons.Default.Delete, contentDescription = "Usuń materiał", tint = if (darkMode) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddMaterialDialog(
                onDismiss = { showAddDialog = false },
                onAddMaterial = {
                    viewModel.addOrUpdateMaterial(it)
                    showAddDialog = false
                },
                darkMode = darkMode,
                existingMaterials = materials
            )
        }

        showRemoveDialog?.let {
            RemoveMaterialDialog(
                material = it,
                onDismiss = { showRemoveDialog = null },
                onRemove = { material, quantity ->
                    viewModel.removeMaterial(material, quantity)
                    showRemoveDialog = null
                },
                darkMode = darkMode
            )
        }
    }
}


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

    val units = listOf("g", "ml", "szt", "m")
    var unit by remember { mutableStateOf(units.first()) }
    var price by remember { mutableStateOf("") }
    val categories = listOf("Wosk", "Olejek", "Knot", "Pojemnik", "Barwnik", "Inne")
    var category by remember { mutableStateOf(categories.first()) }
    var nameExpanded by remember { mutableStateOf(false) }

    val filteredMaterialNames = existingMaterials
        .map { it.name }
        .distinct()
        .filter { it.contains(materialName, ignoreCase = true) }
    
    val wicks = existingMaterials.filter { it.category == "Knot" }.map { it.name }

    LaunchedEffect(category) {
        unit = when (category) {
            "Pojemnik" -> "szt"
            "Knot" -> "szt"
            else -> "g"
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Dodaj nowy materiał",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (darkMode) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = nameExpanded,
                    onExpandedChange = { nameExpanded = !nameExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = materialName,
                        onValueChange = {
                            materialName = it
                            nameExpanded = true
                        },
                        label = { Text("Nazwa materiału") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nameExpanded) },
                    )
                    if (filteredMaterialNames.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = nameExpanded,
                            onDismissRequest = { nameExpanded = false }
                        ) {
                            filteredMaterialNames.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        val selectedMaterial = existingMaterials.find { it.name == selectionOption }
                                        if (selectedMaterial != null) {
                                            category = selectedMaterial.category
                                            unit = selectedMaterial.unit
                                            preferredWickName = selectedMaterial.preferredWickName ?: ""
                                            preferredConcentration = selectedMaterial.preferredConcentration?.toString() ?: ""
                                            if (selectedMaterial.category == "Pojemnik") {
                                                val lastSpaceIndex = selectedMaterial.name.lastIndexOf(' ')
                                                if (lastSpaceIndex != -1) {
                                                    materialName = selectedMaterial.name.substring(0, lastSpaceIndex)
                                                    val capacityAndUnit = selectedMaterial.name.substring(lastSpaceIndex + 1)
                                                    val value = capacityAndUnit.filter { it.isDigit() }
                                                    val capacityUnitString = capacityAndUnit.filter { it.isLetter() }
                                                    capacityValue = value
                                                    if (capacityUnitString in capacityUnits) capacityUnit = capacityUnitString
                                                } else {
                                                    materialName = selectedMaterial.name
                                                }
                                            } else {
                                                materialName = selectedMaterial.name
                                            }
                                        } else {
                                            materialName = selectionOption
                                        }
                                        nameExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (category == "Pojemnik") {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = capacityValue,
                            onValueChange = { capacityValue = it },
                            label = { Text("Pojemność") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        var capacityUnitExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = capacityUnitExpanded,
                            onExpandedChange = { capacityUnitExpanded = !capacityUnitExpanded },
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().width(100.dp),
                                readOnly = true,
                                value = capacityUnit,
                                onValueChange = { },
                                label = { Text("Jedn.") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = capacityUnitExpanded) },
                            )
                            ExposedDropdownMenu(
                                expanded = capacityUnitExpanded,
                                onDismissRequest = { capacityUnitExpanded = false }
                            ) {
                                capacityUnits.forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            capacityUnit = selectionOption
                                            capacityUnitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Ilość (szt.)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var wickExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = wickExpanded, onExpandedChange = { wickExpanded = !wickExpanded }) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = preferredWickName,
                            onValueChange = {},
                            label = { Text("Preferowany knot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wickExpanded) },
                        )
                        ExposedDropdownMenu(expanded = wickExpanded, onDismissRequest = { wickExpanded = false }) {
                            wicks.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        preferredWickName = selectionOption
                                        wickExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else if (category == "Olejek") {
                    OutlinedTextField(
                        value = preferredConcentration,
                        onValueChange = { preferredConcentration = it },
                        label = { Text("Preferowane stężenie (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            label = { Text("Ilość") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        var unitExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded },
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.menuAnchor().width(100.dp),
                                readOnly = true,
                                value = unit,
                                onValueChange = { },
                                label = { Text("Jedn.") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                (if (category == "Knot") listOf("szt", "m") else units).forEach { selectionOption ->
                                    DropdownMenuItem(
                                        text = { Text(selectionOption) },
                                        onClick = {
                                            unit = selectionOption
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena całkowita (zł)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalName = if (category == "Pojemnik") {
                                "${materialName.trim()} ${capacityValue.trim()}${capacityUnit}".trim()
                            } else {
                                materialName.trim()
                            }
                            val finalUnit = if (category == "Pojemnik") "szt" else unit

                            val newMaterial = Material(
                                name = finalName,
                                quantity = quantity.toDoubleOrNull() ?: 0.0,
                                unit = finalUnit,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = category,
                                preferredWickName = if (category == "Pojemnik") preferredWickName else null,
                                preferredConcentration = if (category == "Olejek") preferredConcentration.toDoubleOrNull() else null
                            )
                            onAddMaterial(newMaterial)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED)
                        )
                    ) {
                        Text("Dodaj")
                    }
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (darkMode) Color(0xFF3B3B44) else Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Usuń materiał", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (darkMode) Color.White else Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Text(String.format("Dostępna ilość: %.2f %s", material.quantity, material.unit), fontSize = 14.sp, color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantityToRemove,
                    onValueChange = { quantityToRemove = it },
                    label = { Text("Ilość do usunięcia") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { quantityToRemove = String.format("%.2f", material.quantity) }) {
                    Text("Wszystko")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val quantity = quantityToRemove.toDoubleOrNull() ?: 0.0
                            onRemove(material, quantity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
                    ) {
                        Text("Usuń")
                    }
                }
            }
        }
    }
}



@Composable
fun CandlesTab(darkMode: Boolean) {
    val topSpacing = 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, top = topSpacing),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Świeczki - wkrótce", color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorTab(darkMode: Boolean, viewModel: MaterialsViewModel) {
    val materials by viewModel.getAllMaterials().collectAsState(initial = emptyList())
    val textColor = if (darkMode) Color.White else Color.Black

    var concentration by remember { mutableStateOf("5.0") }
    var candleCount by remember { mutableStateOf("1") }
    var selectedContainer by remember { mutableStateOf<Material?>(null) }
    var selectedWax by remember { mutableStateOf<Material?>(null) }
    var selectedFragrance by remember { mutableStateOf<Material?>(null) }
    var selectedWick by remember { mutableStateOf<Material?>(null) }
    var wickLength by remember { mutableStateOf("0.15") } // Domyślna długość knota w metrach
    var selectedDye by remember { mutableStateOf<Material?>(null) }

    val containers = materials.filter { it.category == "Pojemnik" }
    val waxes = materials.filter { it.category == "Wosk" }
    val fragrances = materials.filter { it.category == "Olejek" }
    val wicks = materials.filter { it.category == "Knot" }
    val dyes = materials.filter { it.category == "Barwnik" }

    var fragranceAmount by remember { mutableStateOf(0.0) }
    var waxAmount by remember { mutableStateOf(0.0) }
    var totalPrice by remember { mutableStateOf(0.0) }

    fun extractCapacity(name: String): Double {
        val regex = "([0-9.]+)".toRegex()
        val match = regex.find(name)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    fun getPricePerUnit(material: Material?): Double {
        return material?.let { if (it.quantity > 0) it.price / it.quantity else 0.0 } ?: 0.0
    }

    LaunchedEffect(selectedContainer) {
        selectedContainer?.preferredWickName?.let { wickName ->
            wicks.find { it.name == wickName }?.let { 
                selectedWick = it
            }
        }
    }

    LaunchedEffect(selectedFragrance) {
        selectedFragrance?.preferredConcentration?.let {
            concentration = it.toString()
        }
    }

    LaunchedEffect(concentration, candleCount, selectedContainer, selectedWax, selectedFragrance, selectedWick, wickLength, selectedDye) {
        val containerCapacity = selectedContainer?.let { extractCapacity(it.name) } ?: 0.0
        val concentrationValue = concentration.replace(',', '.').toDoubleOrNull() ?: 0.0
        val count = candleCount.toIntOrNull() ?: 1
        val wickLengthValue = wickLength.replace(',', '.').toDoubleOrNull() ?: 0.0

        if (containerCapacity > 0 && selectedWax != null && count > 0) {
            val singleWaxAndFragrance = containerCapacity * 0.875
            val singleFragranceAmount = singleWaxAndFragrance * (concentrationValue / 100.0)
            val singleWaxAmount = singleWaxAndFragrance - singleFragranceAmount

            val containerPrice = getPricePerUnit(selectedContainer)
            val fragrancePrice = getPricePerUnit(selectedFragrance) * singleFragranceAmount
            val wickPrice = when (selectedWick?.unit) {
                "m" -> getPricePerUnit(selectedWick) * wickLengthValue
                "szt" -> getPricePerUnit(selectedWick)
                else -> 0.0
            }
            val waxPrice = getPricePerUnit(selectedWax) * singleWaxAmount
            val dyePrice = getPricePerUnit(selectedDye) // Assuming a fixed amount of dye per candle, but price per unit is needed

            val singleCandlePrice = containerPrice + fragrancePrice + wickPrice + waxPrice + dyePrice

            fragranceAmount = singleFragranceAmount * count
            waxAmount = singleWaxAmount * count
            totalPrice = singleCandlePrice * count
        } else {
            fragranceAmount = 0.0
            waxAmount = 0.0
            totalPrice = 0.0
        }
    }

    val topSpacing = 12.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = topSpacing)
    ) {
        Text("Kalkulator Świeczek", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = concentration,
            onValueChange = { concentration = it },
            label = { Text("Stężenie zapachu (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        var containerExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = containerExpanded, onExpandedChange = { containerExpanded = !containerExpanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedContainer?.name ?: "",
                onValueChange = {},
                label = { Text("Pojemnik") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = containerExpanded) },
            )
            ExposedDropdownMenu(expanded = containerExpanded, onDismissRequest = { containerExpanded = false }) {
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
        Spacer(modifier = Modifier.height(4.dp))

        var waxExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = waxExpanded, onExpandedChange = { waxExpanded = !waxExpanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedWax?.name ?: "",
                onValueChange = {},
                label = { Text("Wosk") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = waxExpanded) },
            )
            ExposedDropdownMenu(expanded = waxExpanded, onDismissRequest = { waxExpanded = false }) {
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
        Spacer(modifier = Modifier.height(4.dp))

        var fragranceExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = fragranceExpanded, onExpandedChange = { fragranceExpanded = !fragranceExpanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedFragrance?.name ?: "",
                onValueChange = {},
                label = { Text("Olejek") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fragranceExpanded) },
            )
            ExposedDropdownMenu(expanded = fragranceExpanded, onDismissRequest = { fragranceExpanded = false }) {
                fragrances.forEach { fragrance ->
                    DropdownMenuItem(
                        text = { Text(fragrance.name) },
                        onClick = {
                            selectedFragrance = fragrance
                            fragranceExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        var wickExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = wickExpanded, onExpandedChange = { wickExpanded = !wickExpanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedWick?.name ?: "",
                onValueChange = {},
                label = { Text("Knot") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wickExpanded) },
            )
            ExposedDropdownMenu(expanded = wickExpanded, onDismissRequest = { wickExpanded = false }) {
                wicks.forEach { wick ->
                    DropdownMenuItem(
                        text = { Text(wick.name) },
                        onClick = {
                            selectedWick = wick
                            wickExpanded = false
                        }
                    )
                }
            }
        }
        if (selectedWick?.unit == "m") {
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = wickLength,
                onValueChange = { wickLength = it },
                label = { Text("Długość knota (m)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        var dyeExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = dyeExpanded, onExpandedChange = { dyeExpanded = !dyeExpanded }) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedDye?.name ?: "",
                onValueChange = {},
                label = { Text("Barwnik (opcjonalnie)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dyeExpanded) },
            )
            ExposedDropdownMenu(expanded = dyeExpanded, onDismissRequest = { dyeExpanded = false }) {
                dyes.forEach { dye ->
                    DropdownMenuItem(
                        text = { Text(dye.name) },
                        onClick = {
                            selectedDye = dye
                            dyeExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = candleCount,
            onValueChange = { candleCount = it },
            label = { Text("Ilość świeczek") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.align(Alignment.Start)) {
            Text("Ilość zapachu: ${String.format("%.2f", fragranceAmount)} g/ml", fontWeight = FontWeight.SemiBold, color = textColor)
            Text("Ilość wosku: ${String.format("%.2f", waxAmount)} g/ml", fontWeight = FontWeight.SemiBold, color = textColor)
            Spacer(modifier = Modifier.height(4.dp))
            val count = candleCount.toIntOrNull() ?: 1
            val priceText = if (count > 1) "Szacowana cena za $count świeczek" else "Szacowana cena świeczki"
            Text("$priceText: ${String.format("%.2f", totalPrice)} zł", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        }
    }
}


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
        Text("Ustawienia", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (darkMode) Color(0xFFE5E7EB) else Color(0xFF374151))
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
                    Text("Tryb ciemny", fontWeight = FontWeight.Medium, color = if (darkMode) Color(0xFFF3F4F6) else Color(0xFF1F2937))
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
