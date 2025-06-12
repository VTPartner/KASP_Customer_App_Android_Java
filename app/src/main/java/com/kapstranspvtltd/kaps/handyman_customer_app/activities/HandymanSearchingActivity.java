package com.kapstranspvtltd.kaps.handyman_customer_app.activities;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.databinding.ActivityDriverAgentSearchingBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityHandymanSearchingBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HandymanSearchingActivity extends BaseActivity implements OnMapReadyCallback {

    ActivityHandymanSearchingBinding binding;

    String selectedReason ;
    CustPrograssbar custPrograssbar;

    GoogleMap mMap;
    String bookingId;

    Pickup pickup;

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int COUNTDOWN_TIME = 100; // 5 minutes in seconds
    private CountDownTimer countDownTimer;
    private PolylineOptions polylineOptions;
    private List<LatLng> polylinePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandymanSearchingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 201);
        }
        bookingId = getIntent().getStringExtra("booking_id");
        custPrograssbar = new CustPrograssbar();

        binding.catgName.setText(Glb.categoryName);

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tripInfo();
        binding.imgBack.setOnClickListener(v -> finish());
        startCountDownTimer();
        binding.btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());
        Glide.with(this)
                .asGif()
                .load(R.drawable.searching_driver)
                .into(binding.searchingDriverIcon);

        if(Glb.pickup != null){
            pickup = Glb.pickup;
        }
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                String timeLeft = String.format(Locale.getDefault(),
                        "Booking will be cancelled in %02d:%02d", minutes, seconds);
                binding.txtTimer.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                // Auto cancel booking after timer expires
                cancelBooking();
            }
        }.start();
    }

    private void tripInfo() {
        binding.txtBookingId.setText("Service CRN " + bookingId);
        if(Glb.pickup != null) {
            binding.txtPickupaddress.setText(Glb.pickup.getAddress());
        }

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Get pickup and drop coordinates
        LatLng pickupLatLng = new LatLng(pickup.getLat(), pickup.getLog());


        // Add markers
        mMap.addMarker(new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));

//        mMap.addMarker(new MarkerOptions()
//                .position(dropLatLng)
//                .title("Drop")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_long)));

        // Draw route
        //drawRoute(pickupLatLng, dropLatLng);

        // Move camera to show both markers
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);
        LatLngBounds bounds = builder.build();

        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }



    private void showCancelConfirmationDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_cancel_booking_sheet, null);

        // Initialize views
        ImageView driverImage = sheetView.findViewById(R.id.driverImage);
        TextView driverName = sheetView.findViewById(R.id.driverName);
        TextView messageText = sheetView.findViewById(R.id.messageText);
        RecyclerView reasonsRecyclerView = sheetView.findViewById(R.id.reasonsRecyclerView);
        Button submitButton = sheetView.findViewById(R.id.submitButton);
        ProgressBar progressBar = sheetView.findViewById(R.id.progressBar);

        // Set driver details
//        Glide.with(this)
//                .load(driverImageUrl)
//                .placeholder(R.drawable.placeholder)
//                .circleCrop()
//                .into(driverImage);
//
//        driverName.setText(driverName);
//        messageText.setText("You are about to cancel the booking which was assigned to " + driverName);

        // Setup RecyclerView
        reasonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<String> cancelReasons = Arrays.asList(
                "Driver delayed pickup",
                "Wrong vehicle assigned",
                "Driver unreachable",
                "Change of plans",
                "Other reasons",
                // ... add all other reasons
                "Customer support was helpful in resolving issues"
        );

//        CancelReasonAdapter adapter = new CancelReasonAdapter(cancelReasons, reason -> {
//            selectedReason = reason;
//            submitButton.setEnabled(true);
//        });
//        reasonsRecyclerView.setAdapter(adapter);

        // Setup submit button
        submitButton.setEnabled(false);
        submitButton.setOnClickListener(v -> {
            if (selectedReason.isEmpty()) {
                Toast.makeText(this, "Please select a cancellation reason.", Toast.LENGTH_SHORT).show();
                return;
            }
            bottomSheetDialog.dismiss();
            cancelBooking();
        });

        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();
    }

    private void cancelBooking() {
        finish();
//        showLoading(true);
//        String url = APIClient.baseUrl + "cancel_booking";
//        String serverToken = AccessToken.getAccessToken();
//        prefe
//
//        Map<String, String> params = new HashMap<>();
//        params.put("booking_id", bookingId);
//        params.put("customer_id", customerId);
//        params.put("driver_id", driverId);
//        params.put("pickup_address", pickup.getAddress());
//        params.put("server_token", serverToken);
//        params.put("cancel_reason", selectedReason);
//
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
//                response -> {
//                    showLoading(false);
//                    // Handle success
//                    finish(); // or navigate to main screen
//                },
//                error -> {
//                    showLoading(false);
//                    handleError(error);
//                });
//
//        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}