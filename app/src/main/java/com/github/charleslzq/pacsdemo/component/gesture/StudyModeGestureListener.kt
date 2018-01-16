package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore

/**
 * Created by charleslzq on 17-11-27.
 */
class StudyModeGestureListener(dispatch: (Any) -> Unit, private val onDragStart: () -> Unit) : ScaleCompositeGestureListener(dispatch) {

    override fun onDoubleTap(motionEvent: MotionEvent?): Boolean {
        dispatch(ImageFrameStore.StudyModeReset())
        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        dispatch(ImageFrameStore.LocationTranslate(distanceX, distanceY))
        return true
    }

    override fun onLongPress(e: MotionEvent) = onDragStart()
}