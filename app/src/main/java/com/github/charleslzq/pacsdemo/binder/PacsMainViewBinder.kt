package com.github.charleslzq.pacsdemo.binder

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.vo.PacsDemoViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMainViewBinder(
        mainView: View
) : ViewBinder<View, PacsDemoViewModel>(mainView) {
    private val thumbListViewBinder = ThumbListViewBinder(view.findViewById(R.id.thumbList))
    private val viewSelectorBinder = ViewSelectorBinder(view.findViewById(R.id.viewSelector))
    private val imageProgressBarBinder = ImageProgressBarBinder(view.findViewById(R.id.imageSeekBar))
    private val buttonPanelBinder = ButtonPanelBinder(view.findViewById(R.id.buttonPanel))

    init {
        onNewModel {
            thumbListViewBinder.model = it
            viewSelectorBinder.model = it
            buttonPanelBinder.model = it
            if (it != null) {
                it.selected = 0
                onModelChange(it::selected) { _, _ ->
                    resetProgress()
                }
            }
            resetProgress()
        }
    }

    private fun resetProgress() {
        if (model != null && model!!.layoutOption == PacsDemoViewModel.LayoutOption.ONE_ONE) {
            imageProgressBarBinder.model = model!!.seriesList[model!!.selected].imageFramesViewModel
        }
    }
}