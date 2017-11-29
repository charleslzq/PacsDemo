package com.github.charleslzq.pacsdemo.gesture

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        viewWidth: Int,
        viewHeight: Int,
        framesViewModel: ImageFramesViewModel
) : ScaleCompositeGestureListener(viewWidth, viewHeight, framesViewModel) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        framesViewModel.scaleFactor = 1.0f
        framesViewModel.matrix = Matrix()
        framesViewModel.colorMatrix = ColorMatrix()
        framesViewModel.pseudoColor = false
        return true
    }

    override fun onFling(startMotion: MotionEvent, currentMotion: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val motionX = currentMotion.x - startMotion.x
        val motionY = currentMotion.y - startMotion.y
        val newMatrix = Matrix(framesViewModel.matrix)
        newMatrix.postTranslate(motionX, motionY)
        framesViewModel.matrix = newMatrix
        return true
    }
}