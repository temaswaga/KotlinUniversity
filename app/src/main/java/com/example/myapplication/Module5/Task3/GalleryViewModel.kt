package com.example.myapplication.Module5Task3

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _photos = MutableStateFlow<List<File>>(emptyList())
    val photos = _photos.asStateFlow()

    private var currentPhotoFile: File? = null

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val files = storageDir?.listFiles { file ->
            file.isFile && file.extension.lowercase(Locale.ROOT) == "jpg"
        } ?: emptyArray()

        _photos.value = files.sortedByDescending { it.lastModified() }
    }

    fun createNewPhotoUri(): Uri? {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        val file = File(storageDir, fileName)
        currentPhotoFile = file
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun onPhotoTaken(success: Boolean) {
        if (success) {
            loadPhotos()
        } else {
            currentPhotoFile?.let {
                if (it.exists() && it.length() == 0L) {
                    it.delete()
                }
            }
        }
        currentPhotoFile = null
    }

    fun exportToGallery(sourceFile: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyGallery")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        context.contentResolver.update(uri, contentValues, null, null)
                    }
                    withContext(Dispatchers.Main) { onComplete(true) }
                } else {
                    withContext(Dispatchers.Main) { onComplete(false) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }
}