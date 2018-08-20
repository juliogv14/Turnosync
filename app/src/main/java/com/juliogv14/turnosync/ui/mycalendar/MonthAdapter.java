package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.juliogv14.turnosync.R;
import com.juliogv14.turnosync.data.Shift;
import com.juliogv14.turnosync.data.ShiftType;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * La clase MonthAdapter es la clase encargada de proporcionar la vista en forma de cuadricula
 * que representa el calendario de un mes. Se muestran los turnos asignados al usuario conectado en la aplicación.
 * Extiende BaseAdapter
 *
 * @author Julio García
 * @see BaseAdapter
 */
public class MonthAdapter extends BaseAdapter {
    /** Fecha del primer dia del mes */
    private DateTime mMonthDate;
    /** Contexto */
    private Context mContext;
    /** Medidas del dispositivo */
    private DisplayMetrics mDisplayMetrics;
    /** Lista de los elementos del adaptador */
    private List<String> mItems;
    /** Numero de dias que se muestran del mes anterior */
    private int mDaysLastMonth;
    /** Numero de dias que se muestran del mes posterior */
    private int mDaysNextMonth;
    /** Tamaño de las casillas del encabezado con los numeros */
    private int mTitleHeight;
    /** Vector con los dias de la semana */
    private String[] mDays;

    /** Listado con los turnos asociados al usuario del mes correspondiente */
    private ArrayList<Shift> mShiftsList;
    /** Mapa con los tipos de turno */
    private Map<String, ShiftType> mShiftsTypesMap;
    /** Indice para recorrer el vector de turnos */
    private int mCurrentShiftIndex;


    /**
     * Constructor del adaptador
     * @param c Contexto
     * @param monthDate Fecha del primer dia del mes
     * @param metrics Medidas del dispositivo
     * @param shifts Listado con los turnos asociados al usuario del mes correspondiente
     * @param types Mapa con los tipos de turno
     */
    MonthAdapter(Context c, Date monthDate, DisplayMetrics metrics, ArrayList<Shift> shifts, Map<String, ShiftType> types) {
        mContext = c;
        mMonthDate = new DateTime(monthDate);
        mDisplayMetrics = metrics;
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        mShiftsList = shifts;
        mShiftsTypesMap = types;
        populateMonth();
    }

    /**
     * Calcula los dias que se deben mostrar en el calendario
     */
    private void populateMonth() {

        //Header with day names
        mItems = new ArrayList<>();
        Collections.addAll(mItems, mDays);

        //Days last month
        int firstDayWeek = mMonthDate.getDayOfWeek();
        mDaysLastMonth = firstDayWeek-1;
        int firstDayDisplay = mMonthDate.minusDays(firstDayWeek-1).getDayOfMonth();
        for (int i = 0; i < mDaysLastMonth; i++) {
            mItems.add(String.valueOf(firstDayDisplay+i));
        }

        //Days current month
        int daysThisMonth = mMonthDate.dayOfMonth().getMaximumValue();
        for (int i = 1; i <= daysThisMonth; i++) {
            mItems.add(String.valueOf(i));
        }

        //Days next month
        int lastDayWeek = mMonthDate.withDayOfMonth(mMonthDate.dayOfMonth().getMaximumValue()).getDayOfWeek();
        mDaysNextMonth = 7 - lastDayWeek;

        for (int i = 1; i <= mDaysNextMonth; i++) {
            mItems.add(String.valueOf(i));
        }
        
        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
    }

    /**
     * Devuelve una fecha a partir de una posición del adaptador
     * @param position Posición del adaptador
     * @return Fecha correspondiente
     */
    private DateTime getDate(int position) {
        //monthDate is the first day of the month
        int header = 7;
        int monthDatePos = header + mDaysLastMonth;
        return mMonthDate.plusDays(position-monthDatePos);
    }


    /**
     * Metodo en el que se construye la vista del elemento a dibujar.
     * Se infla un elemento de la vista bajo la vista padre
     * Se crea la referencia de la vista mediante databinding.
     * Se rellenan los datos de la vista según el tipo de casilla, Encabezado o dia del mes.
     *
     * @param position Posición del elemento
     * @param convertView Vista del elemento
     * @param parent Vista padre
     * @return Vista del elemento a dibujar
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //init shift index
        if(position == 0) {
            mCurrentShiftIndex = 0;
        }

        //int dayCellHeight = (parent.getMeasuredHeight() - mTitleHeight) / 6;
        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        //If its a day type view
        int itemType = getItemViewType(position);
        switch (itemType){
            case 0:
                TextView textView = new TextView(mContext);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                textView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));
                textView.setText(mItems.get(position));
                textView.setTextColor(Color.WHITE);
                textView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                return textView;
            case 1:
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
                }
                ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);
                convertView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, dayCellHeight));
                String stringDayInMonth = mItems.get(position);
                mItemShiftBinding.textViewDayMonth.setText(stringDayInMonth);
                mItemShiftBinding.textViewShiftType.setText("");

                //Background
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
                background.setColor(Color.WHITE);
                convertView.setBackground(background);

                //Get date from position
                DateTime date = getDate(position);
                //Day number color
                if (date.getMonthOfYear() != mMonthDate.getMonthOfYear()) {
                    // previous or next month
                    if (mItemShiftBinding.textViewShiftType.getText().equals("")) {
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.GRAY);
                    } else {
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                    }
                } else {
                    // current month
                    if (CalendarUtils.isToday(date)) {
                        //Today
                        mItemShiftBinding.textViewDayMonth.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    } else {
                        //Other day
                        mItemShiftBinding.textViewDayMonth.setTextColor(Color.BLACK);
                    }
                }

                //Displaying shifts
                if (!mShiftsList.isEmpty() && mCurrentShiftIndex < mShiftsList.size() && !mShiftsTypesMap.isEmpty()) {
                    Shift shift = mShiftsList.get(mCurrentShiftIndex);
                    DateTime shiftDate = new DateTime(shift.getDate());
                    if (date.getDayOfMonth() == shiftDate.getDayOfMonth() && date.getMonthOfYear() == shiftDate.getMonthOfYear()) {
                        mCurrentShiftIndex++;
                        ShiftType type = mShiftsTypesMap.get(shift.getType());
                        mItemShiftBinding.textViewShiftType.setText(type.getTag());
                        background.setColor(type.getColor());
                        convertView.setBackground(background);
                    }
                }

                return convertView;
            default:
                TextView error = new TextView(mContext);
                error.setBackgroundColor(Color.RED);
                convertView = error;
                return convertView;
        }
    }

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de casillas de la cuadricula
     */
    @Override
    public int getCount() {
        return mItems.size();
    }

    /**
     * Permite identificar el tipo de vista correspondiente a la posición
     * @param position Posición a comprobar
     * @return Identificador del tipo de vista
     */
    @Override
    public int getItemViewType(int position) {
        if (position < 7) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Devuelve el numero total de tipos de vistas distintas.
     * @return Numero total de tipos de vistas distintas
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * Obtiene el elemento correspondiente a la posición proporcionada.
     * @param position Posición del elemento
     * @return Elemento de la lista en la posición dada.
     */
    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }
}

