package com.kapstranspvtltd.kaps.handyman_customer_app.activities.bookings;

import static android.content.ContentValues.TAG;

import static com.kapstranspvtltd.kaps.retrofit.APIClient.resizeBitmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.models.CancelReason;
import com.kapstranspvtltd.kaps.activities.goods_service_booking_activities.EditDropLocationActivity;
import com.kapstranspvtltd.kaps.adapters.CancelReasonAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.ServiceBookingDetails;
import com.kapstranspvtltd.kaps.databinding.ActivityHandymanOngoingBookingDetailsBinding;
import com.kapstranspvtltd.kaps.databinding.DialogPaymentDetailsBinding;
import com.kapstranspvtltd.kaps.fcm.AccessToken;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class HandymanOngoingBookingDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityHandymanOngoingBookingDetailsBinding binding;
    private GoogleMap mMap;
    private Marker driverMarker;
    private List<LatLng> polylinePoints = new ArrayList<>();
    private Handler locationUpdateHandler = new Handler();
    private static final int LOCATION_UPDATE_INTERVAL = 1000; // 10 seconds

    private String bookingId;
    private String customerId;


    private boolean isLoading = true;
    private ServiceBookingDetails bookingDetails;
    private Timer locationUpdateTimer;

    CustPrograssbar custPrograssbar;

    PreferenceManager preferenceManager;

    boolean isFromFCM;
    boolean cabService;

    private BitmapDescriptor driverIcon; // Global variable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHandymanOngoingBookingDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.cab_new);
        Bitmap smallMarker = resizeBitmap(original, 100, 100); // Resize to 100x100
        driverIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);

        initGeoApiContext();

        custPrograssbar = new CustPrograssbar();

        preferenceManager = new PreferenceManager(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Get booking ID from intent
        bookingId = getIntent().getStringExtra("booking_id");
        customerId = preferenceManager.getStringValue("customer_id");
        isFromFCM = getIntent().getBooleanExtra("isFromFCM", false);



        setupViews();
        fetchBookingDetails();
        binding.imgCall.setOnClickListener(v -> handleCallClick());
        binding.btnCancel.setOnClickListener(v -> showCancelBookingBottomSheet() );
        binding.imgCallSos.setOnClickListener(v -> callEmergencyNumber());
        binding.imgShare.setOnClickListener(v -> {

            String url = "https://kaps9.in/handyman-booking-details/" + bookingId;

            String message = "🚚 Your KAPS booking is confirmed!\n\n"
                    + "📦 View your booking details here:\n"
                    + url + "\n\n"
                    + "Thank you for choosing KAPS – Reliable. Fast. Affordable.";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "KAPS Booking Confirmation");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message);

            v.getContext().startActivity(Intent.createChooser(shareIntent, "Share your KAPS booking via"));
        });

    }

    private void callEmergencyNumber() {
        String emergencyNumber = "112";
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + emergencyNumber));
        startActivity(callIntent);
    }


    private void showCancelBookingBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_cancel_booking, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Initialize views
        CircleImageView driverImage = bottomSheetView.findViewById(R.id.driverImage);
        TextView driverName = bottomSheetView.findViewById(R.id.driverName);
        TextView cancelText = bottomSheetView.findViewById(R.id.cancelText);
        RecyclerView reasonsRecyclerView = bottomSheetView.findViewById(R.id.reasonsRecyclerView);
        TextInputLayout otherReasonLayout = bottomSheetView.findViewById(R.id.otherReasonLayout);
        TextInputEditText otherReasonInput = bottomSheetView.findViewById(R.id.otherReasonInput);
        Button submitButton = bottomSheetView.findViewById(R.id.submitButton);

        // Set driver details
        Glide.with(this)
                .load(bookingDetails.getDriverImage())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .override(100, 100)
                .into(driverImage);

        driverName.setText(bookingDetails.getDriverName());
        cancelText.setText("You are about to cancel the booking which was assigned to " + bookingDetails.getDriverName());

        // Fetch cancel reasons from API
        fetchCancelReasons(reasonsRecyclerView, otherReasonLayout, otherReasonInput, submitButton, bottomSheetDialog);

        bottomSheetDialog.show();
    }

    private void fetchCancelReasons(RecyclerView recyclerView, TextInputLayout otherReasonLayout,
                                    TextInputEditText otherReasonInput, Button submitButton,
                                    BottomSheetDialog dialog) {

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");
        JSONObject params = new JSONObject();
        try {
            params.put("category_id", 5);
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }


        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                APIClient.baseUrl + "get_category_cancel_reasons",
                params,
                response -> {
                    try {
                        JSONArray reasonsArray = response.getJSONArray("reasons");
                        List<CancelReason> cancelReasons = new ArrayList<>();

                        for (int i = 0; i < reasonsArray.length(); i++) {
                            JSONObject reasonObj = reasonsArray.getJSONObject(i);
                            cancelReasons.add(new CancelReason(
                                    reasonObj.getInt("reason_id"),
                                    reasonObj.getString("reason")
                            ));
                        }

                        // Add "Other reasons" option
                        cancelReasons.add(new CancelReason(-1, "Other reasons"));

                        setupCancelReasonAdapter(cancelReasons, recyclerView, otherReasonLayout,
                                otherReasonInput, submitButton, dialog);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showError("Error loading cancel reasons");
                    }
                },
                error -> {
                    error.printStackTrace();
                    showError("Failed to load cancel reasons");
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void setupCancelReasonAdapter(List<CancelReason> reasons, RecyclerView recyclerView,
                                          TextInputLayout otherReasonLayout, TextInputEditText otherReasonInput,
                                          Button submitButton, BottomSheetDialog dialog) {

        final CancelReason[] selectedReason = {null};

        CancelReasonAdapter adapter = new CancelReasonAdapter(reasons, reason -> {
            selectedReason[0] = reason;
            submitButton.setEnabled(true);

            // Show/hide other reason input
            if (reason.getReason().equals("Other reasons")) {
                otherReasonLayout.setVisibility(View.VISIBLE);
            } else {
                otherReasonLayout.setVisibility(View.GONE);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Handle submit button
        submitButton.setOnClickListener(v -> {
            if (selectedReason[0] == null) {
                showError("Please select a reason");
                return;
            }

            String finalReason;
            if (selectedReason[0].getReason().equals("Other reasons")) {
                String otherReason = otherReasonInput.getText().toString();
                if (otherReason.isEmpty()) {
                    otherReasonInput.setError("Please enter a reason");
                    return;
                }
                finalReason = otherReason;
            } else {
                finalReason = selectedReason[0].getReason();
            }

            // Show loading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Cancelling booking...");
            progressDialog.show();

            cancelBooking(finalReason, new CancelBookingCallback() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    progressDialog.dismiss();
                    showError(error);
                }
            });
        });
    }

    private void cancelBooking(String reason, CancelBookingCallback callback) {
        String url =  APIClient.baseUrl +"cancel_handyman_agent_booking";
        String agentAccessToken = AccessToken.getAccessToken();
        String customerAccessToken = AccessToken.getCustomerAccessToken();

        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("booking_id", bookingId);
            params.put("customer_id", customerId);
            params.put("agent_server_token", agentAccessToken);
            params.put("customer_server_token", customerAccessToken);
            params.put("driver_id", bookingDetails.getDriverId());
            params.put("pickup_address", bookingDetails.getPickupAddress());
            params.put("cancel_reason", reason);
            params.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    params,
                    response -> {
                        showError("Booking Cancelled Successfully");
                        onBackPressed();
                    },
                    error -> callback.onError(error.getMessage())
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } catch (JSONException e) {
            callback.onError(e.getMessage());
        }
    }

    interface CancelBookingCallback {
        void onSuccess();
        void onError(String error);
    }

    private void createDriverNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "driver_home_channel";
            CharSequence channelName = "Driver Home Notifications";
            String channelDescription = "Notifications for driver home updates";

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(channelDescription);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String message) {
        String channelId = "customer_home_channel";

        // Create an explicit intent for the HomeActivity
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo) // Make sure to have this icon in your drawable
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        // Add sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationBuilder.setSound(defaultSoundUri);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            // Use a unique notification ID
            int notificationId = (int) System.currentTimeMillis();
            notificationManager.notify(notificationId, notificationBuilder.build());
        }
    }
    private void handleCallClick() {
        try {
            if (bookingDetails == null) return;
            String driverMobileNo = bookingDetails.getDriverMobileNo();
            if (driverMobileNo == null || driverMobileNo.isEmpty()) {
                showError("Driver Mobile number not available");
                return;
            }

            // Create the intent to make a call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + driverMobileNo));

            // Check if there's an app that can handle this intent
            if (callIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(callIntent);
            } else {
                showError("No app available to make calls");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Unable to make call");
        }
    }

    private void showPaymentDialog(String amount) {

        // Inflate dialog layout
        DialogPaymentDetailsBinding dialogBinding = DialogPaymentDetailsBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogBinding.getRoot())
                .create();
//
//        // Set amount
//        dialogBinding.amountValue.setText("₹" + Math.round(Double.parseDouble(amount)));
//        // Setup click listeners
//        dialogBinding.cancelButton.setOnClickListener(v -> dialog.dismiss());
//
//
//        dialog.show();

        int baseAmount = (int) Math.round(Double.parseDouble(amount));
        double penaltyAmount = bookingDetails.getPenaltyAmount();
        int totalPayable = (int) (baseAmount + penaltyAmount);

        // Set amount
//        dialogBinding.amountValue.setText("₹" + Math.round(Double.parseDouble(amount)));

        dialogBinding.amountValue.setText("₹" + baseAmount);
        if (penaltyAmount > 0) {
            dialogBinding.penaltyLabel.setVisibility(View.VISIBLE);
            dialogBinding.penaltyValue.setVisibility(View.VISIBLE);
            dialogBinding.totalPayableLabel.setVisibility(View.VISIBLE);
            dialogBinding.totalPayableValue.setVisibility(View.VISIBLE);
            dialogBinding.penaltyValue.setText("₹" + penaltyAmount);
        } else {
            dialogBinding.penaltyLabel.setVisibility(View.GONE);
            dialogBinding.penaltyValue.setVisibility(View.GONE);
            dialogBinding.totalPayableLabel.setVisibility(View.GONE);
            dialogBinding.totalPayableValue.setVisibility(View.GONE);
        }
        dialogBinding.totalPayableValue.setText("₹" + totalPayable);
        // Setup click listeners
        dialogBinding.cancelButton.setOnClickListener(v -> dialog.dismiss());


        dialog.show();
    }


    private void setupViews() {
        binding.imgBack.setOnClickListener(v -> onBackPressed());

        // Setup other click listeners and initial view states
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFromFCM) {

            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else
            finish();
    }

    private void fetchBookingDetails() {
        showLoading(true);


        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("booking_id", bookingId);
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                APIClient.baseUrl + "handyman_agent_booking_details_live_track",
                params,
                response -> {
                    try {
                        JSONObject result = response.getJSONArray("results").getJSONObject(0);
                        bookingDetails = parseBookingDetails(result);

                        updateUI();
                        if(bookingDetails.getBookingStatus().equalsIgnoreCase("Cancelled") == false) {

                            startLocationUpdates();
                        }
                    } catch (JSONException e) {
                        showError("Failed to parse response");

                    }
                    showLoading(false);
                },
                error -> {
                    handleError(error);

                    showLoading(false);
                });

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void handleError(VolleyError error) {
        String message;
        // Check if there's a network response
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;

            switch (statusCode) {

                case 404:
                    message = "No Booking details Found";
                    finish();
                    break;
                case 400:
                    message = "Bad request";
                    break;
                case 500:
                    message = "Server error";
                    break;
                default:
                    message = "Error fetching plan details";
                    break;
            }
        } else {
            // Handle cases where there's no network response
            if (error instanceof NetworkError) {
                message = "No internet connection";
            } else if (error instanceof TimeoutError) {
                message = "Request timed out";
            } else if (error instanceof ServerError) {
                message = "Server error";
            } else {
                message = "Error fetching plan details";
            }
        }
        showError(message);

    }

    private ServiceBookingDetails parseBookingDetails(JSONObject result) throws JSONException {
        ServiceBookingDetails details = new ServiceBookingDetails();

        details.setCustomerName(result.optString("customer_name"));
        details.setCustomerId(result.optString("customer_id"));
        details.setPickupAddress(result.optString("pickup_address"));
        details.setDropAddress(result.optString("drop_address"));
        details.setDriverName(result.optString("driver_first_name"));
        details.setDriverMobileNo(result.optString("driver_mobile_no"));
        details.setBookingTiming(result.optString("booking_timing"));
        details.setPaymentMethod(result.optString("payment_method"));
        details.setBookingStatus(result.optString("booking_status"));
        details.setBasePrice(result.optDouble("base_price"));
        details.setDriverImage(result.optString("profile_pic"));
        details.setTotalPrice(result.optString("total_price"));
        details.setOtp(result.optString("otp"));
        details.setDriverId(result.optString("driver_id"));

        details.setRatings(result.optString("ratings"));
        details.setDistance(result.optString("distance"));

        details.setSubCatName(result.optString("sub_cat_name"));
        details.setServiceName(result.optString("service_name"));

        details.setPenaltyAmount(result.optDouble("penalty_amount"));

        //parse
        details.setCouponApplied(result.optString("coupon_applied"));
        details.setCouponID(result.optInt("coupon_id"));
        details.setCouponDiscountAmount(result.optDouble("coupon_amount"));
        details.setSetBeforeCouponAmount(result.optDouble("before_coupon_amount"));

        // Parse coordinates
        details.setPickupLat(Double.parseDouble(result.optString("pickup_lat", "0")));
        details.setPickupLng(Double.parseDouble(result.optString("pickup_lng", "0")));
        details.setDropLat(Double.parseDouble(result.optString("destination_lat", "0")));
        details.setDropLng(Double.parseDouble(result.optString("destination_lng", "0")));

        return details;
    }

    private void updateUI() {
        if (bookingDetails == null) return;

        // Update toolbar
        binding.toolbarTitle.setText("Booking #" + bookingId);
        if(bookingDetails.getDistance()!=null)
            binding.totaldistance.setText("Distance: "+bookingDetails.getDistance()+"km");

        String bookingStatus1 = bookingDetails.getBookingStatus();
        boolean isCancelled = bookingStatus1.equalsIgnoreCase("Cancelled");

        if (!isCancelled) {
            binding.btnEditDropLocation.setVisibility(View.VISIBLE);
        } else {
            binding.btnEditDropLocation.setVisibility(View.GONE);
        }

        // Update date and booking ID
        binding.txtDate.setText(bookingDetails.getFormattedBookingTiming());
        binding.txtPackid.setText("#CRN " + bookingId);
        String couponApplied = bookingDetails.getCouponApplied();
        if(couponApplied.equalsIgnoreCase("NA") || couponApplied.equalsIgnoreCase("No")){
            binding.couponAppliedLyt.setVisibility(View.GONE);
        }else{
            binding.couponAppliedLyt.setVisibility(View.VISIBLE);
            binding.txtSubTotal.setText("₹" + bookingDetails.getSetBeforeCouponAmount());
            binding.txtCoupon.setText("₹" + bookingDetails.getCouponDiscountAmount());
        }
        // Update total amount
        binding.txtttotle.setText("₹" + bookingDetails.getTotalPrice());

        String bookingStatus = bookingDetails.getBookingStatus();
        if(bookingStatus.equalsIgnoreCase("Driver Accepted")){
            binding.btnCancel.setVisibility(View.VISIBLE);
        }else{
            binding.btnCancel.setVisibility(View.GONE);
        }
        if(bookingStatus.equalsIgnoreCase("Agent Arrived") || bookingStatus.equalsIgnoreCase("Driver Accepted")){
            binding.otpLyt.setVisibility(View.VISIBLE);
        }
        if(bookingStatus.equalsIgnoreCase("Cancelled")){
            binding.imgCall.setVisibility(View.GONE);
            binding.otpLyt.setVisibility(View.GONE);
        }
        if (bookingStatus.equalsIgnoreCase("Make Payment")) {
            showPaymentDialog(bookingDetails.getTotalPrice());
        }
        // Update rider details
        if (!TextUtils.isEmpty(bookingDetails.getDriverImage())) {
            if (this != null)
                Glide.with(this)
                        .load(bookingDetails.getDriverImage())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .override(100, 100)
                        .into(binding.imgIcon);
        }

        binding.txtOtp.setText(bookingDetails.getOtp());
        binding.txtRidername.setText(bookingDetails.getDriverName());
        binding.txtVtype.setText(bookingDetails.getSubCatName() + " / " + bookingDetails.getServiceName());

        // Update status
        String status = bookingDetails.getCurrentStatus();
        binding.txtStatus.setText(status);
        binding.txtStatus.setTextColor(getStatusColor(bookingDetails.getBookingStatus()));

        // Update addresses
        binding.txtPickaddress.setText(bookingDetails.getPickupAddress());

//        // Update drop addresses (if multiple)
//        binding.lvlDrop.removeAllViews();
//        TextView dropAddressView = new TextView(this);
//        dropAddressView.setText(bookingDetails.getDropAddress());
//        dropAddressView.setTextColor(getResources().getColor(R.color.black));
//        dropAddressView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
//        binding.lvlDrop.addView(dropAddressView);

        // Update payment details
        binding.txtPorterfare.setText("₹" + bookingDetails.getTotalPrice());
        binding.txtTotal.setText("₹" + bookingDetails.getTotalPrice());

        // Update payment method
        if (!TextUtils.isEmpty(bookingDetails.getPaymentMethod()) &&
                !bookingDetails.getPaymentMethod().equals("NA")) {
            binding.lvlDiscount.setVisibility(View.GONE);
            binding.lvlWallet.setVisibility(View.GONE);
        }

        // Show/hide cancel button based on status
        boolean showCancelButton = bookingDetails.getBookingStatus().equals("Driver Accepted") ||
                bookingDetails.getBookingStatus().equals("Agent Arrived");
        binding.lvlRider.setVisibility(showCancelButton ? View.VISIBLE : View.GONE);

        // Initialize map if ready
//        if (mMap != null && bookingDetails.getBookingStatus().equalsIgnoreCase("Cancelled") == false) {
//            drawInitialRoute();
//        }
    }


    private int getStatusColor(String status) {
        switch (status) {
            case "Driver Accepted":
                return getResources().getColor(R.color.blue);
            case "Agent Arrived":
                return getResources().getColor(R.color.orange);
            case "OTP Verified":
                return getResources().getColor(R.color.indigo);
            case "Start Service":
                return getResources().getColor(R.color.green);
            default:
                return getResources().getColor(R.color.colorerror);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        // Cancel any existing timer
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
        }

        // Create new timer for periodic updates
        locationUpdateTimer = new Timer();
        locationUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchDriverLocation();
            }
        }, 0, LOCATION_UPDATE_INTERVAL); // Update every 10 seconds
    }

    private void fetchDriverLocation() {
        if (bookingDetails == null || bookingDetails.getDriverId() == null) {
            return;
        }

        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("driver_id", bookingDetails.getDriverId());
            params.put("customer_id", customerId);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String url = APIClient.baseUrl + "handyman_agent_current_location";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url,
                params,
                response -> {
                    try {
                        JSONObject result = response.getJSONArray("results").getJSONObject(0);
                        double lat = result.getDouble("current_lat");
                        double lng = result.getDouble("current_lng");
                        updateDriverLocation(new LatLng(lat, lng));
                        String bookingStatus = bookingDetails.getBookingStatus();
                        System.out.println("calculate_bookingStatus::"+bookingStatus);
                        if (bookingStatus.equalsIgnoreCase("Driver Accepted") || bookingStatus.equalsIgnoreCase("Agent Arrived")) {
                            calculateDriverDistance(lat, lng, binding.arrivalDistanceTxt, bookingDetails.getBookingStatus(), binding.distanceLyt);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error silently to avoid disrupting UI
                    Log.e("Location Update", "Error fetching driver location", error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void calculateDriverDistance(double driverLat, double driverLng, TextView distanceView, String bookingStatus, LinearLayout distanceLyt) {
        // Check if we should show the distance based on booking status
//        if (!"Accepted by the driver".equals(bookingStatus) && !"Driver Accepted".equals(bookingStatus)) {
//            distanceView.setVisibility(View.GONE);
//            return;
//        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient fusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    String url = String.format(Locale.US,
                            "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%f,%f&destinations=%f,%f&mode=driving&key=%s",
                            location.getLatitude(),
                            location.getLongitude(),
                            driverLat,
                            driverLng,
                            getString(R.string.google_maps_key)
                    );

                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.GET,
                            url,
                            null,
                            response -> {
                                try {
                                    JSONArray rows = response.getJSONArray("rows");
                                    JSONObject elements = rows.getJSONObject(0)
                                            .getJSONArray("elements")
                                            .getJSONObject(0);

                                    if ("OK".equals(elements.getString("status"))) {
                                        String distance = elements.getJSONObject("distance")
                                                .getString("text");
                                        String duration = elements.getJSONObject("duration")
                                                .getString("text");

                                        // Update UI with driver's distance and ETA
                                        distanceLyt.setVisibility(View.VISIBLE);
                                        String driverDistanceText = String.format("Driver is %s away (%s)",
                                                distance, duration);
                                        distanceView.setText(driverDistanceText);
                                    } else {
                                        distanceLyt.setVisibility(View.GONE);
                                    }
                                } catch (JSONException e) {
                                    Log.e(TAG, "Error parsing distance matrix response", e);
                                    distanceLyt.setVisibility(View.GONE);
                                }
                            },
                            error -> {
                                Log.e(TAG, "Error fetching distance", error);
                                distanceView.setVisibility(View.GONE);
                            }
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

                    VolleySingleton.getInstance(this).addToRequestQueue(request);
                } else {
                    distanceView.setVisibility(View.GONE);
                }
            });
        } else {
            distanceView.setVisibility(View.GONE);
        }
    }

//    private void updateDriverLocation(LatLng location) {
//        runOnUiThread(() -> {
//            if (driverMarker == null) {
//                MarkerOptions markerOptions = new MarkerOptions()
//                        .position(location)
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home_current));
//                driverMarker = mMap.addMarker(markerOptions);
//            } else {
//                driverMarker.setPosition(location);
//            }
//
//            updateRouteToDestination(location);
//        });
//    }
private LatLng previousLocation = null; // Add this as a global variable
    private float previousRotation = 0f; // Add this


    private void updateDriverLocation(LatLng newLocation) {
        runOnUiThread(() -> {
            if (driverMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(newLocation)
                        .flat(true)
                        .anchor(0.5f, 0.5f)
                        .icon(driverIcon);
                driverMarker = mMap.addMarker(markerOptions);
            } else {
                driverMarker.setPosition(newLocation);

                if (previousLocation != null) {
                    float distance = getDistance(previousLocation, newLocation);

                    if (distance > 0.5) { // Only update rotation if moved more than 2 meters
                        float bearing = getBearing(previousLocation, newLocation);
                        previousRotation = bearing; // Save last good rotation
                    }
                    driverMarker.setRotation(previousRotation); // Always set the rotation
                }
            }

            previousLocation = newLocation; // Update for next calculation

            updateRouteToDestination(newLocation);
        });
    }

    private float getDistance(LatLng from, LatLng to) {
        float[] result = new float[1];
        Location.distanceBetween(
                from.latitude, from.longitude,
                to.latitude, to.longitude,
                result
        );
        return result[0]; // in meters
    }


    // Helper function to calculate bearing between two LatLng points
    private float getBearing(LatLng from, LatLng to) {
        double lat1 = Math.toRadians(from.latitude);
        double lng1 = Math.toRadians(from.longitude);
        double lat2 = Math.toRadians(to.latitude);
        double lng2 = Math.toRadians(to.longitude);

        double dLng = lng2 - lng1;
        double y = Math.sin(dLng) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLng);
        double bearing = Math.toDegrees(Math.atan2(y, x));

        return (float) ((bearing + 360) % 360);
    }


    private void updateRouteToDestination(LatLng driverLocation) {
        // Determine destination based on booking status
        LatLng destination;

            destination = bookingDetails.getPickupLatLng();
        // Added work location marker
        MarkerOptions dropMarker = new MarkerOptions()
                .position(destination)
                .title("Drop Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_destination_long));
        mMap.addMarker(dropMarker);

        // Draw route between driver and destination
        String url = getDirectionsUrl(driverLocation, destination);
        new FetchRouteTask().execute(url);
    }

    private class FetchRouteTask extends AsyncTask<String, Void, List<LatLng>> {
        @Override
        protected List<LatLng> doInBackground(String... urls) {
            try {
                String jsonData = downloadUrl(urls[0]);
                return parseRouteData(jsonData);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<LatLng> routePoints) {
            if (routePoints != null) {
                drawRoute(routePoints);
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&key=" + getString(R.string.google_maps_key);
        return "https://maps.googleapis.com/maps/api/directions/json?" + parameters;
    }

    private String downloadUrl(String strUrl) throws IOException {
        StringBuilder jsonData = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            String line;
            while ((line = br.readLine()) != null) {
                jsonData.append(line);
            }

            br.close();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return jsonData.toString();
    }

    private List<LatLng> parseRouteData(String jsonData) {
        JSONObject jObject;
        List<LatLng> points = new ArrayList<>();

        try {
            jObject = new JSONObject(jsonData);
            JSONArray routes = jObject.getJSONArray("routes");

            if (routes.length() == 0) return points;

            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
            JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");

            for (int i = 0; i < steps.length(); i++) {
                String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                List<LatLng> decodedPoints = decodePolyline(polyline);
                points.addAll(decodedPoints);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return points;
    }

    private void drawRoute(List<LatLng> points) {
        if (mMap == null) return;

        // Remove existing polyline if any
        if (currentPolyline != null) {
            currentPolyline.remove();
        }

        // Draw new polyline
        PolylineOptions lineOptions = new PolylineOptions()
                .addAll(points)
                .width(12)
                .color(Color.BLUE)
                .geodesic(true);

        currentPolyline = mMap.addPolyline(lineOptions);

        // Adjust map camera to show entire route
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private Polyline currentPolyline;

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

//        if (bookingDetails != null) {
//            drawInitialRoute();
//        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
        }
    }

    private void showLoading(Boolean show) {
        if (show)
            custPrograssbar.prograssCreate(this);
        else custPrograssbar.closePrograssBar();
    }

    /**
     * Edit Drop location implementation
     */

    private static final int EDIT_LOCATION_REQUEST_CODE = 1001;

    private void showEditDropLocationBottomSheet() {
        // Show confirmation dialog first
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm_edit_location, null);
        AlertDialog confirmDialog = builder.setView(dialogView).create();

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> confirmDialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            confirmDialog.dismiss();
            launchEditLocationActivity();
        });

        confirmDialog.show();
    }

    private void launchEditLocationActivity() {
        Intent intent = new Intent(this, EditDropLocationActivity.class);
        intent.putExtra("current_lat", bookingDetails.getDropLat());
        intent.putExtra("current_lng", bookingDetails.getDropLng());
        intent.putExtra("current_address", bookingDetails.getDropAddress());
        startActivityForResult(intent, EDIT_LOCATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_LOCATION_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double newLat = data.getDoubleExtra("new_lat", 0);
            double newLng = data.getDoubleExtra("new_lng", 0);
            String newAddress = data.getStringExtra("new_address");
            System.out.println("newAddress::"+newAddress);
            updateDropLocation(newLat, newLng, newAddress, this::fetchBookingDetails);
        }
    }


    private GeoApiContext geoApiContext;
    private double totalDistance = 0;
    private double totalDuration = 0;
    private double totalDistanceValue = 0;
    private double totalDurationValue = 0;

    // Initialize this in onCreate
    private void initGeoApiContext() {
        geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
    }

    private double calculateNewTotalPrice(double currentDistance, double totalDistanceBeforeEdit,
                                          double totalPriceBeforeEdit, double basePrice) {
        // Calculate price per km from original booking
        double pricePerKm = totalPriceBeforeEdit / totalDistanceBeforeEdit;

        // Calculate new total price based on new distance
        double newTotalPrice = currentDistance * pricePerKm;

        // Ensure new price doesn't go below base price
        return Math.max(newTotalPrice, basePrice);
    }

    private void updateDropLocation(double lat, double lng, String address, Runnable onSuccess) {
        custPrograssbar.prograssCreate(this);

        // Get existing values
        double totalPriceBeforeEdit = Double.parseDouble(bookingDetails.getTotalPrice());
        double basePrice = bookingDetails.getBasePrice();
        double totalDistanceBeforeEdit = Double.parseDouble(bookingDetails.getDistance());

        // Calculate new price based on current distance
        DirectionsApiRequest request = DirectionsApi.newRequest(geoApiContext)
                .origin(new com.google.maps.model.LatLng(bookingDetails.getPickupLat(),
                        bookingDetails.getPickupLng()))
                .destination(new com.google.maps.model.LatLng(lat, lng))
                .mode(TravelMode.DRIVING);

        request.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];

                    // Get current distance in km
                    double currentDistance = route.legs[0].distance.inMeters / 1000.0;
                    String time = route.legs[0].duration.humanReadable;

                    // Calculate new total price
                    double newTotalPrice = calculateNewTotalPrice(
                            currentDistance,
                            totalDistanceBeforeEdit,
                            totalPriceBeforeEdit,
                            basePrice
                    );

                    // Update server with new values
                    updateLocationOnServer(lat, lng, address, currentDistance, time, newTotalPrice, onSuccess);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                runOnUiThread(() -> {
                    custPrograssbar.closePrograssBar();
                    showError("Failed to calculate distance");
                });
            }
        });
    }

    private void updateLocationOnServer(double lat, double lng, String address,
                                        double distance, String time, double newTotalPrice,
                                        Runnable onSuccess) {

        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject params = new JSONObject();
        try {
            params.put("booking_id", bookingId);
            params.put("customer_id", customerId);
            params.put("driver_id", bookingDetails.getDriverId());
            params.put("drop_lat", lat);
            params.put("drop_lng", lng);
            params.put("drop_address", address);
            params.put("destination_lat", lat);
            params.put("destination_lng", lng);
            params.put("distance", distance);
            params.put("time", time);
            params.put("total_price", newTotalPrice);
            params.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
            custPrograssbar.closePrograssBar();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                APIClient.baseUrl + "edit_handyman_agent_drop_location",
                params,
                response -> {
                    custPrograssbar.closePrograssBar();
                    showError("Drop location updated successfully");
                    if (onSuccess != null) onSuccess.run();
                },
                error -> {
                    custPrograssbar.closePrograssBar();
                    handleError(error);
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private String formatDuration(double minutes) {
        long hours = (long) (minutes / 60);
        long mins = (long) (minutes % 60);

        if (hours > 0) {
            return String.format("%dh %dm", hours, mins);
        } else {
            return String.format("%dm", mins);
        }
    }

}