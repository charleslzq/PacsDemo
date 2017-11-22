package com.github.charleslzq.pacsdemo

import android.view.MotionEvent
import android.view.View

/**
 * Created by charleslzq on 17-11-22.
 */
class TouchToControlPlayListener(
        private val playControllable: PlayControllable
) : View.OnTouchListener {

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        view.performClick()
        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            when (playControllable.isRunning()) {
                true -> playControllable.pause()
                false -> playControllable.play()
            }
        }
        return true
    }

}