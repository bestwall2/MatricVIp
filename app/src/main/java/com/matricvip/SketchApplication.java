package com.matricvip;

import android.app.Application;

/**
 * Application entry point.
 * Kept as a separate class (instead of relying on the default Application)
 * so global initialisation can be added later without touching the manifest.
 */
public class SketchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SafeHttpLogger.init(this);
    }
}
