package com.github.charleslzq.pacsdemo.gesture

import android.graphics.ColorMatrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        framesViewModel: ImageFramesViewModel
) : ScaleCompositeGestureListener(framesViewModel) {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        if (framesViewModel.playable()) {
            framesViewModel.playing = !framesViewModel.playing
        }
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        val rawDistance = (distanceX / 10).toInt()
        framesViewModel.currentIndex = Math.min(Math.max(framesViewModel.currentIndex - rawDistance, 0), framesViewModel.size - 1)
        return true
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        if (framesViewModel.playable()) {
            framesViewModel.playing = false
        }
        framesViewModel.colorMatrix = ColorMatrix()
        framesViewModel.currentIndex = 0
        framesViewModel.pseudoColor = false
        return true
    }
}