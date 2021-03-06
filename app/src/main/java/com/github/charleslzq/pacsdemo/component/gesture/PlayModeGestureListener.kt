package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent

/**
 * Created by charleslzq on 17-11-27.
 */
class PlayModeGestureListener(
        layoutPosition: Int
) : ScaleCompositeGestureListener(layoutPosition) {

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            EventBus.post(ImageDisplayEvent.IndexScroll(layoutPosition, distanceX))
        }
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        EventBus.post(DragEventMessage.StartCopyCell(layoutPosition))
    }

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        EventBus.post(ImageDisplayEvent.PlayModeReset(layoutPosition))
        return true
    }
}