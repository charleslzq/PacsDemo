package com.github.charleslzq.pacsdemo.observe

import java.util.*

/**
 * Created by charleslzq on 17-11-25.
 */
interface WithObservers<in T> {
    fun registerObserver(observer: T, name: String = UUID.randomUUID().toString())
    fun removeObserver(name: String)
    fun clearObservers()
}