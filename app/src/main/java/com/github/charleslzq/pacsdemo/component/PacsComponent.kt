package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.pacsdemo.component.store.PacsStore
import com.github.charleslzq.pacsdemo.support.GlobalDispatch

/**
 * Created by charleslzq on 17-11-30.
 */
abstract class PacsComponent<out V>(
        view: V,
        pacsStore: PacsStore,
        protected val dispatch: (Any) -> Unit = GlobalDispatch.DEBUG_DISPATCH
) : Component<V, PacsStore>(view, pacsStore)
        where V : View