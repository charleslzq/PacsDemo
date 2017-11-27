package com.github.charleslzq.pacsdemo.binder

import android.view.View
import android.widget.SeekBar
import com.github.charleslzq.pacsdemo.gesture.PresentationMode
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageProgressBarBinder(
        imageProgressBar: SeekBar
) : ViewBinder<SeekBar, ImageFramesViewModel>(imageProgressBar) {

    init {
        view.visibility = View.INVISIBLE
        onNewModel {
            if (it != null && it.presentationMode == PresentationMode.ANIMATE) {
                view.max = it.size
                view.progress = it.currentIndex
                view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            it.currentIndex = progress
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })

                onModelChange(it::currentIndex) { _, newIndex ->
                    view.progress = newIndex
                }

                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }
}