package com.github.charleslzq.pacsdemo.binder

import android.view.View
import android.widget.SeekBar
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageProgressBarBinder(
        imageProgressBar: SeekBar
) : ViewBinder<SeekBar, ImageFramesViewModel>(imageProgressBar, { ImageFramesViewModel() }) {

    init {
        view.visibility = View.INVISIBLE
        onNewModel {
            if (model.playable()) {
                view.max = model.framesModel.size
                view.progress = model.currentIndex
                view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            model.currentIndex = progress
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })

                onModelChange(model::currentIndex) {
                    view.progress = model.currentIndex
                }

                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }
}