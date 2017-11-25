package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
interface ListObserver<in T> {
    fun itemAdded(element: T)
    fun itemAddedAt(index: Int, element: T)
    fun itemsAdded(elements: Collection<T>)
    fun itemsAddedAt(index: Int, elements: Collection<T>)
    fun itemSetAt(index: Int, element: T)
    fun itemRemoved(element: T)
    fun listCleared()
}