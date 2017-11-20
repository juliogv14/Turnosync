package com.juliogv14.turnosync.utils;

import android.view.View;

import java.util.regex.Pattern;

/**
 * Created by Julio on 15/11/2017.
 * ${FILE_NAME}
 */

public class LoginUtils {

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

}
