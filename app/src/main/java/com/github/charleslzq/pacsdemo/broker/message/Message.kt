package com.github.charleslzq.pacsdemo.broker.message

/**
 * Created by charleslzq on 17-11-14.
 */
data class Message<T> (
        var headers: MutableMap<String, String> = emptyMap<String, String>().toMutableMap(),
        var payload: T
)