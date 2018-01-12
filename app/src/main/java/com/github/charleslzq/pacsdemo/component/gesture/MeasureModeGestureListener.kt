package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.github.charleslzq.pacsdemo.component.store.ImageActions

/**
 * Created by charleslzq on 17-11-30.
 */
class MeasureModeGestureListener(dispatch: (Any) -> Unit) : ScaleCompositeGestureListener(dispatch) {

    override fun onOtherGesture(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> dispatch(ImageActions.selectPoint(getPoint(motionEvent), false, true))
            MotionEvent.ACTION_MOVE -> dispatch(ImageActions.selectPoint(getPoint(motionEvent), true, true))
            MotionEvent.ACTION_UP -> dispatch(ImageActions.selectPoint(getPoint(motionEvent), true, false))
        }
        return true
    }

    private fun getPoint(motionEvent: MotionEvent) = PointF(motionEvent.x, motionEvent.y)
}