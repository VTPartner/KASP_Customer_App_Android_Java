package com.kapstranspvtltd.kaps.retrofit;



import com.kapstranspvtltd.kaps.model.ResponKM;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapsApi {
    @GET("maps/api/directions/json")
    Call<ResponKM> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("mode") String mode,
            @Query("key") String apiKey
    );

}
