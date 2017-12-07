package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.store.PacsStore

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMain(
        mainView: View,
        pacsStore: PacsStore
) : PacsComponentGroup<View>(mainView, pacsStore, listOf(
        Sub(ThumbList::class, byId(R.id.thumbList), sameAsParent()),
        Sub(ViewSelector::class, byId(R.id.viewSelector), sameAsParent()),
        Sub(ButtonPanel::class, byId(R.id.buttonPanel), sameAsParent())
))