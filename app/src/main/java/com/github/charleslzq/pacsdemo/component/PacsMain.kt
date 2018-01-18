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
) : PacsComponent<View>(mainView, pacsStore) {
    init {
        bind {
            child { ThumbList(byId(R.id.thumbList), store) }
            child { ViewSelector(byId(R.id.viewSelector), store) }
            child { ButtonPanel(byId(R.id.buttonPanel), store) }
        }
    }
}