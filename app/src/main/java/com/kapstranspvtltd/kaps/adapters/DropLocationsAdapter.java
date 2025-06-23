package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.DropLocation;

import java.util.Collections;
import java.util.List;

//public class DropLocationsAdapter extends RecyclerView.Adapter<DropLocationsAdapter.ViewHolder>
//        implements ItemTouchHelperAdapter {
//
//    private List<DropLocation> drops;
//    private OnDropClickListener listener;
//
//    public DropLocationsAdapter(List<DropLocation> drops, OnDropClickListener listener) {
//        this.drops = drops;
//        this.listener = listener;
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//            .inflate(R.layout.item_drop_location, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        DropLocation drop = drops.get(position);
//        holder.bind(drop, position);
//    }
//
//    @Override
//    public int getItemCount() {
//        return 0;
//    }
//
//    @Override
//    public boolean onItemMove(int fromPosition, int toPosition) {
//        Collections.swap(drops, fromPosition, toPosition);
//        notifyItemMoved(fromPosition, toPosition);
//        return true;
//    }
//
//    public void addDrop(DropLocation drop) {
//        drops.add(drop);
//        notifyItemInserted(drops.size() - 1);
//    }
//
//    public void updateDrop(int position, DropLocation drop) {
//        drops.set(position, drop);
//        notifyItemChanged(position);
//    }
//
//    class ViewHolder extends RecyclerView.ViewHolder {
//        // ViewHolder implementation
//    }
//}