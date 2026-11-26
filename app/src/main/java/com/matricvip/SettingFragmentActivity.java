package com.matricvip;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.blogspot.atifsoftwares.animatoolib.*;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;

public class SettingFragmentActivity extends Fragment {
	
	private Timer _timer = new Timer();
	
	private String DATE = "";
	private String FORMAT = "";
	private boolean endPageOpened = false;
	
	private ScrollView vscroll1;
	private LinearLayout linear1;
	private ImageView imageview1;
	private TextView name;
	private LinearLayout linear2;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private LinearLayout linear5;
	private LinearLayout linear_change_user_id;
	private TextView masked_user_id;
	private TextView username;
	private TextView textview2;
	private TextView android_id;
	private TextView textview4;
	private TextView date;
	private TextView textview6;
	private TextView phone;
	private TextView textview8;
	
	private SharedPreferences USER_DATA;
	private TimerTask TimerCountdown;
	private Intent topro = new Intent();
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
		View _view = _inflater.inflate(R.layout.setting_fragment, _container, false);
		initialize(_savedInstanceState, _view);
		initializeLogic();
		return _view;
	}
	
	private void initialize(Bundle _savedInstanceState, View _view) {
		vscroll1 = _view.findViewById(R.id.vscroll1);
		linear1 = _view.findViewById(R.id.linear1);
		imageview1 = _view.findViewById(R.id.imageview1);
		name = _view.findViewById(R.id.name);
		linear2 = _view.findViewById(R.id.linear2);
		linear3 = _view.findViewById(R.id.linear3);
		linear4 = _view.findViewById(R.id.linear4);
		linear5 = _view.findViewById(R.id.linear5);
		linear_change_user_id = _view.findViewById(R.id.linear_change_user_id);
		masked_user_id = _view.findViewById(R.id.masked_user_id);
		username = _view.findViewById(R.id.username);
		textview2 = _view.findViewById(R.id.textview2);
		android_id = _view.findViewById(R.id.android_id);
		textview4 = _view.findViewById(R.id.textview4);
		date = _view.findViewById(R.id.date);
		textview6 = _view.findViewById(R.id.textview6);
		phone = _view.findViewById(R.id.phone);
		textview8 = _view.findViewById(R.id.textview8);
		USER_DATA = getContext().getSharedPreferences(Config.PREFS, Activity.MODE_PRIVATE);
		linear_change_user_id.setOnClickListener(v -> UserIdHelper.showChangeUserIdDialog(requireContext(), true, this::refreshMaskedUserId));
	}
	
	private void initializeLogic() {
		username.setText(USER_DATA.getString("NAME", ""));
		android_id.setText(USER_DATA.getString("ANDROID_ID", ""));
		date.setText(USER_DATA.getString("DATE", ""));
		phone.setText(USER_DATA.getString("PHONE", ""));
		DATE = USER_DATA.getString("DATE", "").concat(" 00:00:00");
		FORMAT = "yyyy/MM/dd HH:mm:ss";
		refreshMaskedUserId();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshMaskedUserId();
		_countdown_start();
	}
	public void _countDownStart() {
		TimerCountdown = new TimerTask() {
			@Override
			public void run() {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT, Locale.getDefault());
							dateFormat.setLenient(false);
							
							Date event_date = dateFormat.parse(DATE);
							long diff = event_date.getTime() - System.currentTimeMillis();
							
							if (diff > 0) {
								
								long Days = diff / 86400000;
								long Hours = (diff / 3600000) % 24;
								long Minutes = (diff / 60000) % 60;
								long Seconds = (diff / 1000) % 60;
								
								date.setText(String.format(
								Locale.getDefault(),
								"%02d يوم %02d ساعة %02d دقيقة %02d ثانية",
								Days, Hours, Minutes, Seconds
								));
								
							} else {
								
								date.setText("انتهى الوقت");
								
								TimerCountdown.cancel();
								_timer.cancel();
								
								SketchwareUtil.showMessage(getContext().getApplicationContext(), "TIME OUT");
							}
							
						} catch (Exception e) {
							TimerCountdown.cancel();
							_timer.cancel();
						}
					}
				});
			}
		};
		
		_timer.scheduleAtFixedRate(TimerCountdown, 0, 1000);
	}
	
	
	public void _countdown_start() {
		if (TimerCountdown != null) {
			TimerCountdown.cancel();
			TimerCountdown = null;
		}
		
		try {
			SimpleDateFormat dateFormat2 = new SimpleDateFormat(FORMAT, Locale.getDefault());
			dateFormat2.setLenient(false);
			
			Date event_date2 = dateFormat2.parse(DATE);
			long currentTime = System.currentTimeMillis();
			
			if (event_date2 != null && currentTime < event_date2.getTime()) {
				
				_countDownStart();
				
			} else {
				
				date.setText("00/00/00 00:00:00");
				
				if (isAdded() && getActivity() != null && !endPageOpened) {
					
					endPageOpened = true;
					
					Intent topro = new Intent(getActivity(), EndvipActivity.class);
					startActivity(topro);
				}
			}
			
		} catch (Exception e) {
			if (TimerCountdown != null) {
				TimerCountdown.cancel();
				TimerCountdown = null;
			}
		}
	}
	

	private void refreshMaskedUserId() {
		if (masked_user_id != null && getContext() != null) {
			masked_user_id.setText(UserIdHelper.mask(UserIdHelper.getUserId(getContext())));
		}
	}
}
