package com.github.charleslzq.pacsdemo.gesture

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        val framesViewModel: ImageFramesViewModel
) : NoOpAllGestureListener() {
    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        framesViewModel.scaleFactor = 1.0f
        framesViewModel.matrix = Matrix()
        framesViewModel.colorMatrix = ColorMatrix()
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