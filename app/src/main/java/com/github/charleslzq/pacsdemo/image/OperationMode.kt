package com.github.charleslzq.pacsdemo.image

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
): View.OnTouchListener {

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        return gestureDetector.onTouchEvent(motionEvent) || scaleGestureDetector.onTouchEvent(motionEvent)
    }
}

class ListMode(
        context: Context,
        gestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener(),
        scaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
): OperationMode(GestureDetector(context, gestureListener), ScaleGestureDetector(context, scaleGestureListener))

class ImageMode(
        context: Context,
        gestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener(),
        scaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
): OperationMode(GestureDetector(context, gestureListener), ScaleGestureDetector(context, scaleGestureListener))

class EditMode(
        context: Context,
        gestureListener: GestureDetector.SimpleOnGestureListener = GestureDetector.SimpleOnGestureListener(),
        scaleGestureListener: ScaleGestureDetector.SimpleOnScaleGestureListener = ScaleGestureDetector.SimpleOnScaleGestureListener()
): OperationMode(GestureDetector(context, gestureListener), ScaleGestureDetector(context, scaleGestureListener))