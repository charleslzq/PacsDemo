package com.github.charleslzq.pacsdemo.observe

/**
 * Created by charleslzq on 17-11-25.
 */
class ObservableMutableList<T>(
        private val internalList: MutableList<T> = emptyList<T>().toMutableList()
): MutableList<T>, WithObservers<ListObserver<T>> {
    override val size: Int
        get() = internalList.size
    private val observers: MutableMap<String, ListObserver<T>> = emptyMap<String, ListObserver<T>>().toMutableMap()

    override fun registerObserver(observer: ListObserver<T>, name: String) {
        observers[name] = observer
    }

    override fun removeObserver(name: String) {
        observers.remove(name)
    }

    override fun clearObservers() {
        observers.clear()
    }

    override fun contains(element: T): Boolean = internalList.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = internalList.containsAll(elements)

    override fun get(index: Int): T = internalList[index]

    override fun indexOf(element: T): Int = internalList.indexOf(element)

    override fun isEmpty(): Boolean = internalList.isEmpty()

    override fun iterator(): MutableIterator<T> = internalList.iterator()

    override fun lastIndexOf(element: T): Int = internalList.lastIndexOf(element)

    override fun add(element: T): Boolean {
        if (internalList.add(element)) {
            observers.values.forEach { it.itemAdded(element) }
            return true
        }
        return false
    }

    override fun add(index: Int, element: T) {
        internalList.add(index, element)
        observers.values.forEach { it.itemAddedAt(index, element) }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        if (internalList.addAll(index, elements)) {
            observers.values.forEach { it.itemsAddedAt(index, elements) }
            return true
        }
        return false
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (internalList.addAll(elements)) {
            observers.values.forEach { it.itemsAdded(elements) }
            return true
        }
        return false
    }

    override fun listIterator(): MutableListIterator<T> = internalList.listIterator()

    override fun listIterator(index: Int): MutableListIterator<T> = internalList.listIterator(index)

    override fun remove(element: T): Boolean {
        if (internalList.remove(element)) {
            observers.values.forEach { it.itemRemoved(element) }
            return true
        }
        return false
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val removedItems = internalList.intersect(elements)
        if (internalList.removeAll(elements)) {
            observers.values.forEach { observer ->
                removedItems.forEach { observer.itemRemoved(it) }
            }
            return true
        }
        return false
    }

    override fun removeAt(index: Int): T {
        val element = internalList.removeAt(index)
        observers.values.forEach { it.itemRemoved(element) }
        return element
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val removedItems = internalList.minus(elements)
        if (internalList.retainAll(elements)) {
            observers.values.forEach { observer ->
                removedItems.forEach { observer.itemRemoved(it) }
            }
            return true
        }
        return false
    }

    override fun set(index: Int, element: T): T {
        val oldValue = internalList.set(index, element)
        if (oldValue != element) {
            observers.values.forEach { it.itemSetAt(index, element) }
        }
        return oldValue
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = internalList.subList(fromIndex, toIndex)

    override fun clear() {
        internalList.clear()
        observers.values.forEach { it.listCleared() }
    }
}