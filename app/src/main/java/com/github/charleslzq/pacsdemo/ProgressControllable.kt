package com.github.charleslzq.pacsdemo

/**
 * Created by charleslzq on 17-11-22.
 */
interface ProgressControllable : PlayControllable {
    fun changeProgress(progress: Int)
}