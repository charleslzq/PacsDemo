package com.github.charleslzq.pacsdemo.binder

import android.view.View
import android.widget.TextView
import com.github.charleslzq.pacsdemo.vo.ImageFramesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ProgressTextViewBinder(
        textView: TextView
) : ViewBinder<TextView, ImageFramesViewModel>(textView) {

    init {
        view.visibility = View.INVISIBLE
        onNewModel { model ->
            if (model != null && model.size > 1) {
                setProgress(0, model.size)
                onModelChange(model::currentIndex) { _, newIndex ->
                    setProgress(newIndex, model.size)
                }
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.INVISIBLE
            }
        }
    }

    fun setProgress(index: Int, size: Int) {
        view.post({
            view.text = "${index + 1} / ${size}"
        })
    }
}