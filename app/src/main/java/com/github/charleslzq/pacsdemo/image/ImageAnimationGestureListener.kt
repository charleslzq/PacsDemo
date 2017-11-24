package com.github.charleslzq.pacsdemo.image

import android.view.GestureDetector
import android.view.MotionEvent

/**
 * Created by charleslzq on 17-11-23.
 */
class ImageAnimationGestureListener(
        private val imageListView: ImageListView
) : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        if (imageListView.isRunning()) {
            imageListView.pause()
        } else {
            imageListView.play()
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        imageListView.reset()
        return true
    }
}