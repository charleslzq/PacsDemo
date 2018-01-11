package com.github.charleslzq.pacsdemo.component.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by charleslzq on 17-11-27.
 */
open class NoOpCompositeGestureListener : GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener {

    override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector) = false

    override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {}

    override fun onScale(scaleGestureDetector: ScaleGestureDetector) = false

    open fun onOtherGesture(view: View, motionEvent: MotionEvent) = false
}