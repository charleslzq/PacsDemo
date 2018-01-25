package com.github.charleslzq.pacsdemo.component

import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.ImageDisplayActions
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.support.TypefaceUtil

/**
 * Created by charleslzq on 17-11-27.
 * 右下角控制面板, 包含后退和调整布局按钮
 */
class ButtonPanel(
    buttonPanel: View,
    pacsStore: PacsStore
) : PacsComponent<View>(buttonPanel, pacsStore) {
    private val splitButton: Button = view.findViewById<Button>(R.id.spliteButton).apply {
        setOnClickListener {
            layoutSelector.show()
        }
    }
    private val backButton: Button = view.findViewById(R.id.backButton)
    private val layoutSelector: PopupMenu = PopupMenu(buttonPanel.context, splitButton).apply {
        menu.add(Menu.NONE, R.id.one_one, Menu.NONE, "1 X 1")
        menu.add(Menu.NONE, R.id.one_two, Menu.NONE, "1 X 2")
        menu.add(Menu.NONE, R.id.two_two, Menu.NONE, "2 X 2")
        menu.add(Menu.NONE, R.id.three_three, Menu.NONE, "3 X 3")
        setOnMenuItemClickListener(::onLayoutSelected)
    }
    private val layoutMap = mapOf(
        R.id.one_one to PacsStore.LayoutOption.ONE_ONE,
        R.id.one_two to PacsStore.LayoutOption.ONE_TWO,
        R.id.two_two to PacsStore.LayoutOption.TWO_TWO,
        R.id.three_three to PacsStore.LayoutOption.THREE_THREE
    )

    init {
        TypefaceUtil.configureTextView(TypefaceUtil.FONT_AWESOME, splitButton, backButton)
    }

    private fun onLayoutSelected(item: MenuItem): Boolean {
        layoutMap[item.itemId]?.let {
            store.dispatch(ImageDisplayActions.changeLayout(it))
        }
        return true
    }
}