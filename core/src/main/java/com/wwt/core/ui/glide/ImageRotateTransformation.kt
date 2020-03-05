package com.wwt.core.ui.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.io.InputStream
import java.security.MessageDigest

class ImageRotateTransformation(
    private val context: Context,
    private val imageUri: Uri,
    private val rotateRotationAngle: Float = 90f
) : BitmapTransformation() {

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {
        val matrix = Matrix()

        matrix.postRotate(getImageOrientation(imageUri).toFloat())

        return Bitmap.createBitmap(
            toTransform,
            0,
            0,
            toTransform.width,
            toTransform.height,
            matrix,
            true
        )
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("rotate$rotateRotationAngle".toByteArray())
    }

    private fun getImageOrientation(imageUri: Uri): Int {
        ExifInterface.ORIENTATION_ROTATE_90
        var orientation = 90
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(imageUri)
            val exifInterface = ExifInterface(inputStream!!)
            orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                orientation = 90
            }
        } catch (e: Exception) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (ignored: Exception) {
                }

            }
        }
        return orientation
    }
}