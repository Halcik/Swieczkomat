package com.example.swieczkomat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swieczkomat.data.Material
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.ui.dialogs.AddMaterialDialog
import com.example.swieczkomat.ui.dialogs.EditMaterialDialog
import com.example.swieczkomat.ui.dialogs.RemoveMaterialDialog
import java.util.Locale

@Composable
fun MaterialsTab(darkMode: Boolean, viewModel: MaterialsViewModel) {
    val materials by viewModel.getAllMaterials().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Material?>(null) }
    var showRemoveDialog by remember { mutableStateOf<Material?>(null) }
    val topSpacing = 12.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, end = 12.dp, top = topSpacing)
        ) {
            Text(
                "Lista materiałów",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (darkMode) Color.White else Color.Black
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(materials) { material ->
                    MaterialRow(
                        material,
                        darkMode,
                        onEdit = { showEditDialog = material },
                        onRemove = { showRemoveDialog = material }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF2563EB),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, contentDescription = "Dodaj materiał", tint = Color.White) }

        if (showAddDialog) AddMaterialDialog(onDismiss = { showAddDialog = false }, onAddMaterial = { viewModel.addOrUpdateMaterial(it); showAddDialog = false }, darkMode = darkMode, existingMaterials = materials)
        showEditDialog?.let { EditMaterialDialog(material = it, onDismiss = { showEditDialog = null }, onEditMaterial = { viewModel.updateMaterial(it); showEditDialog = null }, darkMode = darkMode, existingMaterials = materials) }
        showRemoveDialog?.let { RemoveMaterialDialog(material = it, onDismiss = { showRemoveDialog = null }, onRemove = { m, q -> viewModel.removeMaterial(m, q); showRemoveDialog = null }, darkMode = darkMode) }
    }
}

@Composable
private fun MaterialRow(material: Material, darkMode: Boolean, onEdit: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // większy odstęp
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${material.name} (${material.category})")
                Text(
                    String.format(Locale.getDefault(), "Ilość: %.2f %s", material.quantity, material.unit),
                    fontSize = 12.sp
                )
                material.preferredWickName?.let { Text("Knot: $it", fontSize = 10.sp) }
                material.preferredConcentration?.let { Text("Stężenie: $it%", fontSize = 10.sp) }
            }
            val pricePerUnit = if (material.quantity > 0) material.price / material.quantity else 0.0
            Text(String.format(Locale.getDefault(), "%.2f zł / %s", pricePerUnit, material.unit))
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edytuj materiał",
                    tint = if (darkMode) Color.White else Color.Black
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń materiał",
                    tint = if (darkMode) Color.White else Color.Black
                )
            }
        }
    }
}
