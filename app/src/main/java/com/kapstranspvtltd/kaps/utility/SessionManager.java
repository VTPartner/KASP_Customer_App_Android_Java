package com.kapstranspvtltd.kaps.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.kapstranspvtltd.kaps.model.SelectAddress;
import com.pusher.client.channel.User;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    public static List<Drop> dropList=new ArrayList<>();
    private final SharedPreferences mPrefs;

    private  com.kapstranspvtltd.kaps.utility.PreferenceManager preferenceManager;
    SharedPreferences.Editor mEditor;
    public static String rtl = "rtl";
    public static String intro = "intro";
    public static String login = "login";
    public static String user = "users";
    public static String currency = "currency";
    public static String coupon = "coupon";
    public static String couponid = "couponid";
    public static String wallet = "wallet";
    public static String contact = "contact";
    public static String language = "language";


    public SessionManager(Context context) {

        preferenceManager = new com.kapstranspvtltd.kaps.utility.PreferenceManager(context);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mPrefs.edit();
    }
    public void setStringData(String key, String val) {
        mEditor.putString(key, val);
        mEditor.commit();
    }
    public String getStringData(String key) {
        return mPrefs.getString(key, null);
    }
    public void setFloatData(String key, float val) {
        mEditor.putFloat(key, val);
        mEditor.commit();
    }
    public float getFloatData(String key) {
        return mPrefs.getFloat(key, 0);
    }
    public void setBooleanData(String key, Boolean val) {
        mEditor.putBoolean(key, val);
        mEditor.commit();
    }
    public boolean getBooleanData(String key) {
        return mPrefs.getBoolean(key, false);
    }

    public void setIntData(String key, int val) {
        mEditor.putInt(key, val);
        mEditor.commit();
    }
    public int getIntData(String key) {
        return mPrefs.getInt(key, 0);
    }

    public void setUserDetails(User val) {
        mEditor.putString(user, new Gson().toJson(val));
        mEditor.commit();
    }
    public User getUserDetails() {
        return new Gson().fromJson(mPrefs.getString(user, ""), User.class);
    }

    public void setAddress(SelectAddress val) {
        mEditor.putString("address", new Gson().toJson(val));
        mEditor.commit();
    }

    public SelectAddress getAddress() {
        return new Gson().fromJson(mPrefs.getString("address", ""), SelectAddress.class);
    }

    public void logoutUser() {
        mEditor.clear();
        mEditor.commit();
    }


    public String getCustomerId() {
        return  preferenceManager.getStringValue("customer_id", "");
    }
}
