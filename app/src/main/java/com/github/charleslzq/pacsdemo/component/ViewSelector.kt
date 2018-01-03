package com.github.charleslzq.pacsdemo.component

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.ViewFlipper
import com.github.charleslzq.pacsdemo.component.store.PacsStore
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
                    ViewUtils.getTypedChildren(displayedChild as LinearLayout, ConstraintLayout::class.java)
                }
                else -> {
                    ViewUtils.getTypedChildren(displayedChild as TableLayout, TableRow::class.java)
                            .flatMap { ViewUtils.getTypedChildren(it, ConstraintLayout::class.java) }
                }
            }
        }
    }
}