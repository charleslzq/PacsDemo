package com.github.charleslzq.pacsdemo.component.base

import android.view.View
import com.github.charleslzq.pacsdemo.component.observe.ObservableStatus.Companion.getDelegate
import kotlin.reflect.KProperty0

/**
 * Created by charleslzq on 17-11-27.
 */
abstract class Component<out V, out S>(
        val view: V,
        val state: S
) where V : View {

    fun <T> onStateChange(kProperty0: KProperty0<T>, ignoreUnchanged: Boolean = true, handler: (Triple<T, T, Boolean>) -> Unit) {
        handler(Triple(kProperty0.get(), kProperty0.get(), true))
        getDelegate(kProperty0)?.onChange {
            if (!ignoreUnchanged || it.first != it.second) {
                handler(Triple(it.first, it.second, false))
            }
        }
    }

    fun onStatesChange(vararg kProperty0: KProperty0<*>, ignoreUnchanged: Boolean = true, handler: (Boolean) -> Unit) {
        handler(true)
        kProperty0.forEach {
            getDelegate(it)?.onChange {
                if (!ignoreUnchanged || it.first != it.second) {
                    handler(false)
                }
            }
        }
    }
}