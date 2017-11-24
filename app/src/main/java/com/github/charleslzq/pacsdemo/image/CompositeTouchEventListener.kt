package com.github.charleslzq.pacsdemo.image

import android.view.MotionEvent
import android.view.View

/**
 * Created by charleslzq on 17-11-23.
 */
class CompositeTouchEventListener(
        private val listeners: List<(View, MotionEvent) -> Boolean>
): View.OnTouchListener {
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        return listeners.any { it.invoke(view, motionEvent) }
    }
}