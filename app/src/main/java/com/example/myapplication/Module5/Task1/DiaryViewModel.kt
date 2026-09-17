package com.example.myapplication.Module5Task1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val dateFormat = SimpleDateFormat("d MMM yyyy HH:mm", Locale.ENGLISH)

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries = _entries.asStateFlow()

    init {
        loadAllEntries()
    }

    private fun loadAllEntries() {
        val files = context.filesDir.listFiles { file ->
            file.isFile && file.extension == "txt"
        } ?: emptyArray()

        val loaded = files.mapNotNull { file ->
            parseFileToEntry(file)
        }.sortedByDescending { it.timestamp }

        _entries.value = loaded
    }

    private fun parseFileToEntry(file: File): DiaryEntry? {
        val nameWithoutExt = file.nameWithoutExtension
        val parts = nameWithoutExt.split("_", limit = 2)
        val timestamp = parts[0].toLongOrNull() ?: file.lastModified()
        val title = if (parts.size > 1) parts[1] else ""
        val content = try {
            file.readText()
        } catch (e: Exception) {
            return null
        }
        val dateString = dateFormat.format(Date(timestamp))

        return DiaryEntry(
            fileName = file.name,
            title = title,
            content = content,
            dateString = dateString,
            timestamp = timestamp
        )
    }

    fun saveEntry(oldFileName: String?, titleInput: String, contentInput: String) {
        val cleanTitle = titleInput.trim().replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        val timestamp = System.currentTimeMillis()
        val newFileName = if (cleanTitle.isNotEmpty()) {
            "${timestamp}_$cleanTitle.txt"
        } else {
            "$timestamp.txt"
        }

        if (oldFileName != null) {
            File(context.filesDir, oldFileName).delete()
        }

        val newFile = File(context.filesDir, newFileName)
        newFile.writeText(contentInput)

        val newEntry = DiaryEntry(
            fileName = newFileName,
            title = cleanTitle,
            content = contentInput,
            dateString = dateFormat.format(Date(timestamp)),
            timestamp = timestamp
        )

        val currentList = _entries.value.toMutableList()
        if (oldFileName != null) {
            currentList.removeAll { it.fileName == oldFileName }
        }
        currentList.add(0, newEntry)
        _entries.value = currentList
    }

    fun deleteEntry(fileName: String) {
        File(context.filesDir, fileName).delete()
        _entries.value = _entries.value.filterNot { it.fileName == fileName }
    }
}