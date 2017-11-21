package com.github.charleslzq.pacsdemo

import android.graphics.drawable.AnimationDrawable
import android.util.Log

/**
 * Created by charleslzq on 17-11-20.
 */
class ControllableAnimationDrawable(
        private val total: Int,
        private val offset: Int = 0
) : AnimationDrawable() {
    var currentIndex = 0
    var finish = false

    override fun selectDrawable(index: Int): Boolean {
        Log.i("test", "Processing $index frame(s), $offset, ${this.isRunning}")
        currentIndex = index
        if (offset + currentIndex == total -1) {
            finish = true
            this.stop()
        }
        return super.selectDrawable(index)
    }
}