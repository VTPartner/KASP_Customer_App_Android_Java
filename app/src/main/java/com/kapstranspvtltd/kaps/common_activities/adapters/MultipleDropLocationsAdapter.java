package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.utility.Drop;

import java.util.List;

public class MultipleDropLocationsAdapter extends RecyclerView.Adapter<MultipleDropLocationsAdapter.ViewHolder> {
    private List<Drop> drops;
    private OnDropLocationClickListener listener;
    private ItemTouchHelper touchHelper;

    public interface OnDropLocationClickListener {
        void onEditClick(Drop drop, int position);
        void onDeleteClick(Drop drop, int position);
    }

    public MultipleDropLocationsAdapter(List<Drop> drops, OnDropLocationClickListener listener) {
        this.drops = drops;
        this.listener = listener;
    }

    public void setTouchHelper(ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_multiple_drop_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drop drop = drops.get(position);
        holder.tvAddress.setText(drop.getAddress());
        holder.tvDropNumber.setText("Drop " + (position + 1));
        
        if (!TextUtils.isEmpty(drop.getRname()) && !TextUtils.isEmpty(drop.getRmobile())) {
            holder.tvReceiverDetails.setText(drop.getRname() + " • " + drop.getRmobile());
            holder.tvReceiverDetails.setVisibility(View.VISIBLE);
        } else {
            holder.tvReceiverDetails.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(drop, position);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(drop, position);
        });

        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                touchHelper.startDrag(holder);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return drops.size();
    }

    public void updateDrops(List<Drop> newDrops) {
        drops.clear();
        drops.addAll(newDrops);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDropNumber, tvAddress, tvReceiverDetails;
        ImageView btnEdit, btnDelete, dragHandle;

        ViewHolder(View view) {
            super(view);
            tvDropNumber = view.findViewById(R.id.tvDropNumber);
            tvAddress = view.findViewById(R.id.tvAddress);
            tvReceiverDetails = view.findViewById(R.id.tvReceiverDetails);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnDelete = view.findViewById(R.id.btnDelete);
            dragHandle = view.findViewById(R.id.dragHandle);
        }
    }
}