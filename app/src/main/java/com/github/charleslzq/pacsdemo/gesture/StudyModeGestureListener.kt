package com.github.charleslzq.pacsdemo.gesture

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListenerPlayModeGestureListener(
        val framesViewModel: ImageFramesViewModel
) :NoOpAllGestureListener() {
    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        framesViewModel.scaleFactor = 1.0f
        framesViewModel.matrix = Matrix()
        return true
    }
}