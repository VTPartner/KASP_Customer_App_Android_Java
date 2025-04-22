package com.kapstranspvtltd.kaps.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.activities.models.VehicleModel;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ItemVehicleBinding;

import java.text.DecimalFormat;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private Context mContext;
    private List<VehicleModel> vehicleList;
    private RecyclerTouchListener listener;

    private int previousSelected = 0;
    private double distance;

    public interface RecyclerTouchListener {
        void onClickVehicleItem(VehicleModel item, int position);
        void onClickVehicleInfo(VehicleModel item, int position);
    }

    public VehicleAdapter(Context context, List<VehicleModel> vehicles, RecyclerTouchListener listener, double distance) {
        this.mContext = context;
        this.vehicleList = vehicles;
        this.listener = listener;
        this.distance = distance;
        if (vehicles != null && !vehicles.isEmpty()) {
            vehicles.get(0).setSelected(true);
            // Notify the listener about initial selection
            if (listener != null) {
                listener.onClickVehicleItem(vehicles.get(0),0);
                //listener.onClickVehicleInfo(vehicles.get(0), 0);
            }
        }
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VehicleModel vehicle = vehicleList.get(position);

        // Set vehicle name
        holder.binding.txtVehicleName.setText(vehicle.getVehicleName());

        // Load vehicle image
        Glide.with(mContext)
                .load(vehicle.getVehicleImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(holder.binding.imgVehicle);

        // Calculate price and time
        double totalPrice = calculatePrice(vehicle);
        String time = calculateTime(vehicle);

//        holder.binding.txtBasePrice.setText(sessionManager.getStringData(SessionManager.currency) +
//                new DecimalFormat("##.##").format(totalPrice));
//        holder.binding.txtTime.setText(time);
holder.binding.txtBasePrice.setText("₹"+vehicle.getBaseFare()+"");
holder.binding.txtPricePerKm.setText("₹"+vehicle.getPricePerKm()+"");
        // Update UI based on selection state
        updateSelectionState(holder, vehicle);

        // Click listeners
        holder.binding.layoutMain.setOnClickListener(v -> {
            if (position != previousSelected) {
                vehicleList.get(previousSelected).setSelected(false);
                notifyItemChanged(previousSelected);
                vehicle.setSelected(true);
                notifyItemChanged(position);
                previousSelected = position;
//                listener.onClickVehicleInfo(vehicle, position);
                listener.onClickVehicleItem(vehicle, position);
            }
        });

        holder.binding.imgInfo.setOnClickListener(v -> {
            if (vehicle.isSelected()) {
//                listener.onClickVehicleItem(vehicle, position);
                listener.onClickVehicleInfo(vehicle, position);
            }
        });
    }

    private void updateSelectionState(VehicleViewHolder holder, VehicleModel vehicle) {
        if (vehicle.isSelected()) {
            holder.binding.txtVehicleName.setTextColor(ContextCompat.getColor(mContext, R.color.black));
//            holder.binding.txtTime.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.binding.txtBasePrice.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            holder.binding.imgVehicle.setBackgroundResource(R.drawable.circlebg);
            holder.binding.imgInfo.setVisibility(View.VISIBLE);
        } else {
            holder.binding.imgVehicle.setBackgroundResource(0);
            holder.binding.txtVehicleName.setTextColor(ContextCompat.getColor(mContext, R.color.colorgrey2));
//            holder.binding.txtTime.setTextColor(ContextCompat.getColor(mContext, R.color.colorgrey2));
            holder.binding.txtBasePrice.setTextColor(ContextCompat.getColor(mContext, R.color.colorgrey2));
            holder.binding.imgInfo.setVisibility(View.GONE);
        }
    }

    private double calculatePrice(VehicleModel vehicle) {
        if (distance <= vehicle.getStartDistance()) {
            return vehicle.getBaseFare();
        } else {
            double extraDistance = distance - vehicle.getStartDistance();
            return vehicle.getBaseFare() + (extraDistance * vehicle.getPricePerKm());
        }
    }

    private String calculateTime(VehicleModel vehicle) {
        Double time = distance * vehicle.getTimeTaken();
        if (time < 60) {
            return new DecimalFormat("##").format(time) + " mins";
        } else {
            double hours = time / 60;
            return new DecimalFormat("##.##").format(hours) + " Hours";
        }
    }

    @Override
    public int getItemCount() {
        return vehicleList != null ? vehicleList.size() : 0;
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        ItemVehicleBinding binding;

        VehicleViewHolder(ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public void updateDistance(double newDistance) {
        this.distance = newDistance;
        notifyDataSetChanged();
    }
}