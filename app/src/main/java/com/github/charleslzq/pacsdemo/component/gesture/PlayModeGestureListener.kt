package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.ColorMatrix
import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        framesViewState: ImageFramesViewState,
        private val onDrag: () -> Unit
) : ScaleCompositeGestureListener(framesViewState) {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        if (framesViewState.playable()) {
            framesViewState.playing = !framesViewState.playing
        }
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            val rawDistance = (distanceX / 10).toInt()
            framesViewState.currentIndex = Math.min(Math.max(framesViewState.currentIndex - rawDistance, 0), framesViewState.framesModel.size - 1)
        }
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
        onDrag()
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        if (framesViewState.playable()) {
            framesViewState.playing = false
        }
        framesViewState.colorMatrix = ColorMatrix()
        framesViewState.currentIndex = 0
        framesViewState.pseudoColor = false
        return true
    }
}