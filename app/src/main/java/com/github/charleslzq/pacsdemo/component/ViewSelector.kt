package com.github.charleslzq.pacsdemo.component

import android.view.View
import android.widget.*
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.event.BindingEvent
import com.github.charleslzq.pacsdemo.component.event.DragEventMessage
import com.github.charleslzq.pacsdemo.component.event.ImageDisplayEvent
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.component.store.PatientSeriesModel
import com.github.charleslzq.pacsdemo.support.ViewUtils

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper,
        pacsStore: PacsStore
) : PacsComponentGroup<ViewFlipper>(viewFlipper, pacsStore, listOf(
        Sub(ImageCell::class, this::getImageCellsFromPanel, { parentState, index -> parentState.imageCells[index] })
)) {
    init {
        EventBus.onEvent<DragEventMessage.DropAtCellWithData> {
            val layoutPosition = it.layoutPosition
            val dataPosition = it.dataPosition
            if (dataPosition >= 0 && dataPosition < store.seriesList.size) {
                EventBus.post(ImageDisplayEvent.PlayModeReset(layoutPosition))
                EventBus.post(BindingEvent.ModelDropped(layoutPosition, store.seriesList[dataPosition]))
            }
        }
        EventBus.onEvent<DragEventMessage.DropToCopyCell> {
            val sourcePosition = it.sourcePosition
            val destPosition = it.targetPosition
            if (sourcePosition >= 0 && sourcePosition < store.imageCells.size) {
                val data = store.imageCells[sourcePosition].patientSeriesModel
                EventBus.post(ImageDisplayEvent.PlayModeReset(destPosition))
                EventBus.post(BindingEvent.ModelDropped(destPosition, data))
                EventBus.post(ImageDisplayEvent.PlayModeReset(sourcePosition))
                EventBus.post(BindingEvent.ModelDropped(sourcePosition, PatientSeriesModel()))
            }
        }

        render(store::layoutOption) {
            view.displayedChild = store.layoutOption.ordinal
            reRenderChildren()
        }
    }

    companion object {
        private fun getImageCellsFromPanel(view: ViewFlipper): List<View> {
            val displayedChild = view.getChildAt(view.displayedChild)
            return when (PacsStore.LayoutOption.values()[view.displayedChild]) {
                PacsStore.LayoutOption.ONE_ONE -> {
                    listOf(displayedChild)
                }
                PacsStore.LayoutOption.ONE_TWO -> {
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