package com.wwt.core.ui.glide

import android.util.Log
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class GlideLogger<T> : RequestListener<T> {
    override fun onResourceReady(resource: T, model: Any?, target: Target<T>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        Log.d("Glide", "onResourceReady($resource, $model, $target, $dataSource, $isFirstResource")
        return false
    }

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<T>?, isFirstResource: Boolean): Boolean {
        Log.d("Glide", "onLoadFailed($e, $model, $target, $isFirstResource)", e)
        return false
    }
}