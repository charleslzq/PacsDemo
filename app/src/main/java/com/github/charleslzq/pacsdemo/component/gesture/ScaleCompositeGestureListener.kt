package com.github.charleslzq.pacsdemo.component.gesture

import android.view.ScaleGestureDetector
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val layoutPosition: Int
) : NoOpCompositeGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        EventBus.post(ImageDisplayEvent.ScaleChange(layoutPosition, rawScaleFactor))
        return true
    }
}