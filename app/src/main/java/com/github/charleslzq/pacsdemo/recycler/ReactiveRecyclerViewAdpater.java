package com.github.charleslzq.pacsdemo.recycler;

import android.support.v7.widget.RecyclerView;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by charleslzq on 18-2-5.
 */

public abstract class ReactiveRecyclerViewAdpater<D, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements ListChangeObserver<D> {
    public final List<D> dataList = new ReactiveList<>(Collections.singletonList(this));

    @Override
    public final int getItemCount() {
        return dataList.size();
    }

    @Override
    public final void onInsert(int position, D data) {
        notifyItemInserted(position);
    }

    @Override
    public final void onInsert(int position, Collection<? extends D> data) {
        notifyItemRangeInserted(position, data.size());
    }

    @Override
    public final void onRemove(int position, D data) {
        notifyItemRemoved(position);
    }

    @Override
    public final void onUpdate(int position, D newData) {
        notifyItemChanged(position);
    }

    @Override
    public final void onDataChange() {
        notifyDataSetChanged();
    }

    @Override
    public final void onClear(int position, int count) {
        notifyItemRangeRemoved(position, count);
    }
}
