package com.github.charleslzq.pacsdemo.support

import android.graphics.drawable.AnimationDrawable
import com.github.charleslzq.pacsdemo.observe.ObservablePropertyWithObservers

/**
 * Created by charleslzq on 17-11-20.
 */
class IndexListenableAnimationDrawable() : AnimationDrawable() {
    var currentIndex by ObservablePropertyWithObservers(0)

    override fun selectDrawable(index: Int): Boolean {
        currentIndex = index
        return super.selectDrawable(index)
    }
}