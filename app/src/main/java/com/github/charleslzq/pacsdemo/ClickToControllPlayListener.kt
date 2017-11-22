package com.github.charleslzq.pacsdemo

import android.view.View

/**
 * Created by charleslzq on 17-11-22.
 */
class ClickToControllPlayListener(
        private val playControllable: PlayControllable
) : View.OnClickListener {

    override fun onClick(p0: View?) {
        when (playControllable.isRunning()) {
            true -> playControllable.pause()
            false -> playControllable.play()
        }
    }

}