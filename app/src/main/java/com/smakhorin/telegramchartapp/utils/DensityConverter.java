package com.smakhorin.telegramchartapp.utils;

import android.content.res.Resources;

public class DensityConverter {

    /**
     * Converts Density-independent Pixels to regular pixels based on density
     * @param dp       dp pixels to convert
     * @return         converted pixels
     */
    public static float dpToPx(float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }
}
