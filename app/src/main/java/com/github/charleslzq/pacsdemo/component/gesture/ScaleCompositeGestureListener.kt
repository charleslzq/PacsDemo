package com.github.charleslzq.pacsdemo.component.gesture

import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val layoutPosition: Int
): NoOpCompositeGestureListener() {

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        val scaleFactor= Math.max(1.0f, Math.min(rawScaleFactor, 5.0f))
        EventBus.post(ImageDisplayEvent.ScaleChange(layoutPosition, scaleFactor))
        return true
    }
}