package com.juliogv14.turnosync.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class InterfaceUtils {
    public static int dpToPx(Context c, float dp){
        DisplayMetrics dm = c.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }
}
