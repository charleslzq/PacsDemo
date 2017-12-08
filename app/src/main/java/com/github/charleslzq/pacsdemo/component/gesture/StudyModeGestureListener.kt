package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.Matrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        val framesStore: ImageFramesStore,
        layoutPosition: Int
) : ScaleCompositeGestureListener(layoutPosition) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
//        framesStore.scaleFactor = 1.0f
//        framesStore.matrix = Matrix()
//        framesStore.colorMatrix = ColorMatrix()
//        framesStore.pseudoColor = false
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val newMatrix = Matrix(framesStore.matrix)
        newMatrix.postTranslate(distanceX, distanceY)
//        framesStore.matrix = newMatrix
        return true
    }
}