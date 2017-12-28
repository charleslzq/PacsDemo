package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.kotlin.react.Component
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.store.PacsStore

/**
 * Created by charleslzq on 17-11-30.
 */
abstract class PacsComponent<out V>(
        view: V,
        pacsStore: PacsStore,
        protected val dispatch: (Any) -> Unit = { EventBus.post(it) }
) : Component<V, PacsStore>(view, pacsStore)
        where V : View