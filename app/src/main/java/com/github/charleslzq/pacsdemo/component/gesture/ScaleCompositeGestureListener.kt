package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.ImageClicked
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.ScaleChange

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val dispatch: (Any) -> Unit
) : NoOpCompositeGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        dispatch(ImageClicked())
        return true
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        dispatch(ScaleChange(scaleGestureDetector.scaleFactor))
        return true
    }
}