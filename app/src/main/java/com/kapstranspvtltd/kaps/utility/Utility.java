package com.kapstranspvtltd.kaps.utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.kapstranspvtltd.kaps.MyApplication;
import com.kapstranspvtltd.kaps.model.ResponKM;
import com.kapstranspvtltd.kaps.retrofit.MapsApi;
import com.kapstranspvtltd.kaps.R;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utility {

    public static String paymentId = "0";

    public static int paymentsucsses = 0;
    public static String tragectionID = "0";

    public static int isvarification = -1;
    public static int newAddress = 0;
    public static int isSinglePoint = 0;
    public static boolean changeAddress = false;
    public static boolean ratesupdat = false;
    public static boolean walletActivat = false;
    public static Dialog popupWindow;


    public static GoogleApiClient googleApiClient;

    public static void enableLoc(Activity context) {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.i("test", "-->");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })

                    .addOnConnectionFailedListener(connectionResult -> Log.d("Location error", "Location error " + connectionResult.getErrorCode())).build();
            googleApiClient.connect();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();
            if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {// Show the dialog by calling startResolutionForResult(),
                try {
                    status.startResolutionForResult(context, 210);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    public static boolean internetChack() {
        ConnectivityManager ConnectionManager = (ConnectivityManager) MyApplication.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static void showProgress(final Context context) {
        try {
            if (!((Activity) context).isFinishing()) {
                View layout = LayoutInflater.from(context).inflate(R.layout.popup_loading, null);
                popupWindow = new Dialog(context, android.R.style.Theme_Translucent);
                popupWindow.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popupWindow.setContentView(layout);
                popupWindow.setCancelable(false);
                if (!((Activity) context).isFinishing()) {
                    popupWindow.show();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hideProgress() {
        try {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getDirections(Double flat, Double flon, Double tlat, Double tlon) throws IOException {

        String baseUrl = "https://maps.googleapis.com/";


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MapsApi mapsApi = retrofit.create(MapsApi.class);

        Call<ResponKM> call = mapsApi.getDirections(
                flat + "," + flon,
                tlat + "," + tlon,
                "driving",
                MyApplication.mContext.getResources().getString(R.string.API_KEY)
        );

        Response<ResponKM> response = call.execute();

        if (response.isSuccessful()) {
            ResponKM route = response.body();
            try {


                if (route.getRoutes() != null && !route.getRoutes().get(0).getLegs().isEmpty()) {
                    double roadDistance = route.getRoutes().get(0).getLegs().get(0).getDistance().getValue() / 1000.0;
                    Log.e("Distance", "roadDistance" + roadDistance);
                    return roadDistance;
                } else {
                    // Handle error
                    return 0.0;
                }
            } catch (Exception e) {
                return 0.0;
            }
        } else {
            // Handle error
            return 0.0;
        }
    }
}
