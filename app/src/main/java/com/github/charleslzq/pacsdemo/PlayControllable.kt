package com.github.charleslzq.pacsdemo

/**
 * Created by charleslzq on 17-11-22.
 */
interface PlayControllable {
    fun play()
    fun pause()
    fun reset()
    fun isRunning(): Boolean
}