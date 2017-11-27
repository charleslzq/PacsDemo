package com.github.charleslzq.pacsdemo.binder

import android.graphics.Matrix
import android.widget.*
import com.github.charleslzq.pacsdemo.ViewUtils
import com.github.charleslzq.pacsdemo.gesture.PresentationMode
import com.github.charleslzq.pacsdemo.vo.PacsDemoViewModel
import com.github.charleslzq.pacsdemo.vo.PatientSeriesViewModel

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelectorBinder(
        viewFlipper: ViewFlipper
) : ViewBinder<ViewFlipper, PacsDemoViewModel>(viewFlipper) {

    init {
        viewFlipper.displayedChild = PacsDemoViewModel.LayoutOption.ONE_ONE.ordinal
        onNewModel { newModel ->
            if (newModel != null) {
                onModelChange(newModel::layoutOption) { _, _ ->
                    viewFlipper.displayedChild = model!!.layoutOption.ordinal
                    bindChildren()
                }
                onModelChange(newModel::selected) { _, _ ->
                    bindChildren()
                }
            }
        }
    }

    fun getImageViewBindersFromPanel(): List<ImageCellViewBinder> {
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
        val selected = model!!.selected
        val seriesList = model!!.seriesList
        if (binders.size > 1) {
            binders.filterIndexed { index, _ ->
                index + selected < seriesList.size
            }.forEachIndexed { index, holder ->
                holder.model = seriesList[index + selected]
                holder.model!!.imageFramesViewModel.presentationMode = PresentationMode.SLIDE
                resetModel(holder.model!!)
            }
        } else if (binders.size == 1) {
            val binder = binders[0]
            val model = seriesList[selected]
            model.imageFramesViewModel.presentationMode = PresentationMode.ANIMATE
            binder.model = model
            resetModel(model)
        }
    }

    private fun resetModel(model: PatientSeriesViewModel) {
        model.imageFramesViewModel.currentIndex = 0
        model.imageFramesViewModel.playing = false
        model.imageFramesViewModel.matrix = Matrix()
        model.imageFramesViewModel.scaleFactor = 1.0f
        model.imageFramesViewModel.startOffset = 0
    }
}