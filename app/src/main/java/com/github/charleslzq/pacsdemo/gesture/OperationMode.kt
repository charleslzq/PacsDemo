package com.github.charleslzq.pacsdemo.gesture

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
        private val scaleGestureDetector: ScaleGestureDetector
) : View.OnTouchListener {

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        return gestureDetector.onTouchEvent(motionEvent) || scaleGestureDetector.onTouchEvent(motionEvent)
    }
}

class PlayMode(
        context: Context,
        allGestureListener: NoOpAllGestureListener
) : OperationMode(GestureDetector(context, allGestureListener), ScaleGestureDetector(context, allGestureListener))

class StudyMode(
        context: Context,
        allGestureListener: NoOpAllGestureListener
) : OperationMode(GestureDetector(context, allGestureListener), ScaleGestureDetector(context, allGestureListener))

class MeasureMode(
        context: Context,
        allGestureListener: NoOpAllGestureListener
) : OperationMode(GestureDetector(context, allGestureListener), ScaleGestureDetector(context, allGestureListener))