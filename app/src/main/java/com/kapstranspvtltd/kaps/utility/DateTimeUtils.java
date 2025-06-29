package com.kapstranspvtltd.kaps.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static String formatEpochToDateTime(long epochTime) {
        try {
            Date date = new Date(epochTime * 1000L); // Convert epoch seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatEpochToDate(long epochTime) {
        try {
            Date date = new Date(epochTime * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatEpochToTime(long epochTime) {
        try {
            Date date = new Date(epochTime * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatEpochToDay(long epochTime) {
        try {
            Date date = new Date(epochTime * 1000L);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}