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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.ui.theme.SwieczkomatTheme
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.vievmodels.MaterialsViewModelFactory

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
            when (selectedTab) {
                0 -> MaterialsTab(darkMode, materialsViewModel)
                1 -> CandlesTab(darkMode)
                2 -> CalculatorTab(darkMode)
                3 -> SettingsTab(darkMode, onToggleDarkMode)
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
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj materiał")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text(
                "Lista materiałów",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (darkMode) Color.White else Color.Black
            )

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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${material.name} (${material.category}): ${material.quantity} ${material.unit}")
                            Text("${material.price} zł")
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddMaterialDialog(
                onDismiss = { showDialog = false },
                onAddMaterial = {
                    viewModel.addOrUpdateMaterial(it)
                    showDialog = false
                },
                darkMode = darkMode,
                existingMaterials = materials
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
    val units = listOf("g", "ml", "szt")
    var unit by remember { mutableStateOf(units.first()) }
    var price by remember { mutableStateOf("") }
    val categories = listOf("Wosk", "Olejek", "Knot", "Pojemnik", "Barwnik", "Inne")
    var category by remember { mutableStateOf(categories.first()) }

    var nameExpanded by remember { mutableStateOf(false) }

    val filteredMaterialNames = existingMaterials
        .map { it.name }
        .distinct()
        .filter { it.contains(materialName, ignoreCase = true) }


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
                                        materialName = selectionOption
                                        existingMaterials.find { it.name == selectionOption }?.let {
                                            category = it.category
                                            unit = it.unit
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Ilość") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox( // Changed Box to ExposedDropdownMenuBox for consistency
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
                            units.forEach { selectionOption ->
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
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Cena (zł)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newMaterial = Material(
                                name = materialName,
                                quantity = quantity.toDoubleOrNull() ?: 0.0,
                                unit = unit,
                                price = price.toDoubleOrNull() ?: 0.0,
                                category = category
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
fun CandlesTab(darkMode: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Świeczki - wkrótce", color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
    }
}

@Composable
fun CalculatorTab(darkMode: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Kalkulator - wkrótce", color = if (darkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280))
    }
}

@Composable
fun SettingsTab(darkMode: Boolean, onToggleDarkMode: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
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
