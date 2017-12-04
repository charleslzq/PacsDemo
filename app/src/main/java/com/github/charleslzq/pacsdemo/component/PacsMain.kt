package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.pacsdemo.R
import com.github.charleslzq.pacsdemo.component.state.PacsViewState

/**
 * Created by charleslzq on 17-11-27.
 */
class PacsMain(
        mainView: View,
        pacsViewState: PacsViewState
) : PacsComponentGroup<View>(mainView, pacsViewState, listOf(
        Sub(ThumbList::class, byId(R.id.thumbList), sameAsParent()),
        Sub(ViewSelector::class, byId(R.id.viewSelector), sameAsParent()),
        Sub(ButtonPanel::class, byId(R.id.buttonPanel), sameAsParent())
))