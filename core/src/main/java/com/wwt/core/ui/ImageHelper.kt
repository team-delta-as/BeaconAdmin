package com.wwt.core.ui

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.wwt.core.ui.glide.ImageRotateTransformation
import com.wwt.core.ui.glide.GlideApp
import com.wwt.core.ui.glide.GlideLogger

interface ImageHelper {
    fun loadImage(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int = -1,
        errorHandler: () -> Unit = {}
    )

    fun loadImageWithRotate(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int = -1,
        errorHandler: () -> Unit = {}
    )

    fun loadProfileImage(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int = -1,
        errorHandler: () -> Unit = {}
    )

    fun loadImageResource(iconResId: Int, imageView: ImageView, enableCircleCrop: Boolean = false)
}

class GlideImageHelper : ImageHelper {

    override fun loadProfileImage(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int,
        errorHandler: () -> Unit
    ) {
        if (uri.isNullOrBlank()) {
            loadImageResource(placeholderResource, imageView, true)
        } else {
            val glideApp = GlideApp.with(imageView.context)
                .load(Uri.parse(uri))
            if (uri.startsWith("content")) {
                glideApp.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            }
            glideApp.thumbnail(Glide.with(imageView.context).load(placeholderResource))
                .dontAnimate()
                .error(placeholderResource)
                .listener(ErrorGlideLogger(errorHandler))
                .transform(
                    ImageRotateTransformation(
                        imageView.context,
                        Uri.parse(uri)
                    ),
                    CircleCrop()
                )
                .into(imageView)
        }
    }

    override fun loadImage(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int,
        errorHandler: () -> Unit
    ) {
        if (uri.isNullOrBlank()) {
            loadImageResource(placeholderResource, imageView)
        } else {

            val glideApp = GlideApp.with(imageView.context)
                .load(Uri.parse(uri))
            if (uri.startsWith("content")) {
                glideApp.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            }
            glideApp.thumbnail(Glide.with(imageView.context).load(placeholderResource))
                .dontAnimate()
                .error(placeholderResource)
                .listener(ErrorGlideLogger(errorHandler))
                .into(imageView)
        }
    }

    override fun loadImageWithRotate(
        uri: String?,
        imageView: ImageView,
        placeholderResource: Int,
        errorHandler: () -> Unit
    ) {
        if (uri.isNullOrBlank()) {
            loadImageResource(placeholderResource, imageView)
        } else {

            val glideApp = GlideApp.with(imageView.context)
                .load(Uri.parse(uri))
            if (uri.startsWith("content")) {
                glideApp.diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
            }
            glideApp.thumbnail(Glide.with(imageView.context).load(placeholderResource))
                .dontAnimate()
                .error(placeholderResource)
                .listener(ErrorGlideLogger(errorHandler))
                .transform(
                    ImageRotateTransformation(
                        imageView.context,
                        Uri.parse(uri)
                    )
                )
                .into(imageView)
        }
    }

    override fun loadImageResource(
        iconResId: Int,
        imageView: ImageView,
        enableCircleCrop: Boolean
    ) {
        val glideApp = GlideApp.with(imageView.context)
            .load(iconResId)
            .dontAnimate()
            .listener(GlideLogger())
        if (enableCircleCrop)
            glideApp.circleCrop()
        glideApp.into(imageView)
    }

    companion object {
        private class ErrorGlideLogger<T>(val errorFunc: () -> Unit) : RequestListener<T> {
            override fun onResourceReady(
                resource: T,
                model: Any?,
                target: Target<T>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d(
                    "Glide",
                    "onResourceReady($resource, $model, $target, $dataSource, $isFirstResource"
                )
                return false
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<T>?,
                isFirstResource: Boolean
            ): Boolean {
                Log.d("Glide", "onLoadFailed($e, $model, $target, $isFirstResource)", e)
                errorFunc()
                return false
            }
        }
    }
}

fun ImageView.loadImage(
    uri: String?,
    placeholder: Int = -1,
    errorHandler: () -> Unit = {}
) =
    ImageHelperProvider.imageHelper.loadImage(uri, this, placeholder, errorHandler)

fun ImageView.loadImageWithRotate(
    uri: String?,
    placeholder: Int = -1,
    errorHandler: () -> Unit = {}
) =
    ImageHelperProvider.imageHelper.loadImageWithRotate(uri, this, placeholder, errorHandler)

fun ImageView.loadProfileImage(
    uri: String?,
    placeholder: Int = -1,
    errorHandler: () -> Unit = {}
) =
    ImageHelperProvider.imageHelper.loadProfileImage(uri, this, placeholder, errorHandler)


object ImageHelperProvider {
    var imageHelper: ImageHelper = GlideImageHelper()
}