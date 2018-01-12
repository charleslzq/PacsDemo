package com.github.charleslzq.pacsdemo.component

import android.support.constraint.ConstraintLayout
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.ViewFlipper
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.support.ViewUtils

/**
 * Created by charleslzq on 17-11-27.
 */
class ViewSelector(
        viewFlipper: ViewFlipper,
        pacsStore: PacsStore
) : PacsComponent<ViewFlipper>(viewFlipper, pacsStore) {
    private val gestureDetector = GestureDetector(view.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onFling(startEvent: MotionEvent, endEvent: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (Math.abs(startEvent.x - endEvent.x) >= 3 * Math.abs(startEvent.y - endEvent.y)) {
                if (endEvent.x > startEvent.x) {
                    store.dispatch(ImageDisplayActions.changeLayout(store.layoutOption.ordinal - 1))
                } else {
                    store.dispatch(ImageDisplayActions.changeLayout(store.layoutOption.ordinal + 1))
                }
            }
            return true
        }
    })

    init {
        bind {
            children(ViewSelector.Companion::getImageCellsFromPanel) {
                ImageCell(view, store.imageCells[index])
            }
        }

        render(store::layoutOption) {
            view.displayedChild = store.layoutOption.ordinal
            rebind()
        }

        view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
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