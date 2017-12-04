package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.*
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.EventBus
import com.github.charleslzq.pacsdemo.component.state.PacsViewState
import com.github.charleslzq.pacsdemo.component.state.PatientSeriesModel
import com.github.charleslzq.pacsdemo.support.ViewUtils

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper,
        pacsViewState: PacsViewState
) : PacsComponentGroup<ViewFlipper>(viewFlipper, pacsViewState, listOf(
        Sub(ImageCell::class, this::getImageCellsFromPanel, { parentState, index -> parentState.imageCells[index] })
)) {
    init {
        onStateChange(state::layoutOption) {
            view.displayedChild = state.layoutOption.ordinal
            rebind()
        }

        EventBus.onEvent<DragEventMessage.DropAtCellWithData> {
            val layoutPosition = it.layoutPosition
            val dataPosition = it.dataPosition
            if (dataPosition >= 0 && dataPosition < pacsViewState.imageCells.size && layoutPosition in (0..8)) {
                state.imageCells[layoutPosition].patientSeriesModel = pacsViewState.seriesList[dataPosition]
                if (state.layoutOption == PacsViewState.LayoutOption.ONE_ONE) {
                    state.imageCells[layoutPosition].imageFramesViewState.allowPlay = true
                }
            }
        }

        EventBus.onEvent<DragEventMessage.DropToCopyCell> {
            val srcPosition = it.sourcePosition
            val destPosition = it.targetPosition
            state.imageCells[destPosition].patientSeriesModel = state.imageCells[srcPosition].patientSeriesModel
            state.imageCells[destPosition].imageFramesViewState.copyFrom(state.imageCells[srcPosition].imageFramesViewState)
            state.imageCells[srcPosition].patientSeriesModel = PatientSeriesModel()
        }
    }

    companion object {
        private fun getImageCellsFromPanel(view: ViewFlipper): List<View> {
            val displayedChild = view.getChildAt(view.displayedChild)
            return when (PacsViewState.LayoutOption.values()[view.displayedChild]) {
                PacsViewState.LayoutOption.ONE_ONE -> {
                    listOf(displayedChild)
                }
                PacsViewState.LayoutOption.ONE_TWO -> {
                    ViewUtils.getTypedChildren(displayedChild as LinearLayout, RelativeLayout::class.java)
                }
                else -> {
                    ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                            .flatMap { ViewUtils.getTypedChildren(it, RelativeLayout::class.java) }
                }
            }
        }
    }
}