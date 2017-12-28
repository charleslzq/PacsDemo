package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.ClickEvent
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val layoutPosition: Int
) : NoOpCompositeGestureListener() {
    protected val dispatch: (Any) -> Unit = { EventBus.post(it) }

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        dispatch(ClickEvent.ImageClicked(layoutPosition))
        return true
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        dispatch(ImageDisplayEvent.ScaleChange(layoutPosition, rawScaleFactor))
        return true
    }
}