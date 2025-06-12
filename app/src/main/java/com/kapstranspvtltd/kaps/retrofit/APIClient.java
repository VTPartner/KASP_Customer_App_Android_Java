package com.kapstranspvtltd.kaps.retrofit;


import android.graphics.Bitmap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {

    public static final String RAZORPAY_KEY = "rzp_live_2A74eAF3LgxMVM"; //Test key rzp_test_crEnVFpHxMh7sZ
    static Retrofit retrofit = null;
    //    public static String baseUrl = "http://77.37.47.156:8000/api/vt_partner/";
//    public static String baseUrl = "https://www.vtpartner.org/api/vt_partner/";
    private static final int DEV_MODE = 0; // If Dev Mode is 1 then development server is on and if 0 then production server is on

    public static final String baseUrl = DEV_MODE == 1
            ? "http://100.24.44.74:8000/api/vt_partner/"
            : "https://www.kaps9.in/api/vt_partner/";
    //        public static String baseUrl = "http://100.24.44.74:8000/api/vt_partner/";
//        public static String baseUrl = "http://100.24.44.74/api/vt_partner/";
    public static String MAP_KEY = "AIzaSyAAlmEtjJOpSaJ7YVkMKwdSuMTbTx39l_o";

    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxHeight;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


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
