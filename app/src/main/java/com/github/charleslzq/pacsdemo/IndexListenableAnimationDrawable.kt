package com.github.charleslzq.pacsdemo

import android.graphics.drawable.AnimationDrawable

/**
 * Created by charleslzq on 17-11-20.
 */
class IndexListenableAnimationDrawable(
        private val startOffset: Int = 0,
        private val indexChangeCallback: (Int) -> Unit
) : AnimationDrawable() {
    var currentIndex = 0

    override fun selectDrawable(index: Int): Boolean {
        currentIndex = index
        if (this.isRunning) {
            indexChangeCallback.invoke(startOffset + index)
        }
        return super.selectDrawable(index)
    }
}