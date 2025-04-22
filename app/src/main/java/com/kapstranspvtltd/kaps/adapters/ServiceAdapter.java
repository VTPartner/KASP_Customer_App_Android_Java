package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.activities.models.AllServicesHome;
import com.kapstranspvtltd.kaps.R;

import java.util.ArrayList;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<AllServicesHome> services = new ArrayList<>();
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(AllServicesHome service);
    }

    public void setServices(List<AllServicesHome> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_grid, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        holder.bind(services.get(position));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView serviceImage;
        TextView serviceName;

        ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.serviceImage);
            serviceName = itemView.findViewById(R.id.serviceName);
            View overLayView = itemView.findViewById(R.id.coming_soon_lyt_overlay);
            LinearLayout overLayout = itemView.findViewById(R.id.coming_soon_lyt);


            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                AllServicesHome allServicesHome = services.get(position);
                int categoryId = allServicesHome.getCategoryId();
//                if(categoryId != 1){
//                    overLayView.setVisibility(View.VISIBLE);
//                    overLayout.setVisibility(View.VISIBLE);
//                }else{
//                    overLayView.setVisibility(View.GONE);
//                    overLayout.setVisibility(View.GONE);
//                }
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onServiceClick(services.get(position));
                }
            });
        }

        void bind(AllServicesHome service) {
            serviceName.setText(service.getCategoryName());
            Glide.with(itemView.getContext())
                    .load(service.getCategoryImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error_image)
                    .into(serviceImage);
        }
    }
}