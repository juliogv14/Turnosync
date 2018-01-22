package com.juliogv14.turnosync.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.regex.Pattern;

/**
 * Created by Julio on 15/11/2017.
 * ${FILE_NAME}
 */

public class FormUtils {

    public static boolean isEmailValid(String email) {

        //Regex to check if the string contains '@' and '.' as in an email.
        String regEx = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.compile(regEx).matcher(email).matches();
    }

    public static boolean isLoginPasswordValid(String password) {

        return password.length() >= 6;
    }

    public static boolean isRegisterPasswordValid(String password) {

        String regEx = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";
        return Pattern.compile(regEx).matcher(password).matches();
    }

    public static boolean isDisplayNameValid(String displayName) {
        String regEx = "^[\\p{L}-' 0-9]+$";
        return Pattern.compile(regEx).matcher(displayName).matches();
    }

    public static void showLoadingIndicator(View indicatorView, boolean show) {
        if (show) {
            AnimationViewUtils.animateView(indicatorView, View.VISIBLE, 0.4f, 200);
        } else {
            AnimationViewUtils.animateView(indicatorView, View.GONE, 0, 200);
        }
    }

    public static boolean closeKeyboard(Context context, View focusView) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focusView != null && imm != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    public static void checkGooglePlayServices(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(context);
        String TAG = context.getClass().getSimpleName();

        switch (result) {
            //TODO: Toast string
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Log.w(TAG, "SERVICE_VERSION_UPDATE_REQUIRED");
                Toast.makeText(context, "Please update Google Play Services", Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SUCCESS:
                Log.d(TAG, "Play service available success");
                break;
            default:
                Log.d(TAG, "unknown services result: " + result);

        }
    }
}
