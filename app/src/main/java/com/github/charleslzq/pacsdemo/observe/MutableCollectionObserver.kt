package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
interface MutableCollectionObserver<in C, T> where C: Collection<T> {
    fun itemAdded(element: T)
    fun itemsAdded(elements: Collection<T>)
    fun itemRemoved(element: T)
    fun onCollectionCleared()
    fun onReset(oldCollection: C, newCollection: C)
}