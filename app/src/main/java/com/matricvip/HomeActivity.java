package com.matricvip;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnAdapterChangeListener;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.blogspot.atifsoftwares.animatoolib.*;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class HomeActivity extends AppCompatActivity {
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	
	private TabLayout tablayout1;
	private ViewPager viewpager1;
	
	private F_cuiFragmentAdapter f_cui;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.home);
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
		tablayout1 = findViewById(R.id.tablayout1);
		viewpager1 = findViewById(R.id.viewpager1);
		f_cui = new F_cuiFragmentAdapter(getApplicationContext(), getSupportFragmentManager());
	}
	
	private void initializeLogic() {
		tablayout1.setTabTextColors(0xFFFFFFFF, 0xFFFFFFFF);
		tablayout1.setTabRippleColor(new android.content.res.ColorStateList(new int[][]{new int[]{android.R.attr.state_pressed}}, 
		
		new int[] {0xFFFFFFFF}));
		tablayout1.setSelectedTabIndicatorColor(0xFFFFFFFF);
		tablayout1.setupWithViewPager(viewpager1);
		f_cui.setTabCount(3);
		viewpager1.setAdapter(f_cui);
		viewpager1.setCurrentItem((int)1);
		viewpager1.setOffscreenPageLimit((int)3);
		((PagerAdapter)viewpager1.getAdapter()).notifyDataSetChanged();
		Toolbar toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(toolbar);
		
		if (getSupportActionBar() != null)
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		
		TextView title = new TextView(this);
		title.setText("MATRIC NEJMA VIP");
		title.setTextColor(Color.WHITE);
		title.setTextSize(15);
		title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bader.ttf"));
		
		Toolbar.LayoutParams params = new Toolbar.LayoutParams(
		Toolbar.LayoutParams.WRAP_CONTENT,
		Toolbar.LayoutParams.WRAP_CONTENT,
		Gravity.CENTER
		);
		
		toolbar.addView(title, params);
		getWindow().setNavigationBarColor(0xFF1D5B79);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}
	
	public class F_cuiFragmentAdapter extends FragmentStatePagerAdapter {
		// This class is deprecated, you should migrate to ViewPager2:
		// https://developer.android.com/reference/androidx/viewpager2/widget/ViewPager2
		Context context;
		int tabCount;
		
		public F_cuiFragmentAdapter(Context context, FragmentManager manager) {
			super(manager);
			this.context = context;
		}
		
		public void setTabCount(int tabCount) {
			this.tabCount = tabCount;
		}
		
		@Override
		public int getCount() {
			return tabCount;
		}
		
		@Override
		public CharSequence getPageTitle(int _position) {
			if (_position == 0) {
				return "إعدادات";
			}
			if (_position == 1) {
				return "قنوات";
			}
			if (_position == 2) {
				return "مباريات";
			}
			return "";
		}
		
		
		@Override
		public Fragment getItem(int _position) {
			if (_position == 0) {
				return new SettingFragmentActivity();
			}
			if (_position == 1) {
				return new ChannelFragmentActivity();
			}
			if (_position == 2) {
				return new MatchFragmentActivity();
			}
			return new Fragment();
		}
		
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