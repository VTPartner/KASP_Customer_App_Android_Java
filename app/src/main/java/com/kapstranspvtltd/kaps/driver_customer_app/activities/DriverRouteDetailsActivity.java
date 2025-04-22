package com.kapstranspvtltd.kaps.driver_customer_app.activities;

import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.common_activities.ServiceDurationActivity;
import com.kapstranspvtltd.kaps.databinding.ItemDropBinding;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.databinding.ActivityDriverRouteDetailsBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DriverRouteDetailsActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityDriverRouteDetailsBinding binding;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverRouteDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

//        binding.txtAddnewstop.setVisibility(isSinglePoint == 1 ? View.VISIBLE : View.GONE);
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

        Glb.totalDistanceValue = totalDistanceValue;
        Glb.totalDuration = totalDuration;
        Glb.exactTime = exactTime;
        Glb.exactDistance = exactDistance;
        Glb.pickup = pickup;
        Glb.drop = drop;

        binding.btnProce.setOnClickListener(v -> startActivity(new Intent(this, ServiceDurationActivity.class)
                .putExtra("cab",cabService)
                .putExtra("total_distance", totalDistanceValue)
                .putExtra("total_time", totalDuration)
                .putExtra("exact_time", exactTime)
                .putExtra("exact_distance", exactDistance)


                .putExtra("pickup", pickup)
                .putExtra("drop", drop)));

        binding.txtAddnewstop.setOnClickListener(v -> finish());
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
    }

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

        // Determine booking type
        isOutstation = kilometers > OUTSTATION_THRESHOLD_KM;

        runOnUiThread(() -> {
            // Update distance and time
          Glb.exactTime =  exactTime = durationText;
            Glb.exactDistance = exactDistance = distanceText;
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
        public DropAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDropBinding itemBinding = ItemDropBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new DropAdapter.MyViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull DropAdapter.MyViewHolder holder, int position) {
            Drop item = dropList.get(position);
            holder.binding.txtDropaddress.setText(item.getAddress());
            holder.binding.imgDelete.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

            holder.binding.imgDelete.setOnClickListener(v -> {
                if (position != 0) {
                    dropList.remove(item);
                    notifyDataSetChanged();
                    updateMap(gMap);
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
    protected void onDestroy() {
        super.onDestroy();

        // Clear polylines
        if (polylines != null) {
            for (Polyline line : polylines) {
                if (line != null) {
                    line.remove();
                }
            }
            polylines.clear();
        }
        if(dropList != null){
            dropList.clear();
        }

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
    }
}