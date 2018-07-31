package com.juliogv14.turnosync;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * La clase TurnosyncApplication extiende la clase Application
 * para realizar configuraciones al ejecutar la aplicacion
 *
 * @author Julio García
 * @see Application
 */
public class TurnosyncApplication extends Application {

    /**
     * {@inheritDoc}
     * Lifecycle callback.
     * Se configura Firebase Cloud Firestore para tener acceso a la cache de la base de datos sin
     * conexión a internet
     */
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
}
