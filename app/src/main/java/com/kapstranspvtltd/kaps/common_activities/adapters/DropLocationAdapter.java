package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.utility.Drop;

import java.util.Collections;
import java.util.List;

public class DropLocationAdapter extends RecyclerView.Adapter<DropLocationAdapter.ViewHolder> {
    private List<Drop> dropLocations;
    private OnDropLocationListener listener;
    private int maxDrops;
    private boolean cabService;

    public interface OnDropLocationListener {
        void onEditClick(Drop drop, int position);
        void onDeleteClick(Drop drop, int position);
        void onDropMoved(int fromPosition, int toPosition);
    }

    public DropLocationAdapter(List<Drop> dropLocations, OnDropLocationListener listener, int maxDrops, boolean cabService) {
        this.dropLocations = dropLocations;
        this.listener = listener;
        this.maxDrops = maxDrops;
        this.cabService = cabService;
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
        Drop drop = dropLocations.get(position);
        holder.txtDropNumber.setText("Drop Location #" + (position + 1));
        holder.txtDropAddress.setText(drop.getAddress());

        // Show receiver details for non-cab service
        if (!cabService && !TextUtils.isEmpty(drop.getRname())) {
            holder.txtReceiverDetails.setVisibility(View.VISIBLE);
            holder.txtReceiverDetails.setText(drop.getRname() + " - " + drop.getRmobile());
        } else {
            holder.txtReceiverDetails.setVisibility(View.GONE);
        }

        // Show delete button only for additional drops
        holder.imgDelete.setVisibility(position > 0 ? View.VISIBLE : View.GONE);

        // Show drag handle only for multiple drops
        holder.imgDragHandle.setVisibility(dropLocations.size() > 1 ? View.VISIBLE : View.GONE);

        holder.imgEdit.setOnClickListener(v -> listener.onEditClick(drop, position));
        holder.imgDelete.setOnClickListener(v -> listener.onDeleteClick(drop, position));
    }

    @Override
    public int getItemCount() {
        return dropLocations.size();
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(dropLocations, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(dropLocations, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        listener.onDropMoved(fromPosition, toPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtDropNumber, txtDropAddress, txtReceiverDetails;
        ImageView imgEdit, imgDelete, imgDragHandle;

        ViewHolder(View view) {
            super(view);
            txtDropNumber = view.findViewById(R.id.tvDropNumber);
            txtDropAddress = view.findViewById(R.id.tvAddress);
            txtReceiverDetails = view.findViewById(R.id.txtReceiverDetails);
            imgEdit = view.findViewById(R.id.btnEdit);
            imgDelete = view.findViewById(R.id.btnDelete);
            imgDragHandle = view.findViewById(R.id.dragHandle);
        }
    }
}