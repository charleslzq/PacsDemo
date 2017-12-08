package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(
        layoutPosition: Int
) : ScaleCompositeGestureListener(layoutPosition) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        EventBus.post(ImageDisplayEvent.StudyModeReset(layoutPosition))
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        EventBus.post(ImageDisplayEvent.LocationTranslate(layoutPosition, distanceX, distanceY))
        return true
    }
}