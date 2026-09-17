package com.example.myapplication.Module5.Task4

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dao = TodoDatabase.getDatabase(context).todoDao()
    private val preferencesRepository = UserPreferencesRepository(context)

    val todos: StateFlow<List<TodoItem>> = dao.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val highlightCompleted: StateFlow<Boolean> = preferencesRepository.highlightCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.getCount() == 0) {
                importInitialData()
            }
        }
    }

    private suspend fun importInitialData() {
        val defaultList = mutableListOf<TodoItem>()
        try {
            val jsonString = context.assets.open("todos.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                defaultList.add(
                    TodoItem(
                        title = obj.getString("title"),
                        description = obj.optString("description", ""),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
        } catch (e: Exception) {
            defaultList.clear()
            defaultList.add(TodoItem(title = "Купить молоко", description = "2 литра, обезжиренное", isCompleted = false))
            defaultList.add(TodoItem(title = "Позвонить маме", description = "Спросить про выходные", isCompleted = true))
            defaultList.add(TodoItem(title = "Сделать ДЗ по Android", description = "Clean Architecture + Compose", isCompleted = false))
        }
        dao.insertAll(defaultList)
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun addTodo(title: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertTodo(TodoItem(title = title, description = description, isCompleted = false))
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteTodo(todo)
        }
    }

    fun setHighlightCompleted(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesRepository.setHighlightCompleted(enabled)
        }
    }
}