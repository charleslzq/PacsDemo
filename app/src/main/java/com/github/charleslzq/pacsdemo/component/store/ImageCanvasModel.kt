package com.github.charleslzq.pacsdemo.component.store

import android.graphics.Path
import android.graphics.PointF

/**
 * Created by charleslzq on 17-12-8.
 */
data class ImageCanvasModel(
        val paths: List<Path> = emptyList(),
        val texts: List<Pair<PointF, String>> = emptyList()
)