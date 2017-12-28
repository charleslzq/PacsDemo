package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap
import com.github.charleslzq.pacsdemo.support.IndexAwareAnimationDrawable

/**
 * Created by charleslzq on 17-12-8.
 */
data class ImageDisplayModel(
        val duration: Int = 40,
        val currentIndex: Int = 0,
        val playing: Boolean = false,
        val image: Bitmap? = null,
        val animation: IndexAwareAnimationDrawable? = null
)