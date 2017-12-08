package com.github.charleslzq.pacsdemo.component.store

/**
 * Created by charleslzq on 17-12-8.
 */
data class ImagePlayModel(
        val duration: Int = 40,
        val currentIndex: Int = -1,
        val playing: Boolean = false
)