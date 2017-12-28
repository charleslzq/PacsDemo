package com.github.charleslzq.pacsdemo.support

import android.graphics.drawable.AnimationDrawable
import com.github.charleslzq.pacsdemo.component.store.ImageFramesStore

/**
 * Created by charleslzq on 17-11-20.
 */
class IndexAwareAnimationDrawable(val dispatch: (Any) -> Unit, val startOffset: Int) : AnimationDrawable() {

    override fun selectDrawable(index: Int): Boolean {
        dispatch(ImageFramesStore.IndexChange(startOffset + index, false))
        return super.selectDrawable(index)
    }
}