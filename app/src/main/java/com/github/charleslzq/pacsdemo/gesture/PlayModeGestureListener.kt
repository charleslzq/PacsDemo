package com.github.charleslzq.pacsdemo.gesture

import android.graphics.ColorMatrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        viewWidth: Int,
        viewHeight: Int,
        framesViewModel: ImageFramesViewModel
) : ScaleCompositeGestureListener(viewWidth, viewHeight, framesViewModel) {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        when (framesViewModel.playable()) {
            true -> framesViewModel.playing = !framesViewModel.playing
            false -> {
                when (isRightSide(motionEvent)) {
                    true -> {
                        if (framesViewModel.currentIndex < framesViewModel.size - 1) {
                            framesViewModel.currentIndex += 1
                        }
                    }
                    false -> {
                        if (framesViewModel.currentIndex > 0) {
                            framesViewModel.currentIndex -= 1
                        }
                    }
                }
            }
        }
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

    private fun isRightSide(motionEvent: MotionEvent) = motionEvent.x > viewWidth / 2
}