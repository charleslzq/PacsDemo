package com.github.charleslzq.pacsdemo.component.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * Created by charleslzq on 17-11-24.
 */
sealed class OperationMode(private vararg val listeners: (View, MotionEvent) -> Boolean) : View.OnTouchListener {
    override fun onTouch(view: View, motionEvent: MotionEvent) = listeners.any { it(view, motionEvent) }
}

class PlayMode(
        context: Context,
        playModeGestureListener: PlayModeGestureListener,
        private val gestureDetector: GestureDetector = GestureDetector(context, playModeGestureListener),
        private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, playModeGestureListener))
    : OperationMode(
        { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) },
        { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
)

class StudyMode(
        context: Context,
        studyModeGestureListener: StudyModeGestureListener,
        private val gestureDetector: GestureDetector = GestureDetector(context, studyModeGestureListener),
        private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, studyModeGestureListener))
    : OperationMode(
        { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) },
        { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
)

class MeasureMode(
        context: Context,
        measureModeGestureListener: MeasureModeGestureListener,
        private val gestureDetector: GestureDetector = GestureDetector(context, measureModeGestureListener),
        private val scaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, measureModeGestureListener))
    : OperationMode(
        measureModeGestureListener::onOtherGesture,
        { _, motionEvent -> scaleGestureDetector.onTouchEvent(motionEvent) },
        { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
)