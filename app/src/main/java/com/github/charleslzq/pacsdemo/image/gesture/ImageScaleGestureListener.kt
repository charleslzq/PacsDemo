package com.github.charleslzq.pacsdemo.image.gesture

import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.image.ImageListView

/**
 * Created by charleslzq on 17-11-23.
 */
class ImageScaleGestureListener(
        private val imageListView: ImageListView
) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
}