package com.kapstranspvtltd.kaps.activities.pickup_activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.models.VehicleModel;

import java.text.DecimalFormat;
import java.util.List;

public class GoodsVehiclesNewAdapter extends RecyclerView.Adapter<GoodsVehiclesNewAdapter.ViewHolder> {
    private List<VehicleModel> vehicles;
    private Context context;
    private int selectedPosition = -1;
    private OnVehicleSelectedListener listener;
    private double totalDistance;

    private static final long CLICK_DELAY = 300; // 300ms delay for double click
    private long lastClickTime = 0;
    private int lastClickPosition = -1;


    public interface OnVehicleSelectedListener {
        void onVehicleSelected(VehicleModel vehicle, int position);

    }

    public GoodsVehiclesNewAdapter(Context context, List<VehicleModel> vehicles, OnVehicleSelectedListener listener,double totalDistance) {
        this.context = context;
        this.vehicles = vehicles;
        this.listener = listener;
        this.totalDistance = totalDistance;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goods_vehicle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VehicleModel vehicle = vehicles.get(position);
        
        // Load vehicle image using Glide
        Glide.with(context)
            .load(vehicle.getVehicleImage())
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(holder.ivVehicle);

        holder.tvVehicleName.setText(vehicle.getVehicleName());
        holder.tvVehicleWeight.setText(vehicle.getWeight() + " Kg");
        holder.tvBaseFare.setText("Base Fare: ₹" + vehicle.getBaseFare());
        
        // Calculate total price
        double totalPrice = vehicle.getPricePerKm() ;
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedPrice = df.format(totalPrice);
        holder.tvTotalPrice.setText("₹" + formattedPrice);

        // Set selection state
        holder.itemView.setBackgroundResource(
            position == selectedPosition ? 
            R.color.selected_item_background : 
            android.R.color.transparent
        );

        holder.itemView.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_DELAY && lastClickPosition == position) {
                // Double click detected
                if (listener instanceof AllGoodsVehiclesActivity) {
                    ((AllGoodsVehiclesActivity) listener).showVehicleDetailsBottomSheet(vehicle);
                }
            } else {
                // Single click - handle selection
                int previousSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                listener.onVehicleSelected(vehicle, position);
            }
            lastClickTime = currentTime;
            lastClickPosition = position;
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public void updateVehicles(List<VehicleModel> newVehicles) {
        this.vehicles = newVehicles;
        notifyDataSetChanged();
    }

    public VehicleModel getSelectedVehicle() {
        return selectedPosition != -1 ? vehicles.get(selectedPosition) : null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivVehicle;
        TextView tvVehicleName;
        TextView tvVehicleWeight;
        TextView tvBaseFare;
        TextView tvTotalPrice;

        ViewHolder(View itemView) {
            super(itemView);
            ivVehicle = itemView.findViewById(R.id.ivVehicle);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvVehicleWeight = itemView.findViewById(R.id.tvVehicleWeight);
            tvBaseFare = itemView.findViewById(R.id.tvBaseFare);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }
    }
}