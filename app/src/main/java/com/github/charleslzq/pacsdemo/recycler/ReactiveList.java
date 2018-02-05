package com.github.charleslzq.pacsdemo.recycler;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by charleslzq on 18-2-5.
 */

class ReactiveList<E> extends ArrayList<E> {
    private final List<? extends ListChangeObserver<E>> observers;

    ReactiveList(List<? extends ListChangeObserver<E>> observers) {
        this.observers = observers;
    }

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result) {
            for (ListChangeObserver<E> observer : observers) {
                observer.onInsert(size(), e);
            }
        }
        return result;
    }

    @Override
    public boolean remove(Object o) {
        int index = super.indexOf(o);
        boolean result = super.remove(o);
        if (result) {
            for (ListChangeObserver<E> observer : observers) {
                observer.onRemove(index, (E) o);
            }
        }
        return result;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        boolean result = super.addAll(c);
        if (result) {
            for (ListChangeObserver<E> observer : observers) {
                observer.onInsert(size(), c);
            }
        }
        return result;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        boolean result = super.addAll(index, c);
        if (result) {
            for (ListChangeObserver<E> observer : observers) {
                observer.onInsert(index, c);
            }
        }
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean result = super.removeAll(c);
        for (ListChangeObserver<E> observer : observers) {
            observer.onDataChange();
        }
        return result;
    }

    @Override
    public void clear() {
        int size = size();
        super.clear();
        for (ListChangeObserver<E> observer : observers) {
            observer.onClear(0, size);
        }
    }

    @Override
    public E set(int index, E element) {
        E previous = super.set(index, element);
        for (ListChangeObserver<E> observer : observers) {
            observer.onUpdate(index, element);
        }
        return previous;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        for (ListChangeObserver<E> observer : observers) {
            observer.onInsert(index, element);
        }
    }

    @Override
    public E remove(int index) {
        E removed = super.remove(index);
        for (ListChangeObserver<E> observer : observers) {
            observer.onClear(index, 1);
        }
        return removed;
    }
}
