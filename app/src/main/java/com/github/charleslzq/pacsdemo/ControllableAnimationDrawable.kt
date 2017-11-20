package com.github.charleslzq.pacsdemo

import android.graphics.drawable.AnimationDrawable
import android.util.Log

/**
 * Created by charleslzq on 17-11-20.
 */
class ControllableAnimationDrawable : AnimationDrawable() {
    var currentIndex: Int = 0

    override fun selectDrawable(index: Int): Boolean {
        Log.i("test", "Processing $index frame(s), ${this.isRunning}")
        currentIndex = index
        return super.selectDrawable(index)
    }
}