package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.utility.Drop;

import java.util.List;

public class ExistingDropAdapter extends RecyclerView.Adapter<ExistingDropAdapter.ViewHolder> {
    private final List<Drop> drops;
    private final int editingIndex;
    private final DropLocationListener listener;
    
    interface DropLocationListener {
        void onEditLocation(int position);
        void onRemoveLocation(int position);
    }
    
    public ExistingDropAdapter(List<Drop> drops, int editingIndex, DropLocationListener listener) {
        this.drops = drops;
        this.editingIndex = editingIndex;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_existing_drop_location, parent, false);
        return new ViewHolder(view);
    }
    
    @Override 
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drop drop = drops.get(position);
        holder.addressText.setText(drop.getAddress());
        holder.indexText.setText(String.valueOf(position + 1));
        
        // Enable/disable edit based on editing index
        holder.editButton.setEnabled(position == editingIndex);
        holder.editButton.setOnClickListener(v -> listener.onEditLocation(position));
        
        holder.removeButton.setOnClickListener(v -> listener.onRemoveLocation(position));
    }
    
    @Override
    public int getItemCount() {
        return drops.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView addressText;
        TextView indexText;
        ImageButton editButton;
        ImageButton removeButton;
        
        ViewHolder(View view) {
            super(view);
            addressText = view.findViewById(R.id.drop_address);
            indexText = view.findViewById(R.id.drop_index);
            editButton = view.findViewById(R.id.edit_button);
            removeButton = view.findViewById(R.id.remove_button);
        }
    }
}