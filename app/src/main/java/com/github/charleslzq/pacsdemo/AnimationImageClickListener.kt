package com.github.charleslzq.pacsdemo

import android.view.View

/**
 * Created by charleslzq on 17-11-21.
 */
class AnimationImageClickListener(
        private val animationViewManager: AnimationViewManager
) : View.OnClickListener {

    override fun onClick(p0: View?) {
        if (animationViewManager.isRunning()) {
            animationViewManager.pause()
        } else {
            animationViewManager.resume()
        }
    }
}