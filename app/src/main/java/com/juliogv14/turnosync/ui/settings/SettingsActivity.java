package com.juliogv14.turnosync.ui.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.juliogv14.turnosync.R;

/**
 /**
 * La clase SettingsActivity es responsable de contener el fragmento de la configuración de la
 * aplicación.
 * Extiende AppCompatActivity
 *
 * @author Julio García
 * @see AppCompatActivity
 */
public class SettingsActivity extends AppCompatActivity {

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se establece el boton home para volver a la pantalla anterior
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /** {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
