package com.kapstranspvtltd.kaps.common_activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityLocationSearchBinding;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocationSearchActivity extends AppCompatActivity {
    private static final int PLACES_REQUEST_CODE = 1001;
    private ActivityLocationSearchBinding binding;
    private PreferenceManager preferenceManager;
    private List<String> recentSearches;
    private SearchAdapter searchAdapter;
    private ActivityResultLauncher<Intent> placesLauncher;

    // Move the interface outside the inner class
    public interface OnSearchItemClickListener {
        void onSearchItemClick(String search);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupClickListeners();
        loadRecentSearches();
        setupPlacesLauncher();
    }

    private void setupPlacesLauncher() {
        placesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        try {
                            Place place = Autocomplete.getPlaceFromIntent(result.getData());
                            if (place != null) {
                                binding.edtSearch.setText(place.getName());
                                saveRecentSearch(place.getName());

                                // Return the selected place
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("place_name", place.getName());
                                resultIntent.putExtra("place_lat", place.getLatLng().latitude);
                                resultIntent.putExtra("place_lng", place.getLatLng().longitude);
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Error processing selected location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeViews() {
        preferenceManager = new PreferenceManager(this);
        recentSearches = new ArrayList<>();

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }

        // Setup RecyclerViews
        binding.recyclerRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSearchResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.recyclerSearchResults.setVisibility(View.VISIBLE);
                    binding.recentSearchesLayout.setVisibility(View.GONE);
                    launchPlacesAutocomplete();
                } else {
                    binding.recyclerSearchResults.setVisibility(View.GONE);
                    binding.recentSearchesLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadRecentSearches() {
        Set<String> searches = preferenceManager.getStringSet("recent_searches", new HashSet<>());
        recentSearches.clear();
        recentSearches.addAll(searches);
        updateRecentSearchesAdapter();
    }

    private void updateRecentSearchesAdapter() {
        if (searchAdapter == null) {
            searchAdapter = new SearchAdapter(recentSearches, search -> {
                binding.edtSearch.setText(search);
                saveRecentSearch(search);
            }, true); // true for recent searches
            binding.recyclerRecentSearches.setAdapter(searchAdapter);
        } else {
            searchAdapter.updateSearches(recentSearches);
        }
        binding.txtFoundCount.setText(recentSearches.size() + " Found");
    }

    private void saveRecentSearch(String search) {
        Set<String> searches = new HashSet<>(recentSearches);
        searches.add(search);
        preferenceManager.saveStringSet("recent_searches", searches);
        loadRecentSearches();
    }

    private void launchPlacesAutocomplete() {
        try {
            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            Intent intent = builder.build(this);
            placesLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error launching places search", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACES_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (place != null) {
                    binding.edtSearch.setText(place.getName());
                    saveRecentSearch(place.getName());

                    // Return the selected place
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("place_name", place.getName());
                    resultIntent.putExtra("place_lat", place.getLatLng().latitude);
                    resultIntent.putExtra("place_lng", place.getLatLng().longitude);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        }
    }

    // Make the adapter class static
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
        private List<String> searches;
        private OnSearchItemClickListener listener;
        private boolean isRecentSearch;

        public SearchAdapter(List<String> searches, OnSearchItemClickListener listener, boolean isRecentSearch) {
            this.searches = searches;
            this.listener = listener;
            this.isRecentSearch = isRecentSearch;
        }

        @Override
        public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search, parent, false);
            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SearchViewHolder holder, int position) {
            String search = searches.get(position);
            holder.txtLocationName.setText(search);
            holder.txtLocationAddress.setText(""); // You can add address if available
            holder.imgRecent.setVisibility(isRecentSearch ? View.VISIBLE : View.GONE);

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

        static class SearchViewHolder extends RecyclerView.ViewHolder {
            TextView txtLocationName;
            TextView txtLocationAddress;
            ImageView imgLocation;
            ImageView imgRecent;

            SearchViewHolder(View itemView) {
                super(itemView);
                txtLocationName = itemView.findViewById(R.id.txtLocationName);
                txtLocationAddress = itemView.findViewById(R.id.txtLocationAddress);
                imgLocation = itemView.findViewById(R.id.imgLocation);
                imgRecent = itemView.findViewById(R.id.imgRecent);
            }
        }
    }
}