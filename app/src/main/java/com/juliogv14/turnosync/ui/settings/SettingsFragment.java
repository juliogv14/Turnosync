package com.juliogv14.turnosync.ui.settings;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.juliogv14.turnosync.R;

/**
 * La clase SettingsFragment es responsable de manejar los cambios de configuración en la aplicación
 * Extiende PreferenceFragmentCompat.
 * Implementa la interfaz de escucha del cuadro de dialogo ResetPasswordDialog.
 *
 * @author Julio García
 * @see PreferenceFragmentCompat
 * @see SharedPreferences
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** {@inheritDoc} <br>
     * Lifecycle callback.
     * Se crea la ventana de configuración.
     * @see PreferenceScreen
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_general);
        PreferenceScreen prefScreen = getPreferenceScreen();

        int prefCount = prefScreen.getPreferenceCount();
        for (int i = 0; i < prefCount; i++) {
            Preference p = prefScreen.getPreference(i);

            setPreferenceInCategory(p);
        }

    }

    /** {@inheritDoc} <br>
     * Lifecycle callback.
     * Escucha a los cambios en la configuración y muestra el valor actual.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

    /** {@inheritDoc} <br>
     * Lifecycle callback.
     * Al iniciar el fragment se vincula la escucha de la configuración
     */
    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /** {@inheritDoc} <br>
     * Lifecycle callback.
     * Al parar el fragment se desvincula la escucha de la configuración
     */
    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /** Muestra los valores de la preferencia según el tipo que sea. Se usa la clave para obtener el valor
     * @param p Preferencia a mostrar
     */
    private void setPreferenceInCategory(Preference p) {

        SharedPreferences shrPreferences =
                getPreferenceScreen().getSharedPreferences();

        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            int catPrefCount = cat.getPreferenceCount();
            for (int j = 0; j < catPrefCount; j++) {
                Preference pref = cat.getPreference(j);
                if (!(pref instanceof CheckBoxPreference)) {

                    String stringValue = shrPreferences.getString(pref.getKey(), "");
                    setPreferenceSummary(pref, stringValue);
                }
            }
        } else {
            if (!(p instanceof CheckBoxPreference)) {

                String stringValue = shrPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, stringValue);
            }
        }
    }

    /** Muestra en el resumen el valor actual de la preferencia.
     * @param preference Preferencia a mostrar el valor.
     * @param value Objeto que se mostrara como cadena en el resumen
     */
    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            //Look for selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // Set summary from the value as a string
            preference.setSummary(stringValue);
        }

    }
}
