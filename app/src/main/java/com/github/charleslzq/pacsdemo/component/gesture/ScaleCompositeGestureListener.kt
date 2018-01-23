package com.github.charleslzq.pacsdemo.component.gesture

import android.view.MotionEvent
import com.github.charleslzq.pacsdemo.component.store.ImageFrameStore.ImageClicked

/**
 * Created by charleslzq on 17-11-29.
 */
open class ScaleCompositeGestureListener(val dispatch: (Any) -> Unit) :
    NoOpCompositeGestureListener() {

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        dispatch(ImageClicked())
        return true
    }
}