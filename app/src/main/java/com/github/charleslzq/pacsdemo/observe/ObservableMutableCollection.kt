package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
open class ObservableMutableCollection<T, C, O>(
        protected val internal: C
): MutableCollection<T>, WithObservers<O>
where C: MutableCollection<T>, O: MutableCollectionObserver<C, T>
{
    override val size: Int
        get() = internal.size
    protected val observers: MutableMap<String, O> = emptyMap<String, O>().toMutableMap()

    override fun registerObserver(observer: O, name: String) {
        observers[name] = observer
    }

    override fun removeObserver(name: String) {
        observers.remove(name)
    }

    override fun clearObservers() {
        observers.clear()
    }

    override fun add(element: T): Boolean {
        if (internal.add(element)) {
            observers.values.forEach { it.itemAdded(element) }
            return true
        }
        return false
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (internal.addAll(elements)) {
            observers.values.forEach { it.itemsAdded(elements) }
            return true
        }
        return false
    }

    override fun clear() {
        internal.clear()
        observers.values.forEach { it.onCollectionCleared() }
    }

    override fun iterator(): MutableIterator<T> = internal.iterator()

    override fun remove(element: T): Boolean {
        if (internal.remove(element)) {
            observers.values.forEach { it.itemRemoved(element) }
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val removedItems = internal.intersect(elements)
        if (internal.removeAll(elements)) {
            observers.values.forEach { observer ->
                removedItems.forEach { observer.itemRemoved(it) }
            }
            return true
        }
        return false
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val removedItems = internal.minus(elements)
        if (internal.retainAll(elements)) {
            observers.values.forEach { observer ->
                removedItems.forEach { observer.itemRemoved(it) }
            }
            return true
        }
        return false
    }

    override fun contains(element: T): Boolean = internal.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = internal.containsAll(elements)

    override fun isEmpty(): Boolean = internal.isEmpty()
}