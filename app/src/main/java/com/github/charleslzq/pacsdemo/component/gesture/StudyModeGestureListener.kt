package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        framesViewState: ImageFramesViewState
) : ScaleCompositeGestureListener(framesViewState) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        framesViewState.scaleFactor = 1.0f
        framesViewState.matrix = Matrix()
        framesViewState.colorMatrix = ColorMatrix()
        framesViewState.pseudoColor = false
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val newMatrix = Matrix(framesViewState.matrix)
        newMatrix.postTranslate(distanceX, distanceY)
        framesViewState.matrix = newMatrix
        return true
    }
}