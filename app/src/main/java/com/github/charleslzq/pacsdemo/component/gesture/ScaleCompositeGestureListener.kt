package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(
        val dispatch: (Any) -> Unit
) : NoOpCompositeGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        dispatch(ImageFramesStore.ImageClicked())
        return true
    }

    override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
        val rawScaleFactor = scaleGestureDetector.scaleFactor
        dispatch(ImageFramesStore.ScaleChange(rawScaleFactor))
        return true
    }
}