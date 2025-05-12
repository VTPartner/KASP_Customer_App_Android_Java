package com.kapstranspvtltd.kaps.activities.pickup_activities;

import static android.os.Build.VERSION.SDK_INT;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.model.User;
import com.kapstranspvtltd.kaps.polygon.Point;
import com.kapstranspvtltd.kaps.polygon.Polygon;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.utility.Utility;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityGoodsDriverMapDropLocationBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class GoodsDriverMapDropLocationActivity extends BaseActivity implements OnMapReadyCallback {
    private static final int MAX_DROP_LOCATIONS = 3;
    private ActivityGoodsDriverMapDropLocationBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CustPrograssbar custPrograssbar;
    private SessionManager sessionManager;
    private User user;
    private Polygon polygon;
    private Double latitude;
    private Double longitude;
    private Bundle addressBundle;
    private boolean currentLocation = false;
    private Pickup pickup;

    boolean cabService;

    PreferenceManager preferenceManager;

    boolean showExactLocation = false;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        Log.e("TAG", "Place: " + place.getName() + ", " + place.getId());
//                        binding.edSearch.setText(place.getName());
//                        showExactLocation = true;
                        mMap.clear();
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 100);
                        mMap.animateCamera(yourLocation);

                        Point point = new Point(place.getLatLng().latitude, place.getLatLng().longitude);
                        if (polygon != null) {
//                            boolean contains = polygon.contains(point);
//                            Log.e("resulr", "---> " + contains);
                            if (true) {

                                binding.lvlSorry.setVisibility(View.GONE);
                                binding.btnSend.setVisibility(View.VISIBLE);
                            } else {
                                binding.lvlSorry.setVisibility(View.VISIBLE);
                                binding.btnSend.setVisibility(View.GONE);

                            }
                        } else {
                            binding.btnSend.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        e.toString();

                    }


                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoodsDriverMapDropLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        initializeViews();
        setupClickListeners();

        setupMap();
    }

    private void initializeViews() {
        pickup = getIntent().getParcelableExtra("pickup");
        cabService = getIntent().getBooleanExtra("cab",false);
//        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        String address = pickup.getAddress();
        if(address == null || address.isEmpty()){
            Toast.makeText(this,"Please re-confirm your pickup location",Toast.LENGTH_LONG).show();
            return;
        }
//        user = sessionManager.getUserDetails();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
    }

    private void setupClickListeners() {
        // Improve click handling
        View.OnClickListener singleClickListener = new View.OnClickListener() {
            private static final long CLICK_TIME_THRESHOLD = 300;
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > CLICK_TIME_THRESHOLD) {
                    lastClickTime = currentTime;
                    handleClick(v);
                }
            }
        };

        binding.imgBack.setOnClickListener(singleClickListener);
        binding.edSearch.setOnClickListener(singleClickListener);
        binding.btnSend.setOnClickListener(singleClickListener);
        binding.imgCurrunt.setOnClickListener(singleClickListener);

        // Make search EditText more responsive
        binding.edSearch.setFocusable(false);
        binding.edSearch.setClickable(true);
    }

    private void handleClick(View v) {
        if (v.getId() == R.id.img_back) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else if (v.getId() == R.id.ed_search) {
            launchPlacesAutocomplete();
        } else if (v.getId() == R.id.btn_send) {
            showBottomConfirmDialog();
        } else if (v.getId() == R.id.img_currunt) {
            animateCurrentLocationButton();
            getCurrentLocation();
        }
    }

    private void animateCurrentLocationButton() {
        binding.imgCurrunt.animate()
                .rotationBy(360f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (mMap != null) {
                        currentLocation = true;
                        onMapReady(mMap);
                    }
                })
                .start();
    }


    private void launchPlacesAutocomplete() {
        try {
            // Disable the search view temporarily to prevent double clicks
            binding.edSearch.setEnabled(false);

            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            Intent intent = builder.build(this);
            launcher.launch(intent);

            // Re-enable after a delay
            binding.edSearch.postDelayed(() ->
                    binding.edSearch.setEnabled(true), 1000);
        } catch (Exception e) {
            Log.e("Places", "Error launching autocomplete: " + e.getMessage());
            binding.edSearch.setEnabled(true);
        }
    }

    private void showBottomConfirmDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        dialog.setContentView(sheetView);
        String fulladdress = "";
        if(addressBundle != null)
            fulladdress = addressBundle.getString("drop_fulladdress");

        TextView pickupLabel = sheetView.findViewById(R.id.labelPickup);
        TextView pickupAddress = sheetView.findViewById(R.id.confirm_pickaddress);

        TextView title = sheetView.findViewById(R.id.textLabel);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        edName = sheetView.findViewById(R.id.ed_name);
        edMobile = sheetView.findViewById(R.id.ed_mobile);
        TextView btnConfirm = sheetView.findViewById(R.id.btn_send);
        LinearLayout contactLyt = sheetView.findViewById(R.id.contactLyt);
        ImageButton btnContacts = sheetView.findViewById(R.id.btnContacts);
        btnContacts.setOnClickListener(v -> checkContactPermission());

        if(cabService){
            contactLyt.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            chUser.setVisibility(View.GONE);
        }

        pickupAddress.setText(fulladdress);
        pickupLabel.setText("Drop Location");

        chUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String customer_name = preferenceManager.getStringValue("customer_name");
                String customer_mobile_no = preferenceManager.getStringValue("customer_mobile_no");
                edName.setText(customer_name);
                edMobile.setText(customer_mobile_no);
            } else {
                edName.setText("");
                edMobile.setText("");
            }
        });

        btnConfirm.setOnClickListener(v -> {
if(edMobile.getText().toString().trim().isEmpty() || edName.getText().toString().trim().isEmpty()){
    dialog.dismiss();
    showError("Please provide the receiver contact details.");
    return;
}
            if ( cabService == false && !edMobile.getText().toString().trim().isEmpty() && !TextUtils.isEmpty(edName.getText())) {
                if(addressBundle == null || addressBundle.isEmpty()){
                    dialog.dismiss();
                    showError("Please select the drop location first");
                    return;
                }
                dialog.dismiss();
                Drop drop = new Drop();
                drop.setLat(latitude);
                drop.setLog(longitude);
                drop.setAddress(addressBundle.getString("drop_fulladdress"));
                drop.setRname(edName.getText().toString().trim());
                drop.setRmobile(edMobile.getText().toString().trim());

                if (dropList.size() == 3) {
                    // Replace the last drop with the new one
                    dropList.set(2, drop);
                    Toast.makeText(this, "Last drop location updated", Toast.LENGTH_SHORT).show();
                } else {
                    dropList.add(drop);
                }


                startActivity(new Intent(this, ReviewMapActivity.class)
                        .putExtra("cab",cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));

            }else{
                if(addressBundle == null || addressBundle.isEmpty()){
                    dialog.dismiss();
                    showError("Please select the drop location first");
                    return;
                }
                dialog.dismiss();
                Drop drop = new Drop();
                drop.setLat(latitude);
                drop.setLog(longitude);
                drop.setAddress(addressBundle.getString("drop_fulladdress"));
                drop.setRname("");
                drop.setRmobile("");
                dropList.add(drop);

                startActivity(new Intent(this, ReviewMapActivity.class)
                        .putExtra("cab",cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));
            }
        });

        dialog.show();
    }

    private void showBottomConfirmDialogNew() {
        if (dropList != null && dropList.size() >= MAX_DROP_LOCATIONS) {
            showError("Maximum " + MAX_DROP_LOCATIONS + " drop locations allowed");
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        dialog.setContentView(sheetView);
        String fulladdress = "";
        if(addressBundle != null)
            fulladdress = addressBundle.getString("drop_fulladdress");

        TextView pickupLabel = sheetView.findViewById(R.id.labelPickup);
        TextView pickupAddress = sheetView.findViewById(R.id.confirm_pickaddress);
        TextView title = sheetView.findViewById(R.id.textLabel);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        edName = sheetView.findViewById(R.id.ed_name);
        edMobile = sheetView.findViewById(R.id.ed_mobile);
        TextView btnConfirm = sheetView.findViewById(R.id.btn_send);
        LinearLayout contactLyt = sheetView.findViewById(R.id.contactLyt);
        ImageButton btnContacts = sheetView.findViewById(R.id.btnContacts);

        pickupAddress.setText(fulladdress);
        pickupLabel.setText("Drop Location " + (dropList.size() + 1));

        // Update UI based on whether it's the last drop point
        boolean isLastDrop = dropList.size() == MAX_DROP_LOCATIONS - 1;
        btnConfirm.setText(isLastDrop ? "Proceed to Review" : "Add Drop Point");

        btnConfirm.setOnClickListener(v -> {
            if (addressBundle == null || addressBundle.isEmpty()) {
                dialog.dismiss();
                showError("Please select the drop location first");
                return;
            }

            Drop drop = new Drop();
            drop.setLat(latitude);
            drop.setLog(longitude);
            drop.setAddress(addressBundle.getString("drop_fulladdress"));

            if (!cabService && !TextUtils.isEmpty(edMobile.getText()) && !TextUtils.isEmpty(edName.getText())) {
                drop.setRname(edName.getText().toString().trim());
                drop.setRmobile(edMobile.getText().toString().trim());
            } else {
                drop.setRname("");
                drop.setRmobile("");
            }

            dropList.add(drop);
            dialog.dismiss();

            if (isLastDrop || cabService) {
                // If this is the last allowed drop or cab service, go to review
                startActivity(new Intent(this, ReviewMapActivity.class)
                        .putExtra("cab", cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));
            } else {
                // Show success message and allow adding another drop point
                showSuccess("Drop point " + dropList.size() + " added successfully");
                // Clear the map for next drop point
                if (mMap != null) {
                    mMap.clear();
                    binding.txtAddress.setText("");
                    binding.locationMarkertext.setVisibility(View.GONE);
                }
            }
        });

        dialog.show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimary))
                .show();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorerror))
                .show();
    }


    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Use async map loading
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                configureMap();
                onMapReady(googleMap);
            });
        }
    }

    private void configureMap() {
        if (mMap == null) return;

        // Optimize map settings for smooth performance
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(10);

        // Enable hardware acceleration for the map
        View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView != null) {
            mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng = mMap.getCameraPosition().target;
            if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                mMap.clear();
                Point point = new Point(latLng.latitude, latLng.longitude);
//                boolean contains = polygon.contains(point);
                if (true) {
                    binding.lvlSorry.setVisibility(View.GONE);
                    binding.btnSend.setVisibility(View.VISIBLE);
                    new GetAddressFromLatLng().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            latLng.latitude,
                            latLng.longitude
                    );
                } else {
                    binding.lvlSorry.setVisibility(View.VISIBLE);
                    binding.btnSend.setVisibility(View.GONE);
                }
            } else {
                handleAndroidRLocation();
            }
        });
        getCurrentLocation();
        if (currentLocation) {
            getCurrentLocation();
        }
    }

    private void handleAndroidRLocation() {
        if (SDK_INT == Build.VERSION_CODES.R) {
            try {
                LocationManager systemService = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(),
                        (Consumer<Location>) locationCallback -> {
                            LatLng latLng = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                            Point point = new Point(latLng.latitude, latLng.longitude);
//                            boolean contains = polygon.contains(point);
                            if (true) {
                                binding.lvlSorry.setVisibility(View.GONE);
                                binding.btnSend.setVisibility(View.VISIBLE);
                                new GetAddressFromLatLng().executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR,
                                        latLng.latitude,
                                        latLng.longitude
                                );
                            } else {
                                binding.lvlSorry.setVisibility(View.VISIBLE);
                                binding.btnSend.setVisibility(View.GONE);
                            }
                        });
            } catch (Exception e) {
                Log.e("error", "->" + e.toString());
            }
        }
    }

    private void getCurrentLocation() {
        currentLocation = false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            moveMapToLocation(location);
        } else {
            LocationManager systemService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (SDK_INT == Build.VERSION_CODES.R) {
                handleAndroidRCurrentLocation(systemService);
            } else {
                handleOlderVersionsLocation();
            }
        }
    }

    private void handleAndroidRCurrentLocation(LocationManager systemService) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (SDK_INT >= Build.VERSION_CODES.R) {
            systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(),
                    (Consumer<Location>) locationCallback -> {
                        LatLng coordinate = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 100);
                        mMap.animateCamera(yourLocation);
                    });
        }
    }

    private void handleOlderVersionsLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
        lastLocation.addOnSuccessListener(this, location -> {
            if (location != null) {
                moveMapToLocation(location);
            } else {
                Utility.enableLoc(this);
                Toast.makeText(this, getString(R.string.location_not_avalible), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveMapToLocation(Location location) {
        if (mMap == null) return;

        mMap.clear();
        showExactLocation = false;
        LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

        // Smooth camera movement
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 100);
        mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // Animation completed
                binding.locationMarkertext.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            }

            @Override
            public void onCancel() {
                // Animation canceled
            }
        });
    }

    // Add permission handling
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Add lifecycle methods for location updates
    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && currentLocation) {
            getCurrentLocation();
        }
    }

    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(GoodsDriverMapDropLocationActivity.this);
            addressBundle = new Bundle();
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder = new Geocoder(GoodsDriverMapDropLocationActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    String subLocality = address.getSubLocality();
                    addressBundle.putString(getString(R.string.addressline2),
                            subLocality != null ? subLocality : address.getFeatureName());

                    appendAddressComponent(sb, address.getAddressLine(0));
                    appendAddressComponent(sb, address.getLocality());
                    appendAddressComponent(sb, address.getAdminArea());
                    appendAddressComponent(sb, address.getCountryName());
                    appendAddressComponent(sb, address.getPostalCode());

                    addressBundle.putString("drop_fulladdress", sb.toString().trim());
                    return addressBundle;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                addressBundle.putBoolean("error", true);
                return addressBundle;
            }
        }

        private void appendAddressComponent(StringBuilder sb, String component) {
            if (component != null) {
                sb.append(component).append(" ");
            }
        }

        @Override
        protected void onPostExecute(Bundle userAddress) {
            super.onPostExecute(userAddress);
            try {
                Utility.hideProgress();
                if (userAddress != null) {
                    String address = userAddress.getString("drop_fulladdress");
                    if (address != null) {
                        binding.txtAddress.setText(address);
//                        if(showExactLocation == false)
//                            binding.edSearch.setText(address);
                        binding.locationMarkertext.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (custPrograssbar != null) {
            custPrograssbar.closePrograssBar();
        }

    }

    private static final int CONTACT_PERMISSION_CODE = 100;
    private static final int CONTACT_PICK_CODE = 101;

    EditText edName ;
    EditText edMobile ;
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACT_PERMISSION_CODE);
        } else {
            pickContact();
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICK_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    getContactDetails(uri);
                }
            }
        }
    }

    @SuppressLint("Range")
    private void getContactDetails(Uri uri) {
        String name = "";
        String phoneNumber = "";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneCursor.close();
                }
            }
            cursor.close();



            if (edName != null && edMobile != null) {
                edName.setText(name);
                edMobile.setText(phoneNumber.replaceAll("[^0-9]", "")); // Remove non-numeric characters
            }
        }
    }
}