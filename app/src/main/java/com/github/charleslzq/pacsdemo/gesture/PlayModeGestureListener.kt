package com.github.charleslzq.pacsdemo.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.gesture.PresentationMode.ANIMATE
import com.github.charleslzq.pacsdemo.gesture.PresentationMode.SLIDE
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        val framesViewModel: ImageFramesViewModel
) : SimpleAllGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        when (framesViewModel.presentationMode) {
            ANIMATE -> framesViewModel.playing = !framesViewModel.playing
            SLIDE -> {
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
        if (framesViewModel.presentationMode == ANIMATE) {
            framesViewModel.playing = false
        }
        framesViewModel.currentIndex = 0
        return true
    }

    private fun isRightSide(motionEvent: MotionEvent) = motionEvent.x > viewWidth / 2
}