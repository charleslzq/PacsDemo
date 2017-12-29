package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Bitmap

/**
 * Created by charleslzq on 17-12-8.
 */
data class ImageDisplayModel(
        val images: List<Bitmap> = emptyList()
)