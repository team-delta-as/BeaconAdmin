package com.wwt.core.ui.glide

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class DeltaConnectGlideApp : AppGlideModule() {
    // Needed for Glide LibraryGlideModule to work properly
    override fun isManifestParsingEnabled() = false
}