package com.juliogv14.turnosync.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.juliogv14.turnosync.R;

/**
 * La clase InstanceIDService es un servicio usado para actualizar el token que identifica la aplicacion
 * con Firebase para poder enviar notificaciones
 *
 * @author Julio García
 * @see FirebaseInstanceIdService
 */
public class InstanceIDService extends FirebaseInstanceIdService {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Token: " + token);
        saveToken(token);
    }

    /**
     * Guarda el token en SharedPrefenreces para ser usado en la aplicación
     * @param token Token identificador para enviar notificaciones
     * @see SharedPreferences
     */
    private void saveToken(String token){
        SharedPreferences shrPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = shrPreferences.edit();
        editor.putString(getString(R.string.data_key_token), token);
        editor.apply();
    }
}
