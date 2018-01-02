package com.github.charleslzq.pacsdemo.component

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageActions
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-11-27.
 */
class ButtonPanel(
        buttonPanel: View,
        pacsStore: PacsStore
) : PacsComponent<View>(buttonPanel, pacsStore) {
    private val splitButton: Button = view.findViewById(R.id.spliteButton)
    private val backButton: Button = view.findViewById(R.id.backButton)
    private val layoutSelector: PopupMenu = PopupMenu(buttonPanel.context, splitButton)

    init {
        layoutSelector.menu.add(Menu.NONE, R.id.one_one, Menu.NONE, "1 X 1")
        layoutSelector.menu.add(Menu.NONE, R.id.one_two, Menu.NONE, "1 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.two_two, Menu.NONE, "2 X 2")
        layoutSelector.menu.add(Menu.NONE, R.id.three_three, Menu.NONE, "3 X 3")
        layoutSelector.setOnMenuItemClickListener(this::onLayoutSelected)
        splitButton.setOnClickListener {
            layoutSelector.show()
        }

        val fontAwesomeTypeface = TypefaceUtil.getTypeFace(view.context, TypefaceUtil.fontAwesome)
        splitButton.typeface = fontAwesomeTypeface
        backButton.typeface = fontAwesomeTypeface
    }

    private fun onLayoutSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.one_one -> {
                store.dispatch(ImageActions.changeLayout(PacsStore.LayoutOption.ONE_ONE))
            }
            R.id.one_two -> {
                store.dispatch(ImageActions.changeLayout(PacsStore.LayoutOption.ONE_TWO))
            }
            R.id.two_two -> {
                store.dispatch(ImageActions.changeLayout(PacsStore.LayoutOption.TWO_TWO))
            }
            R.id.three_three -> {
                store.dispatch(ImageActions.changeLayout(PacsStore.LayoutOption.THREE_THREE))
            }
        }
        return true
    }
}