package com.github.charleslzq.pacsdemo.support

import android.graphics.drawable.AnimationDrawable
import com.github.charleslzq.pacsdemo.component.store.ImageActions

/**
 * Created by charleslzq on 17-11-20.
 */
class IndexAwareAnimationDrawable(val dispatch: (Any) -> Unit, private val startOffset: Int) : AnimationDrawable() {

    override fun selectDrawable(index: Int): Boolean {
        dispatch(ImageActions.playIndexChange(startOffset + index))
        return super.selectDrawable(index)
    }
}