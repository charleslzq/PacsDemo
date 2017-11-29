package com.github.charleslzq.pacsdemo.gesture

import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val viewWidth: Int,
        val viewHeight: Int,
        val framesViewModel: ImageFramesViewModel
): NoOpCompositeGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        framesViewModel.scaleFactor *= Math.max(1.0f, Math.min(rawScaleFactor, 5.0f))
        return true
    }
}