package com.juliogv14.turnosync.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.juliogv14.turnosync.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * La clase FormUtils es una clase utils que contiene metodos para usados en la entrada de datos
 * @author Julio García
 */
public class FormUtils {

    /** Comprueba que una dirección de email está bien formada
     * @param email Email a comprobar.
     * @return True si es valido. False en caso contrario.
     */
    public static boolean isEmailValid(String email) {

        //Regex to check if the string contains '@' and '.' as in an email.
        String regEx = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.compile(regEx).matcher(email).matches();
    }

    /** Comprueba que una contraseña es sufientemente segura. Minimo 6 caracteres, un numero y una mayuscula y minuscula.
     *
     * @param password contrasña a comprobar.
     * @return True si es valido. False en caso contrario.
     */
    public static boolean isRegisterPasswordValid(String password) {

        String regEx = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$";
        return Pattern.compile(regEx).matcher(password).matches();
    }

    /** Comprueba que nombre está bien formado evitando caracteres raros. Solo se permite guión y apostrofe.
     * @param displayName Nombre a comprobar.
     * @return True si es valido. False en caso contrario.
     */
    public static boolean isDisplayNameValid(String displayName) {
        String regEx = "^[\\p{L}\\p{N}-' ]+$";
        return Pattern.compile(regEx).matcher(displayName).matches();
    }

    /** Comprueba que las iniciales están bien formadas. Solo se admiten letras y numeros.
     * @param initials Iniciales a comprobar.
     * @return True si es valido. False en caso contrario.
     */
    public static boolean isInitialsValid(String initials){
        String regEx = "^[\\p{L}\\p{N}]+$";
        return Pattern.compile(regEx).matcher(initials).matches();
    }

    /** Oculta el teclado
     *
     * @param context Contexto actual
     * @param focusView Vista con la atención
     * @return True si tuvo exito. False en caso contrario.
     */
    public static boolean closeKeyboard(Context context, View focusView) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focusView != null && imm != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            return true;
        }
        return false;
    }

    /** Muestra el teclado
     *
     * @param context Contexto actual
     * @param focusView Vista con la atención
     * @return True si tuvo exito. False en caso contrario.
     */
    public static boolean openKeyboard(Context context, View focusView) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (focusView != null && imm != null) {
            imm.showSoftInput(focusView, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return false;
    }

    /** Transforma una cadena en slug dejandola sin caracteres especiales de idioma.
     *
     * @param str Cadena a tratar
     * @return Resultado del tratamiento
     */
    public static String slugify (String str) {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("a", "[áàãâÀÁÃÂ]");
        patterns.put("e" , "[éèêÉÈÊ]");
        patterns.put("i" , "[íìîÍÌÎ]");
        patterns.put("o" , "[óòôõÓÒÔÕ]");
        patterns.put("u" , "[úùûüÚÙÛÜ]");
        patterns.put("c" , "[çÇ]");
        patterns.put("n" , "[ñÑ]");

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            str = str.replace(entry.getValue(), entry.getKey());
        }
        return str;
    };

    /** Comprueba el estado de los servicios de Google Play */
    public static void checkGooglePlayServices(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(context);
        String TAG = context.getClass().getSimpleName();

        switch (result) {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Log.w(TAG, "SERVICE_VERSION_UPDATE_REQUIRED");
                Toast.makeText(context, R.string.toast_update_services, Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.SUCCESS:
                Log.d(TAG, "Play service available success");
                break;
            default:
                Log.d(TAG, "unknown services result: " + result);

        }
    }
}
