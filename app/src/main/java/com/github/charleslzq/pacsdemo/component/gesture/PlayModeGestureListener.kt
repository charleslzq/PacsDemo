package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(dispatch: (Any) -> Unit, private val onDragStart: () -> Unit) :
    ScaleCompositeGestureListener(dispatch) {

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            dispatch(ImageDisplayActions.indexScroll(distanceX))
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) = onDragStart()

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageDisplayActions.resetDisplay())
        return true
    }
}