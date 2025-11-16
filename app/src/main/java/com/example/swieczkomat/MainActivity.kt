package com.example.swieczkomat

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swieczkomat.ui.theme.SwieczkomatTheme
import com.example.swieczkomat.vievmodels.MaterialsViewModel
import com.example.swieczkomat.vievmodels.MaterialsViewModelFactory
import com.example.swieczkomat.vievmodels.CandleViewModel
import com.example.swieczkomat.vievmodels.CandleViewModelFactory
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.swieczkomat.ui.screens.MaterialsTab
import com.example.swieczkomat.ui.screens.CalculatorTab
import com.example.swieczkomat.ui.screens.SettingsTab
import com.example.swieczkomat.ui.screens.CandlesTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val application = LocalContext.current.applicationContext as Application
            val materialsViewModel: MaterialsViewModel = viewModel(
                factory = MaterialsViewModelFactory(application)
            )
            val candleViewModel: CandleViewModel = viewModel(
                factory = CandleViewModelFactory(application)
            )
            val systemDark = isSystemInDarkTheme()
            var darkModeEnabled by remember { mutableStateOf(systemDark) }
            SwieczkomatTheme(darkTheme = darkModeEnabled) {
                CandleManagerScreen(
                    darkMode = darkModeEnabled,
                    onToggleDarkMode = { darkModeEnabled = !darkModeEnabled },
                    materialsViewModel = materialsViewModel,
                    candleViewModel = candleViewModel
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
    materialsViewModel: MaterialsViewModel,
    candleViewModel: CandleViewModel
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
    val activeLightLabelColor = Color(0xFF1F2937)
    val activeLightIconColor = Color(0xFF7C3AED)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = bgGradientColors))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(colors = headerGradientColors))
                .padding(top = 48.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text("Świeczkomat", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Zarządzaj swoimi świeczkami", fontSize = 14.sp, color = Color(0xFFE9D5FF))
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> MaterialsTab(darkMode, materialsViewModel)
                    1 -> CandlesTab(darkMode, candleViewModel, materialsViewModel)
                    2 -> CalculatorTab(darkMode, materialsViewModel, candleViewModel)
                    3 -> SettingsTab(darkMode, onToggleDarkMode)
                }
            }
        }
        NavigationBar(
            containerColor = tabsBackgroundColor,
            tonalElevation = 0.dp,
            modifier = Modifier.navigationBarsPadding()
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
                    icon = { Icon(tabIcons[i], contentDescription = title, tint = iconColor) },
                    label = { Text(title, color = labelColor, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium) },
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
