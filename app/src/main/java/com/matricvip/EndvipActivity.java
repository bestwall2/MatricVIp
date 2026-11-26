package com.matricvip;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.blogspot.atifsoftwares.animatoolib.*;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.provider.Settings.Secure;

public class EndvipActivity extends AppCompatActivity {
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	private String bader_id = "";
	
	private LinearLayout linear1;
	private ScrollView vscroll1;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private LinearLayout linear5;
	private ImageView imageview6;
	private TextView textview2;
	private LinearLayout linear6;
	private LinearLayout lin1;
	private TextView str;
	private LinearLayout linear10;
	private TextView textview4;
	private TextView textview5;
	
	private Intent bader = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.endvip);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		linear1 = findViewById(R.id.linear1);
		vscroll1 = findViewById(R.id.vscroll1);
		linear3 = findViewById(R.id.linear3);
		linear4 = findViewById(R.id.linear4);
		linear5 = findViewById(R.id.linear5);
		imageview6 = findViewById(R.id.imageview6);
		textview2 = findViewById(R.id.textview2);
		linear6 = findViewById(R.id.linear6);
		lin1 = findViewById(R.id.lin1);
		str = findViewById(R.id.str);
		linear10 = findViewById(R.id.linear10);
		textview4 = findViewById(R.id.textview4);
		textview5 = findViewById(R.id.textview5);
		
		lin1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				bader.setAction(Intent.ACTION_VIEW);
				bader.setData(Uri.parse("whatsapp://send?text=".concat("مرحبا أريد تجديد الإشتراك في تطبيق Matric Nejma ViP بعرض 20 درهم\nكود التفعيل الخاص بي هو\n".concat(bader_id.concat("&phone=".concat("+212 681-718430"))))));
				startActivity(bader);
			}
		});
	}
	
	private void initializeLogic() {
		bader_id = android.provider.Settings.Secure.getString(getApplication().getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
		getWindow().setNavigationBarColor(0xFF1D5B79);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		Toolbar toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(toolbar);
		
		if (getSupportActionBar() != null)
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		TextView title = new TextView(this);
		title.setText("SUBSCRIBE END");
		title.setTextColor(Color.WHITE);
		title.setTextSize(15);
		title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bader.ttf"));
		
		Toolbar.LayoutParams params = new Toolbar.LayoutParams(
		Toolbar.LayoutParams.WRAP_CONTENT,
		Toolbar.LayoutParams.WRAP_CONTENT,
		Gravity.CENTER
		);
		
		toolbar.addView(title, params);
	}
	
	
	@Override
	public void onBackPressed() {
		finishAffinity();
	}
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}