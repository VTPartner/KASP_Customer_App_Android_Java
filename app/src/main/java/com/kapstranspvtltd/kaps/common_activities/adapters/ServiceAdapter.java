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
import com.kapstranspvtltd.kaps.common_activities.models.Service;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private Context context;
    private List<Service> services;
    private OnServiceClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Track selected position

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceAdapter(Context context, List<Service> services, OnServiceClickListener listener) {
        this.context = context;
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_services, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = services.get(position);
        holder.bind(service, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    public void updateServices(List<Service> newServices) {
        this.services = newServices;
        selectedPosition = RecyclerView.NO_POSITION; // Reset selection
        notifyDataSetChanged();
    }

    // Method to get selected service
    public Service getSelectedService() {
        if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < services.size()) {
            return services.get(selectedPosition);
        }
        return null;
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private ImageView serviceImage;
        private TextView serviceName;
        private TextView serviceDescription;
        private TextView servicePrice;
        private ImageView checkmark;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            serviceDescription = itemView.findViewById(R.id.serviceDescription);
            servicePrice = itemView.findViewById(R.id.servicePrice);
            checkmark = itemView.findViewById(R.id.checkmark);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    // Update selection
                    int previousSelected = selectedPosition;
                    selectedPosition = position;

                    // Notify items changed to update their appearance
                    notifyItemChanged(previousSelected);
                    notifyItemChanged(selectedPosition);

                    listener.onServiceClick(services.get(position));
                }
            });
        }

        void bind(Service service, boolean isSelected) {
            serviceName.setText(service.getSubCatName());

            String priceText = String.format("Base Price: ₹%s\nPer Hour: ₹%s",
                    service.getServiceBasePrice(),
                    service.getPricePerHour());
            servicePrice.setText(priceText);

            serviceDescription.setVisibility(View.GONE);

            // Show/hide checkmark based on selection state
            checkmark.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Optional: Change card background when selected
            itemView.setBackgroundColor(isSelected ?
                    context.getResources().getColor(R.color.selected_item_background) :
                    context.getResources().getColor(android.R.color.white));

            Glide.with(context)
                    .load(service.getImage())
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.error_image)
                    .into(serviceImage);
        }
    }
}