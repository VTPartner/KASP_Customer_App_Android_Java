package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityReviewMapBinding;
import com.kapstranspvtltd.kaps.databinding.ItemDropBinding;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReviewMapActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityReviewMapBinding binding;
    private GoogleMap gMap;
    private Pickup pickup;
    private Drop drop;
    private DropAdapter dropAdapter;

    private GeoApiContext geoApiContext;
    private List<Polyline> polylines = new ArrayList<>();

    private double totalDistance = 0;
    private long totalDuration = 0;

    private String exactTime = "",exactDistance="";

    private double totalDistanceValue = 0; // in kilometers
    private long totalDurationValue = 0;   // in minutes
    private boolean isOutstation = false;

    private static final double OUTSTATION_THRESHOLD_KM = 30.0;

    private ExecutorService executorService;
    boolean cabService;

    PreferenceManager preferenceManager;

    private Map<String, Marker> driverMarkers = new HashMap<>();
    private Handler markerUpdateHandler = new Handler(Looper.getMainLooper());
    private static final int MARKER_UPDATE_INTERVAL = 5000; // 5 seconds
    private boolean isTrackingDrivers = true;

    private boolean isPaused = false;

    private void startTrackingDrivers() {
        isTrackingDrivers = true;
        isPaused = false;

        markerUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTrackingDrivers && !isPaused) {
                    fetchNearbyDrivers();
                    markerUpdateHandler.postDelayed(this, MARKER_UPDATE_INTERVAL);
                }
            }
        }, MARKER_UPDATE_INTERVAL);
    }

    private void fetchNearbyDrivers() {
        // Create JSON parameters
        JSONObject params = new JSONObject();
        try {
            params.put("lat", pickup.getLat());
            params.put("lng", pickup.getLog());
            params.put("city_id", preferenceManager.getStringValue("city_id"));
            params.put("price_type", 1);
            params.put("radius_km", 5);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    APIClient.baseUrl + "get_nearby_drivers", params,
                    response -> {
                        try {
                            JSONArray driversArray = response.getJSONArray("nearby_drivers");
                            updateDriverMarkers(driversArray);
                        } catch (JSONException e) {
                            Log.e("ReviewMap", "Error parsing drivers: " + e.getMessage());
                        }
                    },
                    error -> Log.e("ReviewMap", "Error fetching drivers: " + error.getMessage())) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (JSONException e) {
            Log.e("ReviewMap", "Error creating params: " + e.getMessage());
        }
    }

    private void updateDriverMarkers(JSONArray drivers) {
        if (gMap == null) return;

        Set<String> updatedDriverIds = new HashSet<>();

        try {
            for (int i = 0; i < drivers.length(); i++) {
                JSONObject driver = drivers.getJSONObject(i);
                String driverId = driver.getString("goods_driver_id");
                double lat = driver.getDouble("latitude");
                double lng = driver.getDouble("longitude");
                String vehicleMapImage = driver.getString("vehicle_map_image");

                updatedDriverIds.add(driverId);
                LatLng position = new LatLng(lat, lng);

                Marker existingMarker = driverMarkers.get(driverId);
                if (existingMarker != null) {
                    // Animate existing marker
                    animateMarker(existingMarker, position);
                } else {
                    // Create new marker
                    createDriverMarker(driverId, position, vehicleMapImage);
                }
            }

            // Remove markers for drivers no longer in range
            Iterator<Map.Entry<String, Marker>> iterator = driverMarkers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Marker> entry = iterator.next();
                if (!updatedDriverIds.contains(entry.getKey())) {
                    entry.getValue().remove();
                    iterator.remove();
                }
            }
        } catch (JSONException e) {
            Log.e("ReviewMap", "Error updating markers: " + e.getMessage());
        }
    }

    private void createDriverMarker(String driverId, LatLng position, String vehicleMapImage) {
        if(vehicleMapImage == null|| vehicleMapImage.isEmpty() || vehicleMapImage.equalsIgnoreCase("NA")) return;
        // Load vehicle image from URL using Glide
        Glide.with(this)
                .asBitmap()
                .load(vehicleMapImage)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                        // Scale bitmap to appropriate size
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(resource, 100, 100, false);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(position)
                                .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap))
                                .flat(true);

                        Marker marker = gMap.addMarker(markerOptions);
                        if (marker != null) {
                            driverMarkers.put(driverId, marker);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });
    }

    private void animateMarker(final Marker marker, final LatLng toPosition) {
        final LatLng startPosition = marker.getPosition();
        final Handler handler = new Handler(Looper.getMainLooper());
        final long start = SystemClock.uptimeMillis();
        final float durationInMs = 1500f;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation(elapsed / durationInMs);

                double lat = t * toPosition.latitude + (1 - t) * startPosition.latitude;
                double lng = t * toPosition.longitude + (1 - t) * startPosition.longitude;
                marker.setPosition(new LatLng(lat, lng));

                // Rotate marker to face movement direction
                float bearing = computeBearing(startPosition, toPosition);
                marker.setRotation(bearing);

                if (t < 1.0) {
                    handler.postDelayed(this, 16); // 60fps
                }
            }
        });
    }

    private float computeBearing(LatLng start, LatLng end) {
        double lat1 = Math.toRadians(start.latitude);
        double lat2 = Math.toRadians(end.latitude);
        double lng1 = Math.toRadians(start.longitude);
        double lng2 = Math.toRadians(end.longitude);

        double dLng = lng2 - lng1;
        double y = Math.sin(dLng) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);
        return (float) Math.toDegrees(Math.atan2(y, x));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        // Initialize ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        pickup = getIntent().getParcelableExtra("pickup");
        drop = getIntent().getParcelableExtra("drop");
        cabService = getIntent().getBooleanExtra("cab",false);
        binding.txtPickaddress.setText(pickup.getAddress());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        String dropsValue = preferenceManager.getStringValue("multiple_drops", "3");
        int multipleDrops;
        try {
            multipleDrops = Integer.parseInt(dropsValue);
        } catch (NumberFormatException e) {
            multipleDrops = 3; // fallback value
        }
        binding.txtAddnewstop.setVisibility(dropList.size() <= multipleDrops ? View.VISIBLE : View.GONE);

        geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recyclerDrop.setLayoutManager(layoutManager);
        binding.recyclerDrop.setItemAnimator(new DefaultItemAnimator());

        setupItemTouchHelper();
    }

    private void setupClickListeners() {

        binding.txtPickaddress.setOnClickListener(v->{
            Toast.makeText(this, "Navigate back booking locations screen to edit pickup location", Toast.LENGTH_SHORT).show();
//            Glb.showPickup = true;
//            startActivity(new Intent(this, GoodsPickupMapLocationActivity.class));
//            finish();
//            finish();
        });
//        binding.btnProce.setOnClickListener(v -> startActivity(new Intent(this, BookingReviewScreenActivity.class)
        binding.btnProce.setOnClickListener(v ->{

                isPaused = true;
        isTrackingDrivers = false;
        markerUpdateHandler.removeCallbacksAndMessages(null);

                startActivity(new Intent(this, AllGoodsVehiclesActivity.class)
                .putExtra("cab",cabService)
                .putExtra("total_distance", totalDistanceValue)
                .putExtra("total_time", totalDuration)
                .putExtra("exact_time", exactTime)
                        .putExtra("exact_distance", exactDistance)


                .putExtra("pickup", pickup)
                .putExtra("drop", drop));
        });

        binding.txtAddnewstop.setOnClickListener(v -> {
            Glb.addStopClicked = true;
            finish();
        });
    }

    private void setupItemTouchHelper() {
        final int[] oldPos = new int[1];
        final int[] newPos = new int[1];

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                oldPos[0] = viewHolder.getAdapterPosition();
                newPos[0] = target.getAdapterPosition();
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.e("position", "-->" + direction);
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                moveItem(oldPos[0], newPos[0]);
            }
        });

        itemTouchHelper.attachToRecyclerView(binding.recyclerDrop);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
        updateMap(googleMap);
        startTrackingDrivers(); // Start tracking after map is ready
    }

    public void updateMap(GoogleMap googleMap) {
        if (googleMap != null) {
            googleMap.clear();
        }

        totalDistance = 0;
        totalDuration = 0;

        // Clear existing polylines
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

        // Add pickup marker
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(pickup.getLat(), pickup.getLog()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));

        if (dropList != null && !dropList.isEmpty()) {
            dropAdapter = new DropAdapter(dropList);
            binding.recyclerDrop.setAdapter(dropAdapter);

            // Add markers for all drop points
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            boundsBuilder.include(new LatLng(pickup.getLat(), pickup.getLog()));

            for (int i = 0; i < dropList.size(); i++) {
                Drop drop = dropList.get(i);
                Bitmap bitmap = drawTextToBitmap(this, R.drawable.ic_destination_long, String.valueOf(i + 1));
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(drop.getLat(), drop.getLog()))
                        .title("Drop " + (i + 1))
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                boundsBuilder.include(new LatLng(drop.getLat(), drop.getLog()));
            }

            // Draw routes between all points
            LatLng origin = new LatLng(pickup.getLat(), pickup.getLog());
            for (int i = 0; i < dropList.size(); i++) {
                Drop drop = dropList.get(i);
                LatLng destination = new LatLng(drop.getLat(), drop.getLog());
                getDirections(origin, destination, googleMap);
                origin = destination; // Set current destination as next origin
            }

            // Adjust camera to show all markers
            try {
                LatLngBounds bounds = boundsBuilder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                googleMap.animateCamera(cu);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*public void updateMap(GoogleMap googleMap) {

        if (googleMap != null) {
            googleMap.clear();
        }

        totalDistance = 0;
        totalDuration = 0;

        // Clear existing polylines
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();

        // Add pickup marker
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(pickup.getLat(), pickup.getLog()))
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));

        // Move camera to pickup location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(pickup.getLat(), pickup.getLog()), 13));

        if (dropList != null && !dropList.isEmpty()) {
            dropAdapter = new DropAdapter(dropList);
            binding.recyclerDrop.setAdapter(dropAdapter);

            // Add markers for all drop points
            for (int i = 0; i < dropList.size(); i++) {
                Drop drop = dropList.get(i);
                Bitmap bitmap = drawTextToBitmap(this, R.drawable.ic_destination_long, String.valueOf(i + 1));
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(drop.getLat(), drop.getLog()))
                        .title("Drop " + (i + 1))
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            }

            // Draw route from pickup to first drop point
            LatLng origin = new LatLng(pickup.getLat(), pickup.getLog());
            for (int i = 0; i < dropList.size(); i++) {
                Drop drop = dropList.get(i);
                LatLng destination = new LatLng(drop.getLat(), drop.getLog());

                // Get directions between points
                getDirections(origin, destination, googleMap);

                // Set current destination as next origin
                origin = destination;
            }
        }
    }*/

    private void getDirections(LatLng origin, LatLng destination, GoogleMap googleMap) {
        try {
            DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                    .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                    .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                    .mode(TravelMode.DRIVING);

            request.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
                @Override
                public void onResult(DirectionsResult result) {
                    runOnUiThread(() -> {
                        try {
                            if (result.routes != null && result.routes.length > 0) {
                                DirectionsRoute route = result.routes[0];

                                // Calculate distance and duration
                                totalDistance += route.legs[0].distance.inMeters;
                                totalDuration += route.legs[0].duration.inSeconds;

                                totalDistanceValue = totalDistance / 1000.0; // Convert to km
                                totalDurationValue = totalDuration / 60;     // Convert to minutes

                                System.out.println("totalDistanceValue::"+totalDistanceValue);
                                System.out.println("totalDurationValue::"+totalDurationValue);
                                // Update UI with total distance and time
                                updateDistanceAndTime();

                                List<LatLng> decodedPath = PolyUtil.decode(route.overviewPolyline.getEncodedPath());
                                PolylineOptions polylineOptions = new PolylineOptions()
                                        .addAll(decodedPath)
                                        .width(8)
                                        .color(Color.BLUE)
                                        .geodesic(true);

                                Polyline polyline = googleMap.addPolyline(polylineOptions);
                                polylines.add(polyline);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e("Maps", "Error fetching directions: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void updateDistanceAndTime() {
        // Format distance
        String distanceText;
        double kilometers = totalDistance / 1000.0;
        if (totalDistance >= 1000) {
            distanceText = String.format("%.1f km", kilometers);
        } else {
            distanceText = String.format("%d m", (int) totalDistance);
        }

        // Format duration
        String durationText;
        long hours = totalDuration / 3600;
        long minutes = (totalDuration % 3600) / 60;
        if (hours > 0) {
            durationText = String.format("%dh %dm", hours, minutes);
        } else {
            durationText = String.format("%dm", minutes);
        }

        // This value is saved in @GoodsPickupMapLocationActivity.java
        float outstationThreshold = preferenceManager.getFloatValue("outstation_distance", 30.0f);

        //To determine whether local or outstation booking
        isOutstation = kilometers > outstationThreshold;

        runOnUiThread(() -> {
            // Update distance and time
            exactTime = durationText;
            exactDistance = distanceText;
            binding.txtTotalDistance.setText(distanceText);
            binding.txtEstimatedTime.setText(durationText);

            // Update booking type with icon

            if (isOutstation) {
                binding.txtBookingType.setText("Outstation");
                binding.txtBookingType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_outstation, 0, 0, 0);
                binding.txtBookingType.setTextColor(getResources().getColor(R.color.outstation_color));
            } else {
                binding.txtBookingType.setText("In Town Booking");
                binding.txtBookingType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_local, 0, 0, 0);
                binding.txtBookingType.setTextColor(getResources().getColor(R.color.local_color));
            }
            preferenceManager.saveBooleanValue("isOutstation",isOutstation);
            // Optionally update proceed button text/style based on booking type
            updateProceedButton();
        });
    }

    private void updateProceedButton() {
        if (isOutstation) {
            binding.btnProce.setText("Proceed with Outstation Booking");

        } else {
            binding.btnProce.setText("Proceed with Local Booking");

        }
    }

     class DropAdapter extends RecyclerView.Adapter<DropAdapter.MyViewHolder> {
        private final List<Drop> dropList;

        public DropAdapter(List<Drop> dropList) {
            this.dropList = dropList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDropBinding itemBinding = ItemDropBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Drop item = dropList.get(position);
            holder.binding.txtDropaddress.setText(item.getAddress());
            holder.binding.imgDelete.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
            holder.binding.imgEdit.setVisibility(View.VISIBLE);
            holder.binding.imgEdit.setOnClickListener(v -> {
                // Save the position being edited
                Glb.editingDropPosition = position;

                // Finish the current activity
                Context context = v.getContext();
                if (context instanceof Activity) {
                    ((Activity) context).setResult(Activity.RESULT_OK); // Optional, if you want to pass result
                    ((Activity) context).finish();
                }
            });

            holder.binding.imgDelete.setOnClickListener(v -> {
                if (position != 0 && position < dropList.size()) {
                    // Remove from the global dropList
                    dropList.remove(position);

                    // Notify the adapter
                    notifyDataSetChanged();

                    // Update the map
                    updateMap(gMap);

                    // Update the "Add New Stop" button visibility
                    String dropsValue = preferenceManager.getStringValue("multiple_drops", "3");
                    int multipleDrops;
                    try {
                        multipleDrops = Integer.parseInt(dropsValue);
                    } catch (NumberFormatException e) {
                        multipleDrops = 3; // fallback value
                    }
                    binding.txtAddnewstop.setVisibility(dropList.size() < multipleDrops ? View.VISIBLE : View.GONE);

                    // Update Glb.addStopClicked flag
                    if(dropList.size() <= 1){
                        Glb.addStopClicked = false;
                    }

                    // Show a toast to confirm deletion
                    Toast.makeText(ReviewMapActivity.this, "Drop location removed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dropList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            ItemDropBinding binding;

            MyViewHolder(ItemDropBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private void moveItem(int oldPos, int newPos) {
        Drop temp = dropList.get(oldPos);
        dropList.set(oldPos, dropList.get(newPos));
        dropList.set(newPos, temp);
        dropAdapter.notifyItemChanged(oldPos);
        dropAdapter.notifyItemChanged(newPos);
        updateMap(gMap);
    }

    // Your existing drawTextToBitmap method remains the same
    public Bitmap drawTextToBitmap(Context gContext, int gResId, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, gResId);

        Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (16 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 3;

        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPaused) {
            // Restart tracking when coming back to screen
            startTrackingDrivers();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isPaused = true;
        isTrackingDrivers = false;
        if(markerUpdateHandler !=null)
        {markerUpdateHandler.removeCallbacksAndMessages(null);}

        // Clear all driver markers
        if(driverMarkers != null) {
            for (Marker marker : driverMarkers.values()) {
                marker.remove();
            }
            driverMarkers.clear();
        }

        // Clear polylines
        if (polylines != null) {
            for (Polyline line : polylines) {
                if (line != null) {
                    line.remove();
                }
            }
            polylines.clear();
        }
//        if(dropList != null){
//            dropList.clear();
//        }

        // Shutdown GeoApiContext using ExecutorService
        if (geoApiContext != null) {
            executorService.execute(() -> {
                try {
                    geoApiContext.shutdown();
                } catch (Exception e) {
                    Log.e("ReviewMap", "Error shutting down GeoApiContext: " + e.getMessage());
                }
            });
        }

        // Shutdown ExecutorService
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        // Clear binding
        binding = null;
//        System.out.println("addStopClicked::"+Glb.addStopClicked);
//        if(!Glb.addStopClicked){
//            dropList.clear();
//        }
    }
}