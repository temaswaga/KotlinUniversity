package com.example.myapplication.Module5.Task4

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider

@Composable
fun TodoScreen() {
    val context = LocalContext.current
    val viewModel: TodoViewModel = remember {
        ViewModelProvider(context as ComponentActivity)[TodoViewModel::class.java]
    }

    var isAddingTask by remember { mutableStateOf(false) }

    if (isAddingTask) {
        AddTodoScreenContent(
            onBack = { isAddingTask = false },
            onSave = { title, description ->
                viewModel.addTodo(title, description)
                isAddingTask = false
            }
        )
    } else {
        TodoListScreenContent(
            viewModel = viewModel,
            onAddNewTask = { isAddingTask = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreenContent(
    viewModel: TodoViewModel,
    onAddNewTask: () -> Unit
) {
    val todos by viewModel.todos.collectAsState()
    val highlightCompleted by viewModel.highlightCompleted.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF3F8FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Todo List",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Цвет завершённых",
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = highlightCompleted,
                            onCheckedChange = { viewModel.setHighlightCompleted(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF006684),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF94A3B8)
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFBAE6FD)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewTask,
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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(todos, key = { it.id }) { todo ->
                TodoItemRow(
                    todo = todo,
                    highlightCompleted = highlightCompleted,
                    onToggle = { viewModel.toggleTodo(todo) },
                    onDelete = { viewModel.deleteTodo(todo) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TodoItemRow(
    todo: TodoItem,
    highlightCompleted: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val cardColor = if (todo.isCompleted && highlightCompleted) {
        Color(0xFF00E600)
    } else {
        Color(0xFFD0D4D8)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onDelete
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF006684),
                    uncheckedColor = Color(0xFF475569)
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                if (todo.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreenContent(
    onBack: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF3F8FB),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Новая задача",
                        fontSize = 20.sp,
                        color = Color(0xFF0F172A)
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
                                color = Color(0xFF0F172A),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            text = "Сохранить",
                            fontSize = 16.sp,
                            color = if (title.isNotBlank()) Color(0xFF006684) else Color(0xFF94A3B8)
                        )
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название задачи") },
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
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (опционально)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color(0xFF64748B),
                    focusedBorderColor = Color(0xFF0284C7)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "Отмена",
                        fontSize = 15.sp,
                        color = Color(0xFF006684)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(title, description)
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006684),
                        disabledContainerColor = Color(0xFFD0D4D8),
                        disabledContentColor = Color(0xFF64748B)
                    )
                ) {
                    Text(
                        text = "Сохранить задачу",
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}