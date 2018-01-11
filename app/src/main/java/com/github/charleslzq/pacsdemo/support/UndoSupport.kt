package com.github.charleslzq.pacsdemo.support

import java.util.*

/**
 * Created by charleslzq on 17-12-29.
 */
class UndoSupport<T> {
    private val doneStack = Stack<T>()
    private val canceledStack = Stack<T>()

    fun copyFrom(another: UndoSupport<T>) {
        reset()
        another.doneStack.forEach { doneStack.push(it) }
        another.canceledStack.forEach { canceledStack.push(it) }
    }

    fun reset() {
        doneStack.clear()
        canceledStack.clear()
    }

    fun canUndo() = doneStack.isNotEmpty()

    fun canRedo() = canceledStack.isNotEmpty()

    fun done(data: T) = doneStack.push(data)

    fun undo() = if (canUndo()) {
        canceledStack.push(doneStack.pop())
        if (doneStack.isNotEmpty()) {
            doneStack.peek()
        } else {
            null
        }
    } else {
        null
    }

    fun redo() = if (canRedo()) {
        canceledStack.pop().also { done(it) }
    } else {
        null
    }

    fun generate(initialValue: () -> T, generator: (T) -> T) = generator(if (doneStack.isEmpty()) {
        initialValue()
    } else {
        doneStack.peek()
    }).also { done(it) }
}