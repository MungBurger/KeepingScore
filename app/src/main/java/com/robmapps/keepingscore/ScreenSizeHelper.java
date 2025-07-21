package com.robmapps.keepingscore;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

/**
 * Helper class to detect screen size and provide appropriate UI adjustments
 */
public class ScreenSizeHelper {

    /**
     * Check if the device is a tablet based on screen size
     * 
     * @param context The application context
     * @return true if the device is a tablet, false otherwise
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout 
                & Configuration.SCREENLAYOUT_SIZE_MASK) 
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * Check if the device is in landscape orientation
     * 
     * @param context The application context
     * @return true if the device is in landscape orientation, false otherwise
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation 
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Get the screen width in dp
     * 
     * @param context The application context
     * @return The screen width in dp
     */
    public static float getScreenWidthDp(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.screenWidthDp;
    }

    /**
     * Get the screen height in dp
     * 
     * @param context The application context
     * @return The screen height in dp
     */
    public static float getScreenHeightDp(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.screenHeightDp;
    }

    /**
     * Convert dp to pixels
     * 
     * @param context The application context
     * @param dp The value in dp
     * @return The value in pixels
     */
    public static int dpToPx(Context context, float dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * displayMetrics.density);
    }
}