package com.github.charleslzq.pacsdemo.support

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by charleslzq on 17-12-22.
 */
interface RxScheduleSupport {
    fun <T> callOnIo(callable: () -> T): T {
        return Observable.fromCallable(callable).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).blockingSingle()
    }

    fun runOnIo(runnable: () -> Unit) {
        Observable.just(1).observeOn(Schedulers.io()).subscribe { runnable() }
    }
}