package com.github.charleslzq.pacsdemo.support

import android.os.Binder
import io.reactivex.Observable

/**
 * Created by charleslzq on 17-12-20.
 */
class ObservableService<T>(private val creator: () -> T) : Binder() {
    fun get(): Observable<T> = Observable.fromCallable(creator)
}