package com.blogspot.atifsoftwares.animatoolib;

import android.app.Activity;

/**
 * Small local replacement for the AnimateTool library.
 * The original library is not needed: the built-in Android transitions
 * provide the same effect and keep the APK small.
 */
public class Animatoo {

    private Animatoo() {
    }

    public static void animateFade(Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public static void animateZoom(Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public static void animateSlideLeft(Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public static void animateSlideRight(Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    }

    public static void animateSlideUp(Activity activity) {
        animateFade(activity);
    }

    public static void animateSlideDown(Activity activity) {
        animateFade(activity);
    }
}
