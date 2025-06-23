package com.kapstranspvtltd.kaps.common_activities.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.utility.Drop;

import java.util.ArrayList;
import java.util.List;

public class SelectedDropLocationAdapter extends RecyclerView.Adapter<SelectedDropLocationAdapter.ViewHolder> {
    private List<Drop> dropLocations;
    private OnDropLocationActionListener listener;

    public interface OnDropLocationActionListener {
        void onEditLocation(int position, Drop drop);
        void onRemoveLocation(int position);
    }

    public SelectedDropLocationAdapter(OnDropLocationActionListener listener) {
        this.dropLocations = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_drop_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drop drop = dropLocations.get(position);
        holder.txtAddress.setText(drop.getAddress());
        holder.txtReceiverDetails.setText(String.format("%s - %s", drop.getRname(), drop.getRmobile()));
        
        holder.btnEdit.setOnClickListener(v -> listener.onEditLocation(position, drop));
        holder.btnRemove.setOnClickListener(v -> listener.onRemoveLocation(position));
    }

    @Override
    public int getItemCount() {
        return dropLocations.size();
    }

    public void updateDropLocations(List<Drop> drops) {
        this.dropLocations = drops;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtAddress, txtReceiverDetails;
        ImageView btnEdit, btnRemove;

        ViewHolder(View view) {
            super(view);
            txtAddress = view.findViewById(R.id.txtAddress);
            txtReceiverDetails = view.findViewById(R.id.txtReceiverDetails);
            btnEdit = view.findViewById(R.id.btnEdit);
            btnRemove = view.findViewById(R.id.btnRemove);
        }
    }
}