package com.github.charleslzq.pacsdemo.component.gesture

import android.graphics.PointF
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.ImageClicked

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(val dispatch: (Any) -> Unit) :
    NoOpCompositeGestureListener() {

    /**
     * 响应单击
     */
    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        dispatch(ImageClicked())
        return true
    }

    override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector) = true

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        scaleGestureDetector.run {
            dispatch(ImageFrameStore.ScaleChange(scaleFactor, PointF(focusX, focusY)))
        }
        return true
    }
}