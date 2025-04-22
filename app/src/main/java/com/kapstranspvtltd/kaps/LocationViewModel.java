package com.kapstranspvtltd.kaps;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kapstranspvtltd.kaps.activities.models.RecieverContact;
import com.kapstranspvtltd.kaps.activities.models.SenderContact;

import java.util.List;

public class LocationViewModel extends ViewModel {
    private final MyApplication vtPartnerApp;
    private final MutableLiveData<List<Location>> _searchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _searchDropResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _cabPickupSearchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _cabDropSearchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _jcbCraneWorkSearchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _driverPickupSearchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _driverDropSearchResults = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _handymanWorkSearchResults = new MutableLiveData<>();

    private final MutableLiveData<List<Location>> _recentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _recentDropLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _cabPickupRecentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _cabDropRecentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _jcbCraneWorkRecentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _driverPickupRecentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _driverDropRecentLocations = new MutableLiveData<>();
    private final MutableLiveData<List<Location>> _handymanWorkRecentLocations = new MutableLiveData<>();

    private final MutableLiveData<Location> _selectedLocation = new MutableLiveData<>();
    private Location pickupLocation;
    private Location dropLocation;

    private final MutableLiveData<SenderContact> _senderContact = new MutableLiveData<>();
    private final MutableLiveData<RecieverContact> _receiverContact = new MutableLiveData<>();

    private static final int MAX_RECENT_LOCATIONS = 5;
    private static final String RECENT_LOCATIONS_KEY = "recent_locations";
    private static final String RECENT_DROP_LOCATIONS_KEY = "recent_drop_locations";
    private static final String RECENT_CAB_PICKUP_LOCATIONS_KEY = "cab_pickup_recent_locations";
    private static final String RECENT_CAB_DROP_LOCATIONS_KEY = "cab_drop_recent_locations";
    private static final String RECENT_JCB_CRANE_WORK_LOCATIONS_KEY = "jcb_crane_work_recent_locations";
    private static final String RECENT_DRIVER_PICKUP_LOCATIONS_KEY = "driver_pickup_recent_locations";
    private static final String RECENT_DRIVER_DROP_LOCATIONS_KEY = "driver_drop_recent_locations";
    private static final String RECENT_HANDYMAN_WORK_LOCATIONS_KEY = "handyman_work_recent_locations";

    public LocationViewModel(MyApplication vtPartnerApp) {
        this.vtPartnerApp = vtPartnerApp;
    }

    // Getters for LiveData


    public LiveData<List<Location>> getSearchResults() {
        return _searchResults;
    }

    public LiveData<List<Location>> getSearchDropResults() {
        return _searchDropResults;
    }

    public LiveData<List<Location>> getCabPickupSearchResults() {
        return _cabPickupSearchResults;
    }

    public LiveData<List<Location>> getCabDropSearchResults() {
        return _cabDropSearchResults;
    }

    public LiveData<List<Location>> getJcbCraneWorkSearchResults() {
        return _jcbCraneWorkSearchResults;
    }

    public LiveData<List<Location>> getDriverPickupSearchResults() {
        return _driverPickupSearchResults;
    }

    public LiveData<List<Location>> getDriverDropSearchResults() {
        return _driverDropSearchResults;
    }

    public LiveData<List<Location>> getHandymanWorkSearchResults() {
        return _handymanWorkSearchResults;
    }

    // Recent Locations LiveData getters
    public LiveData<List<Location>> getRecentLocations() {
        return _recentLocations;
    }

    public LiveData<List<Location>> getRecentDropLocations() {
        return _recentDropLocations;
    }

    public LiveData<List<Location>> getCabPickupRecentLocations() {
        return _cabPickupRecentLocations;
    }

    public LiveData<List<Location>> getCabDropRecentLocations() {
        return _cabDropRecentLocations;
    }

    public LiveData<List<Location>> getJcbCraneWorkRecentLocations() {
        return _jcbCraneWorkRecentLocations;
    }

    public LiveData<List<Location>> getDriverPickupRecentLocations() {
        return _driverPickupRecentLocations;
    }

    public LiveData<List<Location>> getDriverDropRecentLocations() {
        return _driverDropRecentLocations;
    }

    public LiveData<List<Location>> getHandymanWorkRecentLocations() {
        return _handymanWorkRecentLocations;
    }

    // Selected Location and Contact LiveData getters
    public LiveData<Location> getSelectedLocation() {
        return _selectedLocation;
    }

    public LiveData<SenderContact> getSenderContact() {
        return _senderContact;
    }

    public LiveData<RecieverContact> getReceiverContact() {
        return _receiverContact;
    }

    // Location getters
    public Location getPickupLocation() {
        return pickupLocation;
    }

    public Location getDropLocation() {
        return dropLocation;
    }
}