package com.github.charleslzq.pacsdemo.binder

import android.view.View
import android.widget.SeekBar
import com.github.charleslzq.pacsdemo.binder.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageScaleBarBinder(
        scaleBar: SeekBar
) : ViewBinder<SeekBar, ImageFramesViewModel>(scaleBar, { ImageFramesViewModel() }) {

    init {
        onNewModel {
            if (isInit(it) || model.playable()) {
                scaleBar.visibility = View.INVISIBLE
            } else {
                scaleBar.max = 40
                scaleBar.progress = 0
                scaleBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, process: Int, fromUser: Boolean) {
                        if (fromUser) {
                            model.scaleFactor = 1.0f + process * 0.1f
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {

                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {

                    }

                })
                onModelChange(model::scaleFactor) {
                    scaleBar.progress = ((model.scaleFactor - 1.0f) * 10).toInt()
                }
                scaleBar.visibility = View.VISIBLE
            }
        }
    }
}