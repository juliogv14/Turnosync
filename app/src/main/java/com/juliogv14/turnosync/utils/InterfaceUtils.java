package com.juliogv14.turnosync.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * La clase InterfaceUtils es una clase utils que contiene metodos para usados en la vista.
 * @author Julio García
 */
public class InterfaceUtils {
    /** Transforma la entrada en dip a pixeles segun las medidas del dispositivo
     *
     * @param c  Contexto
     * @param dp Numero de dips
     * @return Valor en pixeles
     */
    public static int dpToPx(Context c, float dp){
        DisplayMetrics dm = c.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
    }
}
