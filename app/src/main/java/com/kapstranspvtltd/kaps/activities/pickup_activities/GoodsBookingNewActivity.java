package com.kapstranspvtltd.kaps.activities.pickup_activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityGoodsBookingNewBinding;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GoodsBookingNewActivity extends AppCompatActivity {
    private static final int PICKUP_LOCATION_REQUEST = 1;
    private static final int DROP_LOCATION_REQUEST = 2;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final int PICKUP_MAP_REQUEST = 3;
    private static final int DROP_MAP_REQUEST = 4;

    private ActivityGoodsBookingNewBinding binding;
    private PreferenceManager preferenceManager;
    private FusedLocationProviderClient fusedLocationClient;
    private List<String> recentSearches;
    private SearchAdapter searchAdapter;
    private Pickup pickup;
    private Double pickupLat, pickupLng, dropLat, dropLng;
    private List<Drop> dropList;
    private String senderName = "", senderNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoodsBookingNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupPlacesAPI();
        loadRecentSearches();
        setupClickListeners();
        checkLocationPermission();
        getSenderDetails();
    }

    private void initializeViews() {
        preferenceManager = new PreferenceManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        recentSearches = new ArrayList<>();
        pickup = new Pickup();
        dropList = new ArrayList<>();

        binding.recyclerSearchDropResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getSenderDetails() {
        String customerName = preferenceManager.getStringValue("customer_name");
        String customerMobile = preferenceManager.getStringValue("customer_mobile_no");
        String senderName = preferenceManager.getStringValue("sender_name");
        String senderNumber = preferenceManager.getStringValue("sender_number");

        if (senderName == null || senderName.isEmpty() || senderNumber == null || senderNumber.isEmpty()) {
            this.senderName = customerName.split(" ")[0];
            this.senderNumber = customerMobile;
            preferenceManager.saveStringValue("sender_name", customerName);
            preferenceManager.saveStringValue("sender_number", customerMobile);
        } else {
            this.senderName = senderName;
            this.senderNumber = senderNumber;
        }
    }

    private void setupPlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
    }

    private void setupClickListeners() {
        binding.imgBack.setOnClickListener(v -> finish());

        // Pickup location related clicks
        binding.pickup.setOnClickListener(v -> launchPickupMap());
        binding.btnEditPickup.setOnClickListener(v -> launchPickupMap());


        // Drop location related clicks
        binding.drop.setOnClickListener(v -> launchPlacesAutocomplete(DROP_LOCATION_REQUEST));


        // Continue button
        binding.txtContinue.setOnClickListener(v -> validateAndProceed());

        // Add text change listeners for search functionality
        binding.drop.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRecentSearches(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


    }

    private void loadRecentSearches() {
        Set<String> searches = preferenceManager.getStringSet("recent_searches", new HashSet<>());
        recentSearches.clear();
        recentSearches.addAll(searches);
        updateSearchAdapter(recentSearches);
    }

    private void filterRecentSearches(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String search : recentSearches) {
            if (search.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(search);
            }
        }
        updateSearchAdapter(filteredList);
    }

    private void updateSearchAdapter(List<String> searches) {
        if (searchAdapter == null) {
            searchAdapter = new SearchAdapter(searches, search -> {
                binding.drop.setText(search);
                binding.recyclerSearchDropResults.setVisibility(View.GONE);
            });
            binding.recyclerSearchDropResults.setAdapter(searchAdapter);
        } else {
            searchAdapter.updateSearches(searches);
        }
    }

    private void saveRecentSearch(String search) {
        Set<String> searches = new HashSet<>(recentSearches);
        searches.add(search);
        preferenceManager.saveStringSet("recent_searches", searches);
        loadRecentSearches();
    }

    private void launchPlacesAutocomplete(int requestCode) {
        try {
            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            Intent intent = builder.build(this);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "Error launching places search", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchPickupMap() {
        Intent intent = new Intent(this, GoodsPickupMapLocationActivity.class);
        intent.putExtra("pickup_address", binding.pickup.getText().toString());
        startActivityForResult(intent, PICKUP_MAP_REQUEST);
    }

    private void launchDropMap() {
        Intent intent = new Intent(this, GoodsDriverMapDropLocationActivity.class);
        intent.putExtra("drop_address", binding.drop.getText().toString());
        startActivityForResult(intent, DROP_MAP_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICKUP_LOCATION_REQUEST || requestCode == DROP_LOCATION_REQUEST) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place != null) {
                    String placeName = place.getName();
                    if (requestCode == PICKUP_LOCATION_REQUEST) {
                        binding.pickup.setText(placeName);
                        pickupLat = place.getLatLng().latitude;
                        pickupLng = place.getLatLng().longitude;
                        saveRecentSearch(placeName);
                    } else if (requestCode == DROP_LOCATION_REQUEST) {
                        binding.drop.setText(placeName);
                        dropLat = place.getLatLng().latitude;
                        dropLng = place.getLatLng().longitude;
                        saveRecentSearch(placeName);
                    }
                }
            } else if (requestCode == PICKUP_MAP_REQUEST) {
                Pickup pickup = data.getParcelableExtra("pickup");
                if (pickup != null) {
                    binding.pickup.setText(pickup.getAddress());
                    pickupLat = pickup.getLat();
                    pickupLng = pickup.getLog();
                    saveRecentSearch(pickup.getAddress());
                }
            } else if (requestCode == DROP_MAP_REQUEST) {
                Drop drop = data.getParcelableExtra("drop");
                if (drop != null) {
                    binding.drop.setText(drop.getAddress());
                    dropLat = drop.getLat();
                    dropLng = drop.getLog();
                    saveRecentSearch(drop.getAddress());
                }
            }
        }
    }

    private void validateAndProceed() {
        String pickup = binding.pickup.getText().toString().trim();
        String drop = binding.drop.getText().toString().trim();

        if (pickup.isEmpty()) {
            binding.pickup.setError("Please select pickup location");
            return;
        }

        if (drop.isEmpty()) {
            binding.drop.setError("Please select drop location");
            return;
        }

        if (pickupLat == null || pickupLng == null) {
            Toast.makeText(this, "Please select valid pickup location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dropLat == null || dropLng == null) {
            Toast.makeText(this, "Please select valid drop location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Pickup object
        Pickup pickupObj = new Pickup();
        pickupObj.setLat(pickupLat);
        pickupObj.setLog(pickupLng);
        pickupObj.setAddress(pickup);
        pickupObj.setRname(senderName);
        pickupObj.setRmobile(senderNumber);

        // Create Drop object
        Drop dropObj = new Drop();
        dropObj.setLat(dropLat);
        dropObj.setLog(dropLng);
        dropObj.setAddress(drop);

        // Clear existing drop list
        if (dropList != null) {
            dropList.clear();
        }
        dropList.add(dropObj);

        // Proceed to review map
        Intent intent = new Intent(this, ReviewMapActivity.class);
        intent.putExtra("pickup", pickupObj);
        intent.putExtra("drop", dropObj);
        intent.putExtra("cab", false);
        startActivity(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            pickupLat = location.getLatitude();
                            pickupLng = location.getLongitude();
                            // You can use Geocoder to get address from coordinates
                            // and set it as default pickup location
                        }
                    });
        }
    }

    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
        private List<String> searches;
        private OnSearchItemClickListener listener;

        public interface OnSearchItemClickListener {
            void onSearchItemClick(String search);
        }

        public SearchAdapter(List<String> searches, OnSearchItemClickListener listener) {
            this.searches = searches;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            String search = searches.get(position);
            holder.textView.setText(search);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSearchItemClick(search);
                }
            });
        }

        @Override
        public int getItemCount() {
            return searches.size();
        }

        public void updateSearches(List<String> newSearches) {
            this.searches = newSearches;
            notifyDataSetChanged();
        }

        class SearchViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            SearchViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}