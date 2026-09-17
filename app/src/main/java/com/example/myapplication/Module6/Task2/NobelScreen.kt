package com.example.myapplication.Module6.Task2

import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider

@Composable
fun NobelApp() {
    val context = LocalContext.current
    val viewModel: NobelViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[NobelViewModel::class.java]
    }
    val uiState by viewModel.uiState.collectAsState()
    var selectedPrize by remember { mutableStateOf<NobelPrize?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9FB)
    ) {
        if (selectedPrize != null) {
            NobelDetailScreen(
                prize = selectedPrize!!,
                onBack = { selectedPrize = null }
            )
        } else {
            NobelListScreen(
                uiState = uiState,
                onPrizeClick = { selectedPrize = it },
                onFilterApply = { year, category ->
                    viewModel.fetchPrizes(year, category)
                },
                onRetry = { viewModel.fetchPrizes() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobelListScreen(
    uiState: NobelUiState,
    onPrizeClick: (NobelPrize) -> Unit,
    onFilterApply: (String, String) -> Unit,
    onRetry: () -> Unit
) {
    var yearText by remember { mutableStateOf("2010") }
    val categories = listOf("Все", "Physics", "Chemistry", "Medicine", "Literature", "Peace", "Economics")
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Physics") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Нобелевские премии",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1F1F1F)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Строка фильтров
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it },
                    label = { Text("Год") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1.4f)
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Категория") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { onFilterApply(yearText, selectedCategory) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4D84)),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text("Фильтр", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (uiState) {
                is NobelUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF3C4D84))
                    }
                }
                is NobelUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.message, color = Color(0xFFD32F2F), fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C4D84))
                            ) {
                                Text("Повторить")
                            }
                        }
                    }
                }
                is NobelUiState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.prizes) { prize ->
                            NobelCardItem(prize = prize, onClick = { onPrizeClick(prize) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NobelCardItem(
    prize: NobelPrize,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E4E9)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${prize.year} — ${prize.category.uppercase()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1F1F)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prize.laureatesNames,
                fontSize = 15.sp,
                color = Color(0xFF2E2E2E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prize.shortMotivation,
                fontSize = 13.sp,
                color = Color(0xFF5A5A5A)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NobelDetailScreen(
    prize: NobelPrize,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали премии", fontSize = 20.sp, color = Color(0xFF1F1F1F)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color(0xFF1F1F1F))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text("Год: ${prize.year}", fontSize = 20.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Категория: ${prize.category}", fontSize = 18.sp, fontWeight = FontWeight.Normal)
            Spacer(modifier = Modifier.height(20.dp))
            Text("Лауреаты:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(prize.laureates.indices.toList()) { index ->
                    val laureate = prize.laureates[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E4E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${index + 1}. ${laureate.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F1F1F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Доля: ${laureate.portion}",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Мотивация: ${laureate.motivation}",
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }
    }
}