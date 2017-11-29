package com.github.charleslzq.pacsdemo.binder.gesture

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        framesViewModel: ImageFramesViewModel
) : ScaleCompositeGestureListener(framesViewModel) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        framesViewModel.scaleFactor = 1.0f
        framesViewModel.matrix = Matrix()
        framesViewModel.colorMatrix = ColorMatrix()
        framesViewModel.pseudoColor = false
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val newMatrix = Matrix(framesViewModel.matrix)
        newMatrix.postTranslate(distanceX, distanceY)
        framesViewModel.matrix = newMatrix
        return true
    }
}