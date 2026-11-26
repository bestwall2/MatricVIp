package com.matricvip;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Launcher.
 *
 * <p>The old Facebook-post VIP gate (MCVIP...ENDVIP) and its WhatsApp dialog
 * have been removed. Channel access is enforced server-side by
 * {@link SubscriptionClient} on the Channels tab. This activity only shows the
 * splash briefly and opens {@link HomeActivity}.</p>
 */
public class MainActivity extends AppCompatActivity {

    private final Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getWindow().setNavigationBarColor(0xFF1D5B79);
        getWindow().setStatusBarColor(0xFF1D5B79);

        SharedPreferences userData =
                getSharedPreferences(Config.PREFS, Activity.MODE_PRIVATE);
        userData.edit()
                .putString("ANDROID_ID",
                        Secure.getString(getContentResolver(), Secure.ANDROID_ID))
                .apply();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        Animatoo.animateFade(MainActivity.this);
                        finish();
                    }
                });
            }
        }, 1200);
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Deprecated
    public void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}
