package com.kapstranspvtltd.kaps.adapters;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.LocationViewModel;
import com.kapstranspvtltd.kaps.MyApplication;
import com.kapstranspvtltd.kaps.activities.models.AllVehicleModel;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ItemGoodsVehiclesBinding;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GoodsVehicleAdapter extends ListAdapter<AllVehicleModel, GoodsVehicleAdapter.GoodsVehicleViewHolder> {
    private final Context context;
    private final OnVehicleClickListener onVehicleClick;
    private final float totalDistance;
    private final OnInfoClickListener onInfoClick;
    private int selectedPosition = -1;
    private final LocationViewModel locationViewModel;

    public interface OnVehicleClickListener {
        void onVehicleClick(AllVehicleModel vehicle);
    }

    public interface OnInfoClickListener {
        void onInfoClick(String sizeImageUrl);
    }

    public GoodsVehicleAdapter(Context context, 
                             OnVehicleClickListener onVehicleClick,
                             float totalDistance,
                             OnInfoClickListener onInfoClick) {
        super(new VehicleDiffCallback());
        this.context = context;
        this.onVehicleClick = onVehicleClick;
        this.totalDistance = totalDistance;
        this.onInfoClick = onInfoClick;
        this.locationViewModel = ((MyApplication) context.getApplicationContext()).locationViewModel;
    }

    @NonNull
    @Override
    public GoodsVehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoodsVehiclesBinding binding = ItemGoodsVehiclesBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new GoodsVehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GoodsVehicleViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class GoodsVehicleViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoodsVehiclesBinding binding;

        public GoodsVehicleViewHolder(ItemGoodsVehiclesBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int previousSelected = selectedPosition;
                    selectedPosition = position;
                    notifyItemChanged(previousSelected);
                    notifyItemChanged(selectedPosition);
                    onVehicleClick.onVehicleClick(getItem(position));
                }
            });
        }

        public void bind(AllVehicleModel vehicle) {
            binding.getRoot().setCardBackgroundColor(
                getBindingAdapterPosition() == selectedPosition ?
                binding.getRoot().getContext().getColor(R.color.light_blue) :
                binding.getRoot().getContext().getColor(R.color.white)
            );

            binding.nameText.setText(vehicle.getVehicleName());
            binding.capacityText.setText(vehicle.getVehicleWeight() + " Kgs");
            binding.arrivalTimeText.setText("₹" + vehicle.getBasePrice());

            Glide.with(binding.getRoot())
                .load(vehicle.getVehicleImage())
                .placeholder(R.drawable.placeholder)
                .into(binding.vehicleImage);

            binding.infoIcon.setOnClickListener(v -> 
                onInfoClick.onInfoClick(vehicle.getVehicleSizeImage())
            );

            calculateDistanceAndTimeWithGoogleMaps(vehicle.getPerKmPrice(), vehicle.getBasePrice());
        }

        private void calculateDistanceAndTimeWithGoogleMaps(double perKmPrice, double basePrice) {
            Location pickupLocation = locationViewModel.getPickupLocation();
            Location dropLocation = locationViewModel.getDropLocation();

            if (pickupLocation == null || dropLocation == null) {
                return;
            }

            String origin = pickupLocation.getLatitude() + "," + pickupLocation.getLongitude();
            String destination = dropLocation.getLatitude() + "," + dropLocation.getLongitude();
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + origin + "&" +
                    "destinations=" + destination + "&" +
                    "mode=driving&" +
                    "key=" + APIClient.MAP_KEY;

            makeRequest(url, perKmPrice, basePrice, 0);
        }

        private void makeRequest(String url, double perKmPrice, double basePrice, int retryCount) {
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject element = response.getJSONArray("rows")
                            .getJSONObject(0)
                            .getJSONArray("elements")
                            .getJSONObject(0);

                        if (element.getString("status").equals("OK")) {
                            int distanceInMeters = element.getJSONObject("distance").getInt("value");
                            double totalPrice = calculateTotalPriceNew(perKmPrice, basePrice, distanceInMeters);
                            binding.priceText.setText(String.format("₹%.1f", totalPrice));
                        }
                    } catch (Exception e) {
                        Log.e("DistanceMatrix", "Error parsing response: " + e.getMessage());
                    }
                },
                error -> Log.e("DistanceMatrix", "Network Error: " + error.getMessage())
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(itemView.getContext()).addToRequestQueue(request);
        }

        private double calculateTotalPriceNew(double perKmPrice, double basePrice, int distanceInMeters) {
            double distanceInKm = distanceInMeters / 1000.0;
            return distanceInKm * perKmPrice;
        }
    }

    private static class VehicleDiffCallback extends DiffUtil.ItemCallback<AllVehicleModel> {
        @Override
        public boolean areItemsTheSame(@NonNull AllVehicleModel oldItem, @NonNull AllVehicleModel newItem) {
            return oldItem.getVehicleId() == newItem.getVehicleId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull AllVehicleModel oldItem, @NonNull AllVehicleModel newItem) {
            return oldItem.equals(newItem);
        }
    }
}