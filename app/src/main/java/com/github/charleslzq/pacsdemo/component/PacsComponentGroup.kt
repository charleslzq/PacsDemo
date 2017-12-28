package com.github.charleslzq.pacsdemo.component

import android.view.View
import com.github.charleslzq.kotlin.react.ComponentGroup
import com.github.charleslzq.kotlin.react.EventBus
import com.github.charleslzq.pacsdemo.component.store.PacsStore

/**
 * Created by charleslzq on 17-12-4.
 */
abstract class PacsComponentGroup<V>(
        view: V,
        pacsStore: PacsStore,
        config: List<Sub<V, PacsStore, *, *, *>>,
        protected val dispatch: (Any) -> Unit = { EventBus.post(it) }
) : ComponentGroup<V, PacsStore>(view, pacsStore, config)
        where V : View