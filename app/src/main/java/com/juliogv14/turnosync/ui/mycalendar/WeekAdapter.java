package com.juliogv14.turnosync.ui.mycalendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
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
import com.juliogv14.turnosync.data.UserRef;
import com.juliogv14.turnosync.data.viewmodels.MyCalendarVM;
import com.juliogv14.turnosync.databinding.ItemShiftBinding;
import com.juliogv14.turnosync.utils.CalendarUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * La clase WeekAdapter es la clase encargada de proporcionar la vista en forma de cuadricula
 * que representa el calendario de una semana. Se muestran los turnos asignados a los usuarios del grupo
 * para la semana correspondiente.
 * Extiende BaseAdapter
 *
 * @author Julio García
 * @see BaseAdapter
 */
public class WeekAdapter extends BaseAdapter {
    /** Contexto */
    private Context mContext;
    /** ViewModel de MyCalendarFragment con los datos de su vista */
    private MyCalendarVM mCalendarVM;
    /** Medidas del dispositivo */
    private DisplayMetrics mDisplayMetrics;
    /** Listado de usuarios del grupo */
    private ArrayList<UserRef> mGroupUsers;
    /** Mapa de listados de turnos de los usuarios con su identificador como clave */
    private Map<String, ArrayList<Shift>> mUserShiftMap;
    /** Mapa de tipos de turno */
    private Map<String, ShiftType> mShiftsTypesMap;
    /** Fecha del primer dia de la semana */
    private DateTime mWeekDate;

    /** Lista de los elementos del adaptador */
    private List<String> mItems;
    /** Tamaño de las casillas del encabezado con los numeros */
    private int mTitleHeight;
    /** Vector con los dias de la semana */
    private String[] mDays;
    /** Fecha del dia de la semana a mostrar */
    private DateTime mDisplayDay;
    /** Iterador para recorrer los usuarios del grupo */
    private Iterator<UserRef> mUsersIterator;
    /** Identificador del usuario correspondiente a la fila a dibujar */
    private String mCurrentUid;
    /** Indice para recorrer la lista de turnos */
    private int mCurrentShiftIndex;

    /**
     * Constructor del adaptador
     * @param c Contexto
     * @param calendarVM ViewModel de MyCalendarFragment
     * @param metrics Medidas del dispositivo
     * @param weekDate Fecha del primer dia de la semana
     * @param groupUsers Listado de usuarios del grupo
     * @param userShifts Mapa de listados de turnos de los usuarios
     * @param shiftTypes Mapa de tipos de turno
     */
    WeekAdapter(Context c, MyCalendarVM calendarVM, DisplayMetrics metrics, DateTime weekDate, ArrayList<UserRef> groupUsers, Map<String, ArrayList<Shift>> userShifts, Map<String, ShiftType> shiftTypes) {
        mContext = c;
        mCalendarVM = calendarVM;
        mDisplayMetrics = metrics;
        mGroupUsers = groupUsers;
        mUserShiftMap = userShifts;
        mShiftsTypesMap = shiftTypes;

        mWeekDate = weekDate;
        mDisplayDay = new DateTime(mWeekDate);
        mDays = mContext.getResources().getStringArray(R.array.calendar_days_of_week);

        populateMonth();
    }

    /**
     * Calcula los dias que se deben mostrar en el calendario
     */
    private void populateMonth() {

        /* Label items */
        mItems = new ArrayList<>();
        mItems.add(""); //First item

        for (int i = 0; i < mDays.length; i++) {
            String day = mDisplayDay.plusDays(i).getDayOfMonth() + " " + mDays[i];
            mItems.add(day);
        }

        for (UserRef userRef : mGroupUsers) {
            //User name item
            mItems.add(userRef.getShortName());

            //Week days
            for (int i = 0; i < DateTimeConstants.DAYS_PER_WEEK; i++) {
                mItems.add("");
            }

        }

        mUsersIterator = mGroupUsers.iterator();
        mTitleHeight = CalendarUtils.getLabelHeight(mDisplayMetrics);
    }


    /**
     * Metodo en el que se construye la vista del elemento a dibujar.
     * Se infla un elemento de la vista bajo la vista padre
     * Se crea la referencia de la vista mediante databinding.
     * Se rellenan los datos de la vista según el tipo de casilla, Encabezado o dia de la semana.
     *
     * @param position Posición del elemento
     * @param convertView Vista del elemento
     * @param parent Vista padre
     * @return Vista del elemento a dibujar
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);

        int dayCellHeight = CalendarUtils.getDayCellHeight(mDisplayMetrics);

        switch (itemType) {
            case 0:             //names
                TextView names;
                if (convertView == null) {
                    names = new TextView(mContext);
                    names.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    names.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));

                } else {
                    names = (TextView) convertView;
                }

                if (mUsersIterator.hasNext()) {
                    UserRef entry = mUsersIterator.next();
                    names.setText(mItems.get(position));
                    mCurrentUid = entry.getUid();

                }
                if (!mUsersIterator.hasNext()){
                    mUsersIterator = mGroupUsers.iterator();
                }

                mDisplayDay = new DateTime(mWeekDate);
                mCurrentShiftIndex = 0;
                names.setTextColor(Color.WHITE);
                int color = ColorUtils.blendARGB(ContextCompat.getColor(mContext, R.color.colorAccent), Color.WHITE, 0.4f);
                names.setBackgroundColor(color);
                convertView = names;
                return convertView;
            case 1:             //Header with days
                if (convertView == null) {
                    TextView days = new TextView(mContext);
                    days.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    days.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, mTitleHeight));
                    days.setText(mItems.get(position));
                    days.setTextColor(Color.WHITE);
                    days.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorAccent));
                    convertView = days;
                }
                return convertView;
            case 2:             //Shifts
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.item_shift, parent, false);
                }
                ItemShiftBinding mItemShiftBinding = DataBindingUtil.bind(convertView);
                mItemShiftBinding.textViewShiftType.setText("");
                mItemShiftBinding.imageViewChange.setImageDrawable(null);

                //Background
                GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(mContext, R.drawable.bg_shift).mutate();
                background.setColor(Color.WHITE);
                convertView.setBackground(background);

                convertView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, dayCellHeight));

                if (mCurrentShiftIndex < mUserShiftMap.get(mCurrentUid).size() && !mShiftsTypesMap.isEmpty()) {
                    Shift shift = mUserShiftMap.get(mCurrentUid).get(mCurrentShiftIndex);
                    DateTime shiftDate = new DateTime(shift.getDate());

                    //Shfit in date
                    if (mDisplayDay.getDayOfMonth() == shiftDate.getDayOfMonth() && mDisplayDay.getMonthOfYear() == shiftDate.getMonthOfYear()) {
                        mCurrentShiftIndex++;
                        mItemShiftBinding.textViewDayMonth.setVisibility(View.GONE);
                        ShiftType type = mShiftsTypesMap.get(shift.getType());
                        mItemShiftBinding.textViewShiftType.setText(type.getTag());

                        //Check if it is selected
                        boolean editMode = mCalendarVM.getEditMode().getValue();
                        if(!editMode) {
                            VectorDrawable sync = (VectorDrawable) ContextCompat.getDrawable(mContext, R.drawable.ic_sync_black_24dp).mutate();
                            if (shift == mCalendarVM.getOwnShift().getValue()) {
                                convertView.setPadding(0, 0, 0, 0);
                                background.setAlpha(180);
                                sync.setColorFilter(ContextCompat.getColor(mContext, R.color.selected_ownShift), PorterDuff.Mode.SRC_ATOP);
                                sync.setAlpha(180);
                                mItemShiftBinding.imageViewChange.setImageDrawable(sync);
                            } else if (shift == mCalendarVM.getOtherShift().getValue()) {
                                sync.setColorFilter(ContextCompat.getColor(mContext, R.color.selected_otherShift), PorterDuff.Mode.SRC_ATOP);
                                mItemShiftBinding.imageViewChange.setImageDrawable(sync);
                            }
                        }
                        background.setColor(type.getColor());
                        convertView.setBackground(background);
                    }
                }


                mDisplayDay = mDisplayDay.plusDays(1);
                return convertView;
            default:
                TextView error = new TextView(mContext);
                error.setBackgroundColor(Color.RED);
                convertView = error;
                return convertView;
        }
    }

    /**
     * Permite identificar el tipo de vista correspondiente a la posición
     * @param position Posición a comprobar
     * @return Identificador del tipo de vista
     */
    @Override
    public int getItemViewType(int position) {

        if (position % 8 == 0 && position != 0) {    //names and first item
            return 0;
        } else if (position < 8) {   //Header with days
            return 1;
        } else {                    //Shifts
            return 2;
        }
    }

    /**
     * Devuelve el numero total de tipos de vistas distintas.
     * @return Numero total de tipos de vistas distintas
     */
    @Override
    public int getViewTypeCount() {
        return 3;
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

    /**
     * Devuelve el numero total de elementos
     * @return Tamaño total de casillas de la cuadricula
     */
    @Override
    public int getCount() {
        return mItems.size();
    }

}
