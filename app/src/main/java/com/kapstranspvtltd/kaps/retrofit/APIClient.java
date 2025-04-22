package com.kapstranspvtltd.kaps.retrofit;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    public static final String RAZORPAY_KEY = "rzp_test_61op4YoSkMBW6u"; //Test key rzp_test_crEnVFpHxMh7sZ
    static Retrofit retrofit = null;
    //    public static String baseUrl = "http://77.37.47.156:8000/api/vt_partner/";
//    public static String baseUrl = "https://www.vtpartner.org/api/vt_partner/";
    private static final int DEV_MODE = 0; // If Dev Mode is 1 then development server is on and if 0 then production server is on

    public static final String baseUrl = DEV_MODE == 1
            ? "http://100.24.44.74:8000/api/vt_partner/"
            : "http://100.24.44.74/api/vt_partner/";
    //        public static String baseUrl = "http://100.24.44.74:8000/api/vt_partner/";
//        public static String baseUrl = "http://100.24.44.74/api/vt_partner/";
    public static String MAP_KEY = "AIzaSyAAlmEtjJOpSaJ7YVkMKwdSuMTbTx39l_o";


    public static UserService getInterface() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(UserService.class);

    }


}
