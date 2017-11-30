package com.github.charleslzq.pacsdemo.component.gesture

import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val framesViewState: ImageFramesViewState
) : NoOpCompositeGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        framesViewState.scaleFactor *= Math.max(1.0f, Math.min(rawScaleFactor, 5.0f))
        return true
    }
}