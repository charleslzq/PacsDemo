package com.github.charleslzq.pacsdemo.support

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

/**
 * Created by charleslzq on 17-12-22.
 */
interface RxScheduleSupport {
    fun <T> callOnIo(callable: () -> T) = callOn(Schedulers.io(), callable)

    fun runOnIo(runnable: () -> Unit) = runOn(Schedulers.io(), runnable)

    fun <T> callOnCompute(callable: () -> T) = callOn(Schedulers.computation(), callable)

    fun runOnCompute(runnable: () -> Unit) = runOn(Schedulers.computation(), runnable)

    fun <T> callOn(scheduler: Scheduler = Schedulers.trampoline(), callable: () -> T): T {
        return Observable.just(1).observeOn(scheduler).map { callable() }.blockingSingle()
    }

    fun runOn(scheduler: Scheduler = Schedulers.trampoline(), runnable: () -> Unit) {
        Observable.just(1).observeOn(scheduler).subscribe { runnable() }
    }
}