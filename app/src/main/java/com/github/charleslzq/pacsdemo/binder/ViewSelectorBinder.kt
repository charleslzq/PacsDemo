package com.github.charleslzq.pacsdemo.binder

import android.graphics.ColorMatrix
import android.graphics.Matrix
import android.widget.*
import com.github.charleslzq.pacsdemo.ViewUtils
import com.github.charleslzq.pacsdemo.binder.vo.PacsDemoViewModel
import com.github.charleslzq.pacsdemo.binder.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelectorBinder(
        viewFlipper: ViewFlipper
) : ViewBinder<ViewFlipper, PacsDemoViewModel>(viewFlipper, { PacsDemoViewModel() }) {

    init {
        onNewModel {
            onModelChange(model::layoutOption) {
                viewFlipper.displayedChild = model.layoutOption.ordinal
                bindChildren()

            }
            onModelChange(model::selected) {
                if (!isInit(it)) {
                    bindChildren()
                }
            }
        }
    }

    private fun getImageViewBindersFromPanel(): List<ImageCellViewBinder> {
        val displayedChild = view.getChildAt(view.displayedChild)
        return when (PacsDemoViewModel.LayoutOption.values()[view.displayedChild]) {
            PacsDemoViewModel.LayoutOption.ONE_ONE -> {
                listOf(ImageCellViewBinder(displayedChild))
            }
            PacsDemoViewModel.LayoutOption.ONE_TWO -> {
                ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                        .map { ImageCellViewBinder(it) }
            }
            else -> {
                ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                        .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                        .map { ImageCellViewBinder(it) }
            }
        }
    }

    private fun bindChildren() {
        val binders = getImageViewBindersFromPanel()
        val selected = model.selected
        val seriesList = model.seriesList
        if (selected >= 0 && selected < seriesList.size) {
            if (binders.size > 1) {
                binders.filterIndexed { index, _ ->
                    index + selected < seriesList.size
                }.forEachIndexed { index, holder ->
                    val model = seriesList[selected + index]
                    model.imageFramesViewModel.allowPlay = false
                    holder.model = model
                    resetModel(holder.model)
                }
            } else if (binders.size == 1) {
                val binder = binders[0]
                val model = seriesList[selected]
                model.imageFramesViewModel.allowPlay = true
                binder.model = model
                resetModel(model)
            }
        }
    }

    private fun resetModel(model: PatientSeriesViewModel) {
        model.imageFramesViewModel.currentIndex = 0
        model.imageFramesViewModel.playing = false
        model.imageFramesViewModel.matrix = Matrix()
        model.imageFramesViewModel.colorMatrix = ColorMatrix()
        model.imageFramesViewModel.scaleFactor = 1.0f
        model.imageFramesViewModel.startOffset = 0
    }
}