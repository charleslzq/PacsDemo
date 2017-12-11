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

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        EventBus.post(ImageDisplayEvent.ChangePlayStatus(layoutPosition))
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (Math.abs(distanceX) > 3 * Math.abs(distanceY)) {
            val rawDistance = (distanceX / 10).toInt()
            EventBus.post(ImageDisplayEvent.IndexScroll(layoutPosition, rawDistance))
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