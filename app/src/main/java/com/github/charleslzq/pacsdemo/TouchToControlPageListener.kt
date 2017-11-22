package com.github.charleslzq.pacsdemo

import android.view.MotionEvent
import android.view.View

/**
 * Created by charleslzq on 17-11-22.
 */
class TouchToControlPageListener(
        private val pageControllable: PageControllable
) : View.OnTouchListener {

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            when (isRightSide(view, motionEvent)) {
                true -> pageControllable.nextPage()
                false -> pageControllable.previousPage()
            }
        }
        return true
    }

    private fun isRightSide(view: View, motionEvent: MotionEvent) = motionEvent.x > view.measuredWidth / 2
}