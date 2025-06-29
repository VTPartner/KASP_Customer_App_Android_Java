package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
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
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivitySearchingGoodsDriverBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SearchingGoodsDriverActivity extends BaseActivity implements OnMapReadyCallback {

    ActivitySearchingGoodsDriverBinding binding;

    String selectedReason ;
    CustPrograssbar custPrograssbar;

    GoogleMap mMap;
    String bookingId;

    Pickup pickup;

    Drop drop;

    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int COUNTDOWN_TIME = 120; // 2 minutes in seconds
    private CountDownTimer countDownTimer;
    private PolylineOptions polylineOptions;
    private List<LatLng> polylinePoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchingGoodsDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 201);
        }
        bookingId = getIntent().getStringExtra("booking_id");
        custPrograssbar = new CustPrograssbar();


        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tripInfo();

        binding.imgBack.setOnClickListener(v -> onBackPressed());

        startCountDownTimer();

        Glide.with(this)
                .asGif()
                .load(R.drawable.searching_driver)
                .into(binding.searchingDriverIcon);
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cancelBooking();
                    // Don't call super.onBackPressed() here since cancelBooking() calls finish()
                })
                .setNegativeButton("No", null)
                .setCancelable(false)  // Prevent dismissing dialog by clicking outside
                .show();
    }

    @Override
    public void onBackPressed() {
        if (countDownTimer != null) {
            // Show confirmation dialog
            showCancelConfirmationDialog();
        } else {
            super.onBackPressed(); // Call super if timer is null
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
        pickup = getIntent().getParcelableExtra("pickup");
        drop = getIntent().getParcelableExtra("drop");

        binding.txtBookingId.setText("Trip CRN " + bookingId);
        binding.txtPickupaddress.setText(pickup.getAddress());



        // Create horizontal RecyclerView for drop addresses
        RecyclerView dropsRecyclerView = new RecyclerView(this);
        dropsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dropsRecyclerView.setAdapter(new DropsAdapter(dropList));

        // Add RecyclerView to locationMarkertext
        binding.locationMarkertext.addView(dropsRecyclerView);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Set map settings
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Get pickup coordinates
        LatLng pickupLatLng = new LatLng(pickup.getLat(), pickup.getLog());

        // Add pickup marker
        mMap.addMarker(new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long)));

        // Build bounds to include all points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);

        // Add numbered markers for all drops
        LatLng previousPoint = pickupLatLng;
        for (int i = 0; i < dropList.size(); i++) {
            Drop drop = dropList.get(i);
            LatLng dropLatLng = new LatLng(drop.getLat(), drop.getLog());

            // Create numbered marker
            Bitmap numberedMarker = drawTextToBitmap(this,
                    R.drawable.ic_destination_long,
                    String.valueOf(i + 1));

            mMap.addMarker(new MarkerOptions()
                    .position(dropLatLng)
                    .title("Drop " + (i + 1))
                    .icon(BitmapDescriptorFactory.fromBitmap(numberedMarker)));

            builder.include(dropLatLng);

            // Draw route between points
            drawRoute(previousPoint, dropLatLng);
            previousPoint = dropLatLng;
        }

        // Move camera to show all markers
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

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

    private void drawRoute(LatLng origin, LatLng destination) {
        String url = getDirectionsUrl(origin, destination);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse route
                        JSONArray routes = response.getJSONArray("routes");
                        JSONObject route = null;
                        try {
                            route = routes.getJSONObject(0);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                        String encodedPath = overviewPolyline.getString("points");

                        List<LatLng> decodedPath = decodePolyline(encodedPath);

                        // Draw polyline
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(decodedPath)
                                .width(8)
                                .color(getResources().getColor(R.color.colorPrimary))
                                .geodesic(true);

                        mMap.addPolyline(polylineOptions);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Maps", "Direction API Error: " + error.getMessage())
        );

        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&key=" + getString(R.string.google_maps_key);
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }
        return poly;
    }



    private void cancelBooking() {
        String url = APIClient.baseUrl + "customer_not_interested_cancelled_booking";



        JSONObject params = new JSONObject();
        try {
            params.put("booking_id", bookingId);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    params,
                    response -> {

                        Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {

                        String errorMessage = "Failed to cancel booking";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                String errorResponse = new String(error.networkResponse.data);
                                JSONObject errorJson = new JSONObject(errorResponse);
                                errorMessage = errorJson.optString("message", errorMessage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {

            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private class DropsAdapter extends RecyclerView.Adapter<DropsAdapter.ViewHolder> {
        private final List<Drop> drops;

        public DropsAdapter(List<Drop> drops) {
            this.drops = drops;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setPadding(8, 2, 8, 2);
            textView.setMaxWidth(300);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(getResources().getColor(R.color.black1));
            textView.setTextSize(12);
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Drop drop = drops.get(position);
            holder.textView.setText(drop.getAddress());

            // Add arrow after each drop except the last one
            if (position < drops.size() - 1) {
                holder.textView.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.drawable.ic_right, 0);
                holder.textView.setCompoundDrawablePadding(8);
            }
        }

        @Override
        public int getItemCount() {
            return drops.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
            }
        }
    }

}