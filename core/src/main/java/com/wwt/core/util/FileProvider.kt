package com.wwt.core.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.wwt.core.BuildConfig
import com.wwt.core.filestore.GetPoiImageCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileProvider(private val context: Context) {

    fun localImageUri(directory: String, fileName: String): Uri? {
        val storageDir = context.getExternalFilesDir(directory)
        val photoFile = File(storageDir, fileName)
        if (photoFile.exists()) {
            return FileProvider.getUriForFile(
                context,
                "${BuildConfig.APPLICATION_ID}.provider",
                photoFile
            )
        } else {
            return null
        }
    }

    fun cameraImageUri(fileName: String): Uri {
        val storageDir = context.getExternalFilesDir(FileDirectory.PictureDirectory.type)
        val photoFile = File(storageDir, fileName)
        return FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider",
            photoFile
        )
    }

    fun saveImageToInternal(
        bm: Bitmap,
        directory: String,
        fileName: String,
        quality: Int = 100,
        param: GetPoiImageCallback?
    ) {
        context.getExternalFilesDir(directory)?.let { it ->
            it.mkdirs()
            val imageFile = File(it, fileName)
            val out = FileOutputStream(imageFile)
            try {
                bm.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.flush()
                out.close()

                MediaScannerConnection.scanFile(
                    context, arrayOf(imageFile.absolutePath), null
                ) { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                    param?.run {
                        resultUri(localImageUri(directory, fileName).toString())
                    }
                }
            } catch (e: Exception) {
                throw IOException()
            }
        }
    }

    fun saveImageToInternal(
        bm: Bitmap,
        directory: String,
        fileName: String,
        quality: Int = 100
    ): String {
        context.getExternalFilesDir(directory)?.let { it ->
            it.mkdirs()
            val imageFile = File(it, fileName)
            if (imageFile.exists()) {
                if (imageFile.delete()) {
                    cameraImageUri(imageFile.absolutePath)
                }
            }
            val out = FileOutputStream(imageFile)
            try {
                bm.compress(Bitmap.CompressFormat.JPEG, quality, out)
                out.flush()
                out.close()
                return imageFile.absolutePath
            } catch (e: Exception) {
                throw IOException()
            }
        }
        return ""
    }
}

object FileProviderFactory {
    lateinit var fileProvider: com.wwt.core.util.FileProvider
}