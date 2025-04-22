package com.kapstranspvtltd.kaps.common_activities.adapters;

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
import com.kapstranspvtltd.kaps.common_activities.models.OtherService;

import java.util.List;

public class OtherServiceAdapter extends RecyclerView.Adapter<OtherServiceAdapter.ViewHolder> {
    private List<OtherService> services;
    private Context context;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(OtherService service);
    }

    public OtherServiceAdapter(Context context, List<OtherService> services, OnServiceClickListener listener) {
        this.context = context;
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_other_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OtherService service = services.get(position);
        holder.serviceName.setText(service.getServiceName());

        // Format prices
        String priceText = String.format("Base Price: ₹%.2f\nPer Hour: ₹%.2f",
                service.getServiceBasePrice(),
                service.getPricePerHour());
        holder.servicePrice.setText(priceText);

        // Load image using Glide
        Glide.with(context)
                .load(service.getServiceImage())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.error_image)
                .into(holder.serviceImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public void updateServices(List<OtherService> newServices) {
        this.services = newServices;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImage;
        TextView serviceName, serviceDescription, servicePrice;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            servicePrice = itemView.findViewById(R.id.servicePrice);
        }
    }
}