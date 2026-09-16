package com.example.myapplication.Task8

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class CompressPhotoWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val inputPath = inputData.getString("input_path") ?: "photo_raw.jpg"

        delay(2500L)

        val compressedPath = inputPath.replace(".jpg", "_compressed.jpg")
        val outputData = workDataOf("compressed_path" to compressedPath)
        return Result.success(outputData)
    }
}

class WatermarkWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val inputPath = inputData.getString("compressed_path") ?: return Result.failure()

        delay(2500L)

        val watermarkedPath = inputPath.replace(".jpg", "_wm.jpg")
        val outputData = workDataOf("watermarked_path" to watermarkedPath)
        return Result.success(outputData)
    }
}

class UploadPhotoWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val inputPath = inputData.getString("watermarked_path") ?: return Result.failure()

        delay(3000L)

        val timestamp = System.currentTimeMillis()
        val cloudUrl = "https://cloud.example.com/uploaded_$timestamp.jpg"
        val outputData = workDataOf("cloud_url" to cloudUrl)
        return Result.success(outputData)
    }
}