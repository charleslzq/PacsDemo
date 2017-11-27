package com.github.charleslzq.pacsdemo.gesture

import android.view.GestureDetector
import android.view.ScaleGestureDetector

/**
 * Created by charleslzq on 17-11-27.
 */
open class SimpleAllGestureListener : GestureDetector.SimpleOnGestureListener(), ScaleGestureDetector.OnScaleGestureListener {

    override fun onScaleBegin(p0: ScaleGestureDetector?): Boolean {
        return false
    }

    override fun onScaleEnd(p0: ScaleGestureDetector?) {

    }

    override fun onScale(p0: ScaleGestureDetector?): Boolean {
        return false
    }
}