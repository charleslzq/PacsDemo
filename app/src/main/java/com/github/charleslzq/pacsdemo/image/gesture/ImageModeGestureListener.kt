package com.github.charleslzq.pacsdemo.image.gesture

import android.graphics.Matrix
import android.view.GestureDetector
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.image.ImageListView

/**
 * Created by charleslzq on 17-11-24.
 */
class ImageModeGestureListener(
        private val imageListView: ImageListView
) : GestureDetector.SimpleOnGestureListener() {

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        imageListView.savedMatrix = Matrix()
        imageListView.savedMatrix.reset()
        imageListView.imageMatrix = imageListView.savedMatrix
        imageListView.imageFramesState.scaleFactor = 1.0f
        imageListView.invalidate()
        return true
    }

    override fun onFling(startMotionEvent: MotionEvent, currentMotionEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val offsetX = currentMotionEvent.x - startMotionEvent.x
        val offsetY = currentMotionEvent.y - startMotionEvent.y
        imageListView.savedMatrix.postTranslate(offsetX, offsetY)
        imageListView.imageMatrix = imageListView.savedMatrix
        imageListView.invalidate()
        return true
    }
}