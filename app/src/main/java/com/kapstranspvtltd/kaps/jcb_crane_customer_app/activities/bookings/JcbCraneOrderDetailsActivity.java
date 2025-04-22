package com.kapstranspvtltd.kaps.jcb_crane_customer_app.activities.bookings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.pickup_activities.WebViewActivity;
import com.kapstranspvtltd.kaps.common_activities.models.ServiceOrderDetails;
import com.kapstranspvtltd.kaps.databinding.ActivityHandymanOrderDetailsBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityJcbCraneOrderDetailsBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JcbCraneOrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityJcbCraneOrderDetailsBinding binding;
    private GoogleMap mMap;
    private ServiceOrderDetails orderDetails;
    private Set<Polyline> polylines = new HashSet<>();
    private Set<Marker> markers = new HashSet<>();
    private Set<Circle> circles = new HashSet<>();

    CustPrograssbar custPrograssbar;

    PreferenceManager preferenceManager;

    boolean cabService;

//    private PDFGenerator pdfGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJcbCraneOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        custPrograssbar = new CustPrograssbar();

        preferenceManager = new PreferenceManager(this);

        // Setup toolbar
        binding.imaBack.setOnClickListener(v -> finish());

        binding.btnRate.setOnClickListener(v -> showRatingDialog());

        // Get order ID from intent
        String orderId = getIntent().getStringExtra("order_id");
        cabService = getIntent().getBooleanExtra("cab", false);
        if (orderId == null) {
            Toast.makeText(this, "Invalid order details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchOrderDetails(orderId);
        binding.btnGenerateInvoice.setOnClickListener(v -> captureAndShareScreenshot());
    }

    private void showRatingDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_rate_driver, null);
        dialog.setContentView(sheetView);

        // Initialize views
        ImageView driverImage = sheetView.findViewById(R.id.driverImage);
        TextView driverName = sheetView.findViewById(R.id.driverName);
        TextView orderNumber = sheetView.findViewById(R.id.orderNumber);
        RatingBar ratingBar = sheetView.findViewById(R.id.ratingBar);
        TextView ratingDescription = sheetView.findViewById(R.id.ratingDescription);
//        EditText commentBox = sheetView.findViewById(R.id.commentBox);
        MaterialButton submitButton = sheetView.findViewById(R.id.submitButton);

        // Set driver details
        Glide.with(this)
                .load(orderDetails.getDriverImage())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(driverImage);

        driverName.setText(orderDetails.getDriverName());
        orderNumber.setText("Order #" + orderDetails.getOrderId());

        // Set default rating
        ratingBar.setRating(3);
        updateRatingDescription(ratingDescription, 3);

        // Rating change listener
        ratingBar.setOnRatingBarChangeListener((rBar, rating, fromUser) -> {
            updateRatingDescription(ratingDescription, rating);
        });

        // Submit button click
        submitButton.setOnClickListener(v -> {
            submitRating(
                    dialog,
                    (int) ratingBar.getRating(),
                    ratingDescription.getText().toString().trim()
            );
        });

        dialog.show();
    }

    private void updateRatingDescription(TextView descriptionView, float rating) {
        String description;
        int color;

        if (rating <= 1) {
            description = "Worst Experience";
            color = getResources().getColor(R.color.colorerror);
        } else if (rating <= 2) {
            description = "Bad";
            color = getResources().getColor(R.color.orange);
        } else if (rating <= 3) {
            description = "Good";
            color = getResources().getColor(R.color.colorPrimaryDark);
        } else if (rating <= 4) {
            description = "Very Good";
            color = getResources().getColor(R.color.green);
        } else {
            description = "Excellent";
            color = getResources().getColor(R.color.green);
        }

        descriptionView.setText(description);
        descriptionView.setTextColor(color);
    }

    private void submitRating(BottomSheetDialog dialog, int rating, String comment) {
        if (orderDetails == null) return;

        custPrograssbar.prograssCreate(this);

        JSONObject params = new JSONObject();
        try {
            params.put("order_id", orderDetails.getOrderId());
            params.put("ratings", rating);
            params.put("ratings_description", comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "save_jcb_crane_order_ratings",
                params,
                response -> {
                    custPrograssbar.closePrograssBar();
                    dialog.dismiss();
                    Toast.makeText(this, "Rating submitted successfully", Toast.LENGTH_SHORT).show();
                    binding.layoutRating.setVisibility(View.GONE);
                },
                error -> {
                    custPrograssbar.closePrograssBar();
                    Toast.makeText(this, "Failed to submit rating", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void openWebInDesktopMode(String orderId) {
        try {
            String baseUrl = "https://vtpartner.org/dashboard/order-details/";
            String fullUrl = baseUrl + orderDetails.getBookingId() + "/" + orderId;

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("url", fullUrl);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("OpenWeb", "Error opening web page: " + e.getMessage());
            Toast.makeText(this, "Error opening web page", Toast.LENGTH_SHORT).show();
        }
    }
//    private void openWebWithCustomTab(String orderId) {
//        try {
//
//            String baseUrl = "https://vtpartner.org/dashboard/order-details/";
//            String fullUrl = baseUrl + orderDetails.getBookingId() + "/" + orderId;
//
//            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
//            // Customize the toolbar color
//            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
//            // Add share action
//            builder.setShareState(CustomTabsIntent.SHARE_STATE_ON);
//            // Show title
//            builder.setShowTitle(true);
//
//            CustomTabsIntent customTabsIntent = builder.build();
//            customTabsIntent.launchUrl(this, Uri.parse(fullUrl));
//        } catch (Exception e) {
//            Log.e("OpenWeb", "Error opening web page: " + e.getMessage());
//            Toast.makeText(this, "Error opening web page", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void captureAndShareScreenshot() {
        try {
            // Get the root view of the activity
            View rootView = getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);

            // Create bitmap of the view
            Bitmap screenshot = Bitmap.createBitmap(
                    rootView.getDrawingCache(),
                    0,
                    0,
                    rootView.getWidth(),
                    rootView.getHeight()
            );
            rootView.setDrawingCacheEnabled(false);

            // Save bitmap to file
            File imageFile = saveScreenshot(screenshot);
            if (imageFile != null) {
                shareScreenshot(imageFile);
            }
        } catch (Exception e) {
            Log.e("Screenshot", "Error capturing screenshot: " + e.getMessage());
            Toast.makeText(this, "Failed to capture screenshot", Toast.LENGTH_SHORT).show();
        }
    }

    private File saveScreenshot(Bitmap bitmap) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(getExternalFilesDir(null), "screenshots");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Create file
            String fileName = "screenshot_" + orderDetails.getOrderId() + ".jpg";
            File imageFile = new File(directory, fileName);

            // Save bitmap to file
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            return imageFile;
        } catch (IOException e) {
            Log.e("Screenshot", "Error saving screenshot: " + e.getMessage());
            return null;
        }
    }

    private void shareScreenshot(File imageFile) {
        try {
            // Get URI for the file using FileProvider
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    imageFile
            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Add order details in text
            String shareText = String.format("Order Details\nOrder ID: %s\nCustomer: %s\nPickup: %s\nDrop: %s",
                    orderDetails.getOrderId(),
                    orderDetails.getCustomerName(),
                    orderDetails.getPickupAddress(),
                    orderDetails.getDropAddress());
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share Order Details"));
        } catch (Exception e) {
            Log.e("Screenshot", "Error sharing screenshot: " + e.getMessage());
            Toast.makeText(this, "Failed to share screenshot", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchOrderDetails(String orderId) {
        showLoading(true);

        JSONObject params = new JSONObject();
        try {
            params.put("order_id", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "jcb_crane_order_details",
                params,
                response -> {
                    try {
                        JSONObject result = response.getJSONArray("results").getJSONObject(0);
                        orderDetails = ServiceOrderDetails.fromJson(result);
//                        pdfGenerator = new PDFGenerator(this, orderDetails);
//                        pdfGenerator.generateAndSharePDF(binding.btnGenerateInvoice);
                        updateUI();
                        String ratings = orderDetails.getRatings();
                        if(ratings.equalsIgnoreCase("0.0")){
                            binding.layoutRating.setVisibility(View.VISIBLE);
                        }else{
                            binding.layoutRating.setVisibility(View.GONE);
                        }
                        System.out.println("ratings::"+ratings);
                        if (mMap != null) {
                            updateMapRoute();
                        }
                    } catch (JSONException e) {
                        showError("Failed to parse response");
                    }
                    showLoading(false);
                },
                error -> {
                    showError("Network request failed");
                    showLoading(false);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void updateUI() {
        if (orderDetails == null) return;

        binding.toolbarTitle.setText("Order #" + orderDetails.getOrderId());
        binding.txtDate.setText(orderDetails.getFormattedBookingTiming());
        binding.txtPackid.setText("# CRN " + orderDetails.getOrderId());
        binding.txtttotle.setText(orderDetails.getFormattedPriceTotal());

        // Driver details
        if (!TextUtils.isEmpty(orderDetails.getDriverImage())) {
            Glide.with(this)
                    .load(orderDetails.getDriverImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_image_placeholder)
                    .into(binding.imgIcon);
        }

        binding.txtRidername.setText(orderDetails.getDriverName());
        binding.txtVtype.setText(String.format("%s - %s",
                orderDetails.getSubCatName(),
                orderDetails.getServiceName()));
//        binding.txtStatus.setText(orderDetails.getBookingStatus());
        binding.txtStatus.setText("Service Completed");
        binding.txtStatus.setTextColor(getStatusColor(orderDetails.getBookingStatus()));

        // Addresses
        binding.txtPickaddress.setText(orderDetails.getPickupAddress());

        // Update drop addresses (if multiple)
//        binding.lvlDrop.removeAllViews();
//        TextView dropAddressView = new TextView(this);
//        dropAddressView.setText(orderDetails.getDropAddress());
//        dropAddressView.setTextColor(getResources().getColor(R.color.black));
//        dropAddressView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
//        binding.lvlDrop.addView(dropAddressView);

        // Payment details
        binding.txtSubfare.setText(orderDetails.getFormattedSubTotal());
        binding.txtTotal.setText(orderDetails.getFormattedPriceTotal());

        // Coupon discount
        if (orderDetails.getCouponDiscountAmount() > 0) {
            binding.lvlDiscount.setVisibility(View.VISIBLE);
            binding.txtCoupondis.setText("₹" + orderDetails.getCouponDiscountAmount());
        }
//
//        // Wallet amount
//        if (orderDetails.getWalletAmount() > 0) {
//            binding.lvlWallet.setVisibility(View.VISIBLE);
//            binding.txtWallet.setText("₹" + orderDetails.getWalletAmount());
//        }

        // Category and payment method
//        Glide.with(this)
//                .load(orderDetails.getCategoryImage())
//                .into(binding.imgCategory);
//        binding.txtCtitle.setText(orderDetails.getCategoryName());

//        Glide.with(this)
//                .load(orderDetails.getPaymentMethodImage())
//                .into(binding.imgPayment);
        binding.txtPaymenttitle.setText(orderDetails.getPaymentMethod());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (orderDetails != null) {
            updateMapRoute();
        }
    }

    private void updateMapRoute() {
        clearMap();

        LatLng pickupLatLng = new LatLng(
                orderDetails.getPickupLat(),
                orderDetails.getPickupLng());


        // Add markers
        markers.add(mMap.addMarker(new MarkerOptions()
                .position(pickupLatLng)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_long))));



        // Add circles
//        circles.add(mMap.addCircle(new CircleOptions()
//                .center(pickupLatLng)
//                .radius(12)
//                .strokeWidth(3)
//                .strokeColor(Color.WHITE)
//                .fillColor(Color.GREEN)));
//
//        circles.add(mMap.addCircle(new CircleOptions()
//                .center(dropLatLng)
//                .radius(12)
//                .strokeWidth(3)
//                .strokeColor(Color.WHITE)
//                .fillColor(Color.RED)));

        // Move camera to show both points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pickupLatLng);

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                builder.build(), 100));
    }



    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&key=" + getString(R.string.google_maps_key);
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private void clearMap() {
        for (Marker marker : markers) marker.remove();
        for (Polyline polyline : polylines) polyline.remove();
        for (Circle circle : circles) circle.remove();
        markers.clear();
        polylines.clear();
        circles.clear();
    }

    private void showLoading(boolean show) {
        if (show)
            custPrograssbar.prograssCreate(this);
        else custPrograssbar.closePrograssBar();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "Driver Accepted":
                return getResources().getColor(R.color.blue);
            case "Agent Arrived":
                return getResources().getColor(R.color.orange);
            case "OTP Verified":
                return getResources().getColor(R.color.indigo);
            case "Start Trip":
                return getResources().getColor(R.color.green);
            default:
                return getResources().getColor(R.color.blue);
        }
    }
}