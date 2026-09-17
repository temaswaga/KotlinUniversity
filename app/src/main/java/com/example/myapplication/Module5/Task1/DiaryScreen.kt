package com.example.myapplication.Module5Task1

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider

sealed class ScreenState {
    object ListScreen : ScreenState()
    object CreateScreen : ScreenState()
    data class EditScreen(val entry: DiaryEntry) : ScreenState()
}

@Composable
fun DiaryScreen() {
    val context = LocalContext.current
    val viewModel: DiaryViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[DiaryViewModel::class.java]
    }

    var screenState by remember { mutableStateOf<ScreenState>(ScreenState.ListScreen) }

    when (val state = screenState) {
        is ScreenState.ListScreen -> {
            DiaryListContent(
                viewModel = viewModel,
                onAddNew = { screenState = ScreenState.CreateScreen },
                onEdit = { entry -> screenState = ScreenState.EditScreen(entry) }
            )
        }
        is ScreenState.CreateScreen -> {
            DiaryEditContent(
                titleHeader = "Новая запись",
                initialTitle = "",
                initialContent = "",
                onBack = { screenState = ScreenState.ListScreen },
                onSave = { title, content ->
                    viewModel.saveEntry(null, title, content)
                    screenState = ScreenState.ListScreen
                }
            )
        }
        is ScreenState.EditScreen -> {
            DiaryEditContent(
                titleHeader = "Редактировать",
                initialTitle = state.entry.title.ifEmpty { state.entry.fileName.removeSuffix(".txt") },
                initialContent = state.entry.content,
                onBack = { screenState = ScreenState.ListScreen },
                onSave = { title, content ->
                    viewModel.saveEntry(state.entry.fileName, title, content)
                    screenState = ScreenState.ListScreen
                }
            )
        }
    }
}

@Composable
fun DiaryListContent(
    viewModel: DiaryViewModel,
    onAddNew: () -> Unit,
    onEdit: (DiaryEntry) -> Unit
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF3F8FB),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNew,
                containerColor = Color(0xFFBAE6FD),
                contentColor = Color(0xFF0F172A),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(size.width / 2f, 4.dp.toPx()),
                        end = Offset(size.width / 2f, size.height - 4.dp.toPx()),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(4.dp.toPx(), size.height / 2f),
                        end = Offset(size.width - 4.dp.toPx(), size.height / 2f),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (entries.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "У вас пока нет записей",
                        fontSize = 17.sp,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Нажмите + чтобы создать первую",
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.fileName }) { entry ->
                        DiaryCard(
                            entry = entry,
                            onClick = { onEdit(entry) },
                            onDelete = { viewModel.deleteEntry(entry.fileName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E7EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title.ifEmpty { "(Без названия)" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.content.take(40),
                    fontSize = 15.sp,
                    color = Color(0xFF334155),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = entry.dateString,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val centerX = size.width / 2f
                        val radius = 2.dp.toPx()
                        drawCircle(Color(0xFF475569), radius, Offset(centerX, size.height * 0.25f))
                        drawCircle(Color(0xFF475569), radius, Offset(centerX, size.height * 0.5f))
                        drawCircle(Color(0xFF475569), radius, Offset(centerX, size.height * 0.75f))
                    }
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(Color(0xFFF1F5F9))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Удалить",
                                color = Color(0xFFB91C1C),
                                fontSize = 15.sp
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEditContent(
    titleHeader: String,
    initialTitle: String,
    initialContent: String,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var content by remember { mutableStateOf(initialContent) }

    Scaffold(
        containerColor = Color(0xFFF3F8FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleHeader,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.65f, size.height * 0.2f)
                                lineTo(size.width * 0.3f, size.height * 0.5f)
                                lineTo(size.width * 0.65f, size.height * 0.8f)
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1E293B),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (content.isNotBlank() || title.isNotBlank()) {
                                onSave(title, content)
                            }
                        }
                    ) {
                        Canvas(modifier = Modifier.size(20.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.2f, size.height * 0.55f)
                                lineTo(size.width * 0.45f, size.height * 0.8f)
                                lineTo(size.width * 0.85f, size.height * 0.3f)
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1E293B),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF3F8FB)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Заголовок (опционально)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF0284C7)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Ваша запись...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF0284C7)
                )
            )
        }
    }
}