package com.juliogv14.turnosync.utils;

import java.util.regex.Pattern;

/**
 * Created by Julio on 15/11/2017.
 * ${FILE_NAME}
 */

public class LoginUtils {

    public static boolean isEmailValid(String email) {

        //Regex to check if the string contains '@' and '.' as in an email.
        String regEx = ".*@.*\\..*";
        return Pattern.compile(regEx).matcher(email).matches();
    }

    public static boolean isLoginPasswordValid(String password) {

        return password.length() > 6;
    }

    public static boolean isRegisterPasswordValid(String password) {

        String regEx = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";
        return Pattern.compile(regEx).matcher(password).matches();
    }
}
