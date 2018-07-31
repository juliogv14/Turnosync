package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.PageMonthBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * La clase MonthPageFragment es el fragmento que representa una pagina del view pager conteniendo un mes del calendario
 *
 * Extiende Fragment.
 *
 * @author Julio García
 * @see Fragment
 */
public class MonthPageFragment extends Fragment {

    /** Tag de clase */
    private final String TAG = this.getClass().getSimpleName();

    //{@
    /** Claves para guardar los parametros en el Bundle asociado a la instancia */
    private static final String CURRENT_MONTH_DATE_KEY = "currentMonthDate";
    private static final String MONTH_SHIFT_LIST_KEY = "shiftList";
    private static final String MONTH_SHIFT_TYPES_MAP_KEY = "shiftTypesMap";
    //@}

    /** Referencia a la vista con databinding */
    protected PageMonthBinding mViewBinding;

    /** Contexto del fragmento */
    private Context mContext;

    /** Referencia al adaptador del Gridview */
    private BaseAdapter mGridAdapter;

    /** Fecha del primer dia del mes */
    private DateTime mMonthDate;
    /** Lista de turnos del usuario */
    private ArrayList<Shift> mShiftList;
    /** Mapa con los tipos de turnos */
    private Map<String, ShiftType> mShiftTypesMap;


    /** Tiempo acumulado total de los turnos del mes */
    private Period mMonthHours;


    /**
     * Metodo estático para crear instancias de la clase y pasar argumentos. Necesaria para permitir
     * la recreación por parte del sistema y no perder los argumentos
     *
     * @param monthDate Fecha del primer dia del mes
     * @param shiftList Lista de turnos del usuario
     * @param shiftTypes Mapa con los tipos de turnos
     * @return instancia de la clase HomeFragment
     */
    public static MonthPageFragment newInstance(DateTime monthDate, ArrayList<Shift> shiftList, HashMap<String, ShiftType> shiftTypes) {
        MonthPageFragment f = new MonthPageFragment();

        Bundle args = new Bundle();
        args.putLong(CURRENT_MONTH_DATE_KEY, monthDate.getMillis());
        args.putParcelableArrayList(MONTH_SHIFT_LIST_KEY, shiftList);
        args.putSerializable(MONTH_SHIFT_TYPES_MAP_KEY, shiftTypes);
        f.setArguments(args);
        return f;
    }

    /**
     * {@inheritDoc} <br>
     * Al vincularse al contexto se obtienen referencias al contexto.
     * @see Context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se marca el indicador para mantener el estado al recrearse.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Recupera los argumentos pasados en {@link #newInstance}
     */
    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = PageMonthBinding.inflate(inflater, container, false);
        Bundle args = getArguments();
        if (args != null) {
            mMonthDate = new DateTime(args.getLong(CURRENT_MONTH_DATE_KEY));
            mShiftList = args.getParcelableArrayList(MONTH_SHIFT_LIST_KEY);
            mShiftTypesMap = (HashMap<String, ShiftType>)args.getSerializable(MONTH_SHIFT_TYPES_MAP_KEY);
        }
        return mViewBinding.getRoot();
    }


    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Se inicializa la vista y las variables. Se crea el adaptador del grid view que crea el calendario.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM");
        mViewBinding.textViewMonth.setText(fmt.print(mMonthDate));
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mGridAdapter = new MonthAdapter(mContext, mMonthDate.toDate(), metrics, mShiftList, mShiftTypesMap);
        ViewGroup.LayoutParams params = mViewBinding.gridViewCalendar.getLayoutParams();
        params.height = (CalendarUtils.getDayCellHeight(metrics) * (mGridAdapter.getCount()/7));
        mViewBinding.gridViewCalendar.setLayoutParams(params);
        mViewBinding.gridViewCalendar.setAdapter(mGridAdapter);
    }

    /**
     * {@inheritDoc} <br>
     * Callback del ciclo de vida.
     * Al desvincularse de la actividad se ponen a null las referencias
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    /**
     * Permite notificar al adaptador que recargue la vista mediante la referencia al fragmento.
     */
    public void notifyGridDataSetChanged() {
        if (mContext != null) {
            ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGridAdapter.notifyDataSetChanged();
                    if(!mShiftTypesMap.isEmpty()){
                        calculateMonthHours();
                        PeriodFormatter formatter = new PeriodFormatterBuilder()
                                .appendHours()
                                .appendSuffix("h ")
                                .appendMinutes()
                                .appendSuffix("min")
                                .toFormatter();
                        String totalHours = getString(R.string.calendar_total_hours) + " " +formatter.print(mMonthHours);
                        mViewBinding.textViewTotalHours.setText(totalHours);
                    }
                }
            });
        }
    }


    /**
     * Recalcula la suma total de horas de los turnos del mes del usuario
     */
    private void calculateMonthHours (){
        mMonthHours = new Period();
        DateTime firstDay = mMonthDate.toDateTime().withDayOfMonth(1).minusDays(1);
        DateTime lastDay = mMonthDate.toDateTime().plusMonths(1).withDayOfMonth(1);
        for (Shift shift : mShiftList) {
            if(shift.getDate().after(firstDay.toDate()) && shift.getDate().before(lastDay.toDate())){
                ShiftType type = mShiftTypesMap.get(shift.getType());
                mMonthHours = mMonthHours.plus(type.getJodaPeriod());
            }
        }

    }
}
