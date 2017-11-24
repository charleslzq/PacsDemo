package com.github.charleslzq.pacsdemo.image.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.image.ImageListView

/**
 * Created by charleslzq on 17-11-24.
 */
class ImageModeGestureListener(
        private val imageListView: ImageListView
): GestureDetector.SimpleOnGestureListener() {

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        imageListView.imageFramesState.scaleFactor = 1.0f
        return true
    }

    override fun onFling(startMotionEvent: MotionEvent, currentMotionEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val offsetX = currentMotionEvent.x - startMotionEvent.x
        val offsetY = currentMotionEvent.y - startMotionEvent.y
        imageListView.imageMatrix.postTranslate(offsetX, offsetY)
        imageListView.invalidate()
        return true
    }
}