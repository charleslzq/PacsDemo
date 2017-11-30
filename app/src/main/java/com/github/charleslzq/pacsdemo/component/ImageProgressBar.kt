package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.SeekBar
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ImageProgressBar(
        imageProgressBar: SeekBar,
        pacsViewState: PacsViewState
) : PacsComponentFragment<SeekBar, ImageFramesViewState>(imageProgressBar, pacsViewState, { it.imageCells[0] }) {

    init {
        view.visibility = View.INVISIBLE
        onStateChange(state::framesModel) {
            state.allowPlay = globalState.layoutOption == PacsViewState.LayoutOption.ONE_ONE
            if (state.playable()) {
                view.max = state.framesModel.size
                view.progress = state.currentIndex
                view.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            state.currentIndex = progress
                        }
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })

                onStateChange(state::currentIndex) {
                    view.progress = state.currentIndex
                }

                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }
}