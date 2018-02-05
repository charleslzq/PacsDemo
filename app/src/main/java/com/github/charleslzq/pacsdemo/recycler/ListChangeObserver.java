package com.github.charleslzq.pacsdemo.recycler;

import java.util.Collection;

/**
 * Created by charleslzq on 18-2-5.
 */

interface ListChangeObserver<E> {
    void onInsert(int position, E data);

    void onInsert(int position, Collection<? extends E> data);

    void onRemove(int position, E data);

    void onUpdate(int position, E newData);

    void onDataChange();

    void onClear(int position, int count);
}
