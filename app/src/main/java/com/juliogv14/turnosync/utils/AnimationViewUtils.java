package com.juliogv14.turnosync.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/**
 * La clase AnimationViewUtils es una clase utils que contiene metodos para animar vistas
 *
 * @author Julio García
 */
public class AnimationViewUtils {

    /** Muestra u oculta la vista pasada por parametro
     *
     * @param indicatorView vista del indicador de carga
     * @param show True para visible, False para retirarla.
     */
    public static void showLoadingIndicator(View indicatorView, boolean show) {
        if (show) {
            animateView(indicatorView, View.VISIBLE, 0.4f, 200);
        } else {
            animateView(indicatorView, View.GONE, 0, 200);
        }
    }

    /** Anima el cambio de visibilidad de una vista
     *
     * @param view Vista que cambia
     * @param toVisibility Visibilidad a la que cambia
     * @param toAlpha Transparencia
     * @param duration Duración de la animación
     */
    //AndroidUtils.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }
}
