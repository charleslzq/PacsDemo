package com.github.charleslzq.pacsdemo.component.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by charleslzq on 17-11-24.
 */
sealed class OperationMode(
        private val gestureDetector: GestureDetector,
        private val scaleGestureDetector: ScaleGestureDetector,
        private val additionalHandler: (View, MotionEvent) -> Boolean
) : View.OnTouchListener {

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        return additionalHandler(view, motionEvent) || gestureDetector.onTouchEvent(motionEvent) || scaleGestureDetector.onTouchEvent(motionEvent)
    }
}

class PlayMode(
        context: Context,
        compositeGestureListener: NoOpCompositeGestureListener
) : OperationMode(GestureDetector(context, compositeGestureListener), ScaleGestureDetector(context, compositeGestureListener), compositeGestureListener::onOtherGesture)

class StudyMode(
        context: Context,
        compositeGestureListener: NoOpCompositeGestureListener
) : OperationMode(GestureDetector(context, compositeGestureListener), ScaleGestureDetector(context, compositeGestureListener), compositeGestureListener::onOtherGesture)

class MeasureMode(
        context: Context,
        compositeGestureListener: NoOpCompositeGestureListener
) : OperationMode(GestureDetector(context, compositeGestureListener), ScaleGestureDetector(context, compositeGestureListener), compositeGestureListener::onOtherGesture)