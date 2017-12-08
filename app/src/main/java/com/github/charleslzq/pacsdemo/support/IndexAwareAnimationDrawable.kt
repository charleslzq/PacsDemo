package com.github.charleslzq.pacsdemo.support

import android.graphics.drawable.AnimationDrawable
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent

/**
 * Created by charleslzq on 17-11-20.
 */
class IndexAwareAnimationDrawable(val layoutPosition: Int, val startOffset: Int) : AnimationDrawable() {

    override fun selectDrawable(index: Int): Boolean {
        EventBus.post(ImageDisplayEvent.IndexChange(layoutPosition, startOffset + index))
        return super.selectDrawable(index)
    }
}