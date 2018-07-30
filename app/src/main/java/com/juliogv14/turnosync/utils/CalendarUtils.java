package com.juliogv14.turnosync.utils;

import android.util.DisplayMetrics;

import org.joda.time.DateTime;

/**
 * La clase CalendarUtils es una clase utils que contiene metodos para usados en el tratamiento
 * del calendario.
 *
 * @author Julio García
 */
public class CalendarUtils {

    /** Obtiene el valor de alto de la casilla de cabecera del calendario dependiendo de las medidas
     * del dispositivo
     * @param displayMetrics Medidas del dispositivo
     * @return Altura de la casilla
     */
    public static int getLabelHeight(DisplayMetrics displayMetrics) {
        int densityDpi = displayMetrics.densityDpi;
        if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 40;
        } else if (densityDpi > DisplayMetrics.DENSITY_MEDIUM && densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 52;
        } else if (densityDpi > DisplayMetrics.DENSITY_HIGH && densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 60;
        } else if (densityDpi > DisplayMetrics.DENSITY_XHIGH && densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            return 70;
        } else if (densityDpi > DisplayMetrics.DENSITY_XXHIGH && densityDpi < DisplayMetrics.DENSITY_XXXHIGH) {
            return 80;
        } else {
            return 54;
        }
    }

    /** Obtiene el valor de alto de la casilla de un turno dependiendo de las medidas
     * del dispositivo
     * @param displayMetrics Medidas del dispositivo
     * @return Altura de la casilla
     */
    public static int getDayCellHeight(DisplayMetrics displayMetrics) {
        int densityDpi = displayMetrics.densityDpi;
        if (densityDpi > DisplayMetrics.DENSITY_LOW && densityDpi < DisplayMetrics.DENSITY_MEDIUM) {
            return 140;
        } else if (densityDpi > DisplayMetrics.DENSITY_MEDIUM && densityDpi < DisplayMetrics.DENSITY_HIGH) {
            return 140;
        } else if (densityDpi > DisplayMetrics.DENSITY_HIGH && densityDpi < DisplayMetrics.DENSITY_XHIGH) {
            return 140;
        } else if (densityDpi > DisplayMetrics.DENSITY_XHIGH && densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            return 140;
        } else if (densityDpi > DisplayMetrics.DENSITY_XXHIGH && densityDpi < DisplayMetrics.DENSITY_XXXHIGH) {
            return 150;
        } else {
            return 140;
        }
    }

    /** Comprueba si la fecha pasada como argumento coincide con el dia actual con la hora 00:00UTC
     * @param dateTime Dia a comprobar
     * @return True si coindice. False en caso contrario.
     */
    public static boolean isToday(DateTime dateTime) {
        return dateTime.withTimeAtStartOfDay().getMillis()
                == DateTime.now().withTimeAtStartOfDay().getMillis();
    }
}
