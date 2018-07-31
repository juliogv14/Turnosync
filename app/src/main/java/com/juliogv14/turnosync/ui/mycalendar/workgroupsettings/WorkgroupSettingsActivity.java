package com.juliogv14.turnosync.ui.mycalendar.workgroupsettings;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.UserWorkgroup;
import com.juliogv14.turnosync.databinding.ActivityWorkgroupSettingsBinding;

import java.util.concurrent.atomic.AtomicLong;

/**
 * La clase WorkgroupSettingsActivity es una actividad encargada de manejar la vista de la configuración
 * Contiene el fragmento WorkgroupSettingsFragment donde se muestran los usuarios del grupo siendo posible
 * añadir, editar el nombre a mostrar y eliminar usuarios.
 * Ademas se puede configurar el numero máximo de horas semanales. Contiene la navegación a ShiftTypesFragment.
 * Contiene el fragmento ShiftTypesFragment que maneja la creación edición y eliminación de tipos de turno.
 * Extiende AppCompatActivity
 * Implementa la interfaz de comunicación de WorkgroupSettingsFragment
 *
 * @author Julio García
 * @see AppCompatActivity
 * @see WorkgroupSettingsFragment.WorkgroupSettingsListener
 * @see WorkgroupSettingsFragment
 * @see ShiftTypesFragment
 */
public class WorkgroupSettingsActivity extends AppCompatActivity implements WorkgroupSettingsFragment.WorkgroupSettingsListener {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    /** Referencia a la vista con databinding */
    ActivityWorkgroupSettingsBinding mViewBinding;

    /** Referencia al Grupo */
    private UserWorkgroup mWorkgroup;

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al crearse se inicializa la vista. Se muestra el fragmentoo
     */
    // TODO: 31/07/2018 Mantener fragmento
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workgroup_settings);

        //Init
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_workgroup_settings);
        mWorkgroup = getIntent().getParcelableExtra(getString(R.string.data_int_workgroup));
        AtomicLong weeklyHours = (AtomicLong) getIntent().getSerializableExtra(getString(R.string.data_int_hours));
        //ArrayList<UserRef> userlist = getIntent().getParcelableArrayListExtra(getString(R.string.data_int_users));
        //mSettingsFragment = WorkgroupSettingsFragment.newInstance(mWorkgroup, userlist);
        Fragment fragment = WorkgroupSettingsFragment.newInstance(mWorkgroup, weeklyHours);
        displaySelectedScreen(fragment);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Responde cuando se selecciona un elemento del menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Muestra el fragmento pasado por argumento. Llamado en {@link #swapFragment}
     * @param fragment Fragmento que sustituye al actual.
     */
    private void displaySelectedScreen(Fragment fragment) {

        //replacing the fragment
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            if(fragment instanceof WorkgroupSettingsFragment){
                fm.popBackStack("ROOT", 0);
                fm.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .commit();
            } else if(fragment instanceof ShiftTypesFragment){
                fm.beginTransaction()
                        .replace(R.id.content_frame, fragment)
                        .addToBackStack("shifttypes")
                        .commit();
            }

        }
    }

    /**
     * Implementación de la interfaz de comunicación con fragmentos
     * Cambia el titulo de la barra de aplicación
     * @param fragmentId Identificador del fragmento
     */
    @Override
    public void onFragmentSwapped(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_workgroupSettings:
                getSupportActionBar().setTitle(R.string.fragment_workgroupSettings);
                break;
            case R.string.fragment_shiftTypes:
                getSupportActionBar().setTitle(R.string.fragment_shiftTypes);
                break;
        }
    }

    /**
     * Implementación de la interfaz de comunicación de WorkgroupSettingsFragment
     * Responde a la navegación hacia ShiftTypesFragment
     * @param fragmentId Identificador del fragmento.
     */
    @Override
    public void swapFragment(int fragmentId) {
        switch (fragmentId) {
            case R.string.fragment_shiftTypes:
                Fragment fragment = ShiftTypesFragment.newInstance(mWorkgroup);
                displaySelectedScreen(fragment);
                break;
        }
    }
}
