package com.matricvip;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Full-screen "Subscription required" Activity (Arabic UI).
 * Displays masked user ID, Retry button, and Change user ID button.
 */
public class NotSubscribedActivity extends AppCompatActivity {

    public static final String EXTRA_MASKED_ID = "masked_user_id";
    private TextView idView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(56, 56, 56, 56);
        root.setBackgroundColor(Color.parseColor("#1D5B79"));
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("الاشتراك مطلوب");
        title.setTextSize(24);
        title.setTextColor(Color.WHITE);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("هذا الجهاز غير مشترك أو انتهى اشتراكه.\nأرسل معرف الجهاز أدناه للمسؤول لتفعيل الاشتراك.");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.parseColor("#E0E0E0"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 28);
        root.addView(subtitle, matchWrap());

        String masked = UserIdHelper.getUserId(this); // full id so the admin can add it

        idView = new TextView(this);
        idView.setText("معرف الجهاز: " + masked);
        idView.setTextSize(14);
        idView.setTextColor(Color.parseColor("#FFD54F"));
        idView.setGravity(Gravity.CENTER);
        idView.setPadding(0, 0, 0, 36);
        root.addView(idView, matchWrap());

        Button retryBtn = new Button(this);
        retryBtn.setText("إعادة المحاولة");
        retryBtn.setTextColor(Color.WHITE);
        retryBtn.setBackgroundColor(Color.parseColor("#468B97"));
        retryBtn.setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        LinearLayout.LayoutParams retryLp = matchWrap();
        retryLp.bottomMargin = 20;
        root.addView(retryBtn, retryLp);

        Button copyIdBtn = new Button(this);
        copyIdBtn.setText("نسخ معرف الجهاز");
        copyIdBtn.setTextColor(Color.WHITE);
        copyIdBtn.setBackgroundColor(Color.parseColor("#33393D"));
        copyIdBtn.setOnClickListener(v -> UserIdHelper.copyId(this));
        root.addView(copyIdBtn, matchWrap());
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
