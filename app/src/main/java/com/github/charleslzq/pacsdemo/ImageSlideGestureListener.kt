package com.github.charleslzq.pacsdemo

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Created by charleslzq on 17-11-23.
 */
class ImageSlideGestureListener(
        private val imageListView: ImageListView
) : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        when (isRightSide(imageListView, motionEvent)) {
            true -> imageListView.nextPage()
            false -> imageListView.previousPage()
        }
        return true
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        imageListView.changeProgress(0)
        return true
    }

    private fun isRightSide(view: View, motionEvent: MotionEvent) = motionEvent.x > view.measuredWidth / 2
}