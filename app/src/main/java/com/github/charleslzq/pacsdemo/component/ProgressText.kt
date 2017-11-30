package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.component.state.ImageFramesViewState
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class ProgressText(
        textView: TextView,
        position: Int,
        pacsViewState: PacsViewState
) : PacsComponentFragment<TextView, ImageFramesViewState>(textView, pacsViewState, { it.imageCells[position] }) {

    init {
        onStateChange(state::framesModel) {
            if (state.framesModel.size > 1) {
                setProgress(0, state.framesModel.size)
                onStateChange(state::currentIndex) {
                    setProgress(it.second, state.framesModel.size)
                }
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    private fun setProgress(index: Int, size: Int) {
        view.post({
            view.text = "${index + 1} / ${size}"
        })
    }
}