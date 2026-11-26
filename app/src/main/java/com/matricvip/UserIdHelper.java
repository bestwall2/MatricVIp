package com.matricvip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Validation, masking, persistence, and dialog helper for user_id.
 */
public final class UserIdHelper {

    public static final int MIN_LEN = 8;
    public static final int MAX_LEN = 128;
    private static final String PATTERN = "^[A-Za-z0-9_-]+$";

    private UserIdHelper() {}

    public static boolean isValid(String userId) {
        if (userId == null) return false;
        int len = userId.length();
        return len >= MIN_LEN && len <= MAX_LEN && userId.matches(PATTERN);
    }

    public static String validateError(String userId) {
        if (userId == null || userId.length() < MIN_LEN) {
            return "المعرف قصير جداً (الحد الأدنى " + MIN_LEN + " أحرف)";
        }
        if (userId.length() > MAX_LEN) {
            return "المعرف طويل جداً (الحد الأقصى " + MAX_LEN + " حرف)";
        }
        if (!userId.matches(PATTERN)) {
            return "الأحرف المسموح بها فقط: A-Z, a-z, 0-9, _, -";
        }
        return null;
    }

    public static String mask(String userId) {
        if (userId == null || userId.length() <= 3) {
            return "***";
        }
        return userId.substring(0, 3) + "***";
    }

    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Config.PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Config.KEY_USER_ID, "").trim();
    }

    public static void saveUserId(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(Config.PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(Config.KEY_USER_ID, userId.trim()).apply();
    }

    public static void showChangeUserIdDialog(final Context context, final boolean cancelable, final Runnable onSaved) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 36, 48, 24);
        root.setBackgroundColor(Color.parseColor("#33393D"));

        TextView title = new TextView(context);
        title.setText("تغيير معرف المستخدم");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        TextView hint = new TextView(context);
        hint.setText("أدخل معرف الاشتراك (8 إلى 128 حرفاً: A-Z, a-z, 0-9, _, -)");
        hint.setTextColor(Color.parseColor("#CCCCCC"));
        hint.setTextSize(13);
        hint.setGravity(Gravity.END);
        hint.setPadding(0, 0, 0, 12);
        root.addView(hint);

        final EditText input = new EditText(context);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.parseColor("#888888"));
        input.setHint("معرف المستخدم");
        input.setBackgroundColor(Color.parseColor("#1D5B79"));
        input.setPadding(24, 20, 24, 20);
        input.setSingleLine(true);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_LEN)});
        String current = getUserId(context);
        if (!current.isEmpty()) {
            input.setText(current);
            input.setSelection(current.length());
        }
        root.addView(input);

        final TextView errorView = new TextView(context);
        errorView.setTextColor(Color.parseColor("#FF8A80"));
        errorView.setTextSize(12);
        errorView.setGravity(Gravity.END);
        errorView.setPadding(0, 8, 0, 8);
        errorView.setVisibility(View.GONE);
        root.addView(errorView);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(root)
                .setCancelable(cancelable)
                .setPositiveButton("حفظ", null)
                .setNegativeButton(cancelable ? "إلغاء" : null, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveBtn.setOnClickListener(v -> {
                String val = input.getText().toString().trim();
                String err = validateError(val);
                if (err != null) {
                    errorView.setText(err);
                    errorView.setVisibility(View.VISIBLE);
                    return;
                }
                saveUserId(context, val);
                Toast.makeText(context, "تم حفظ معرف المستخدم", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                if (onSaved != null) {
                    onSaved.run();
                }
            });
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String err = validateError(s.toString().trim());
                if (err != null) {
                    errorView.setText(err);
                    errorView.setVisibility(View.VISIBLE);
                } else {
                    errorView.setVisibility(View.GONE);
                }
            }
        });

        dialog.show();
    }
}
