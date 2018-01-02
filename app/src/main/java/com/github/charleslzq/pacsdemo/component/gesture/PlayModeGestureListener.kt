package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.store.ImageActions

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        dispatch: (Any) -> Unit,
        val layoutPosition: Int
) : ScaleCompositeGestureListener(dispatch) {

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            dispatch(ImageActions.indexScroll(distanceX))
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        EventBus.post(DragEventMessage.StartCopyCell(layoutPosition))
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageActions.resetDisplay())
        return true
    }
}