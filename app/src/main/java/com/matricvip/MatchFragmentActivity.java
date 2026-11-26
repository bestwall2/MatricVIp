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
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.blogspot.atifsoftwares.animatoolib.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.*;
import org.json.*;
import android.content.pm.PackageManager;
import java.io.IOException;

public class MatchFragmentActivity extends Fragment {
	
	private Timer _timer = new Timer();
	
	private String respo = "";
	private String aid = "";
	private HashMap<String, Object> header = new HashMap<>();
	private String str = "";
	CountdownTimer countdownTimer;
	private String date_en = "";
	private String mytime = "";
	
	private ArrayList<HashMap<String, Object>> match_list = new ArrayList<>();
	private ArrayList<String> liststring = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> dailoglists = new ArrayList<>();
	
	private SwipeRefreshLayout swiperefreshlayout1;
	private RecyclerView recyclerview1;
	
	private AlertDialog cus;
	private RequestNetwork bader_match;
	private RequestNetwork.RequestListener _bader_match_request_listener;
	private Intent play = new Intent();
	private TimerTask t;
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container, @Nullable Bundle _savedInstanceState) {
		View _view = _inflater.inflate(R.layout.match_fragment, _container, false);
		initialize(_savedInstanceState, _view);
		initializeLogic();
		return _view;
	}
	
	private void initialize(Bundle _savedInstanceState, View _view) {
		swiperefreshlayout1 = _view.findViewById(R.id.swiperefreshlayout1);
		recyclerview1 = _view.findViewById(R.id.recyclerview1);
		bader_match = new RequestNetwork((Activity) getContext());
		
		swiperefreshlayout1.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				_request("https://www.facebook.com/61595111861199/posts/122104456683503728/?app=fbl");
			}
		});
		
		_bader_match_request_listener = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String _param1, String _param2, HashMap<String, Object> _param3) {
				final String _tag = _param1;
				final String _response = _param2;
				final HashMap<String, Object> _responseHeaders = _param3;
				try{
					recyclerview1.setVisibility(View.VISIBLE);
					swiperefreshlayout1.setRefreshing(false);
					match_list = new Gson().fromJson(_json_source(_response), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
					recyclerview1.setAdapter(new Recyclerview1Adapter(match_list));
				}catch(Exception e){
					recyclerview1.setVisibility(View.GONE);
					swiperefreshlayout1.setRefreshing(false);
					SketchwareUtil.showMessage(getContext().getApplicationContext(), "RESPONSE_ERROR");
				}
			}
			
			@Override
			public void onErrorResponse(String _param1, String _param2) {
				final String _tag = _param1;
				final String _message = _param2;
				SketchwareUtil.showMessage(getContext().getApplicationContext(), "RESPONSE_ERROR");
			}
		};
	}
	
	private void initializeLogic() {
		swiperefreshlayout1.setRefreshing(true);
		_request("https://www.facebook.com/61595111861199/posts/122104456683503728/?app=fbl");
	}
	public class ListsAdapter extends BaseAdapter {
		ArrayList<HashMap<String, Object>> _data;
		public ListsAdapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public int getCount() {
			return _data.size();
		}
		
		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}
		
		@Override
		public long getItemId(int _index) {
			return _index;
		}
		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.item_servers, null);
			}
			/*lists*/
			final TextView t1 = (TextView) _view.findViewById(R.id.t1);
			/*lists*/
			final LinearLayout l1 = (LinearLayout) _view.findViewById(R.id.l1);
			t1.setText(dailoglists.get((int)/*lists*/_position).get("name").toString());
			l1.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _view){
					if (dailoglists.get((int)/*lists*/_position).get("url").toString().equals("")) {
						SketchwareUtil.showMessage(getContext().getApplicationContext(), "لا يوجد رابط");
					} else {
						play.setClass(getContext().getApplicationContext(), PlayerActivity.class);
						play.putExtra("url", dailoglists.get((int)/*lists*/_position).get("url").toString());
						startActivity(play);
						cus.dismiss();
					}
				}
			});
			
			return _view;
		}
	}
	
	public void _request(final String _url) {
		header = new HashMap<>();
		header.put("Host", "z-m-www.facebook.com");
		header.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.6167.184 Safari/537.36");
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
		header.put("Sec-Fetch-Site", "none");
		header.put("Accept-Language", "ar-MA,ar;q=0.9,en-MA;q=0.8,en-US;q=0.7,en;q=0.6");
		bader_match.setHeaders(header);
		bader_match.startRequestNetwork(RequestNetworkController.GET, _url, "", _bader_match_request_listener);
	}
	
	
	public String _json_source(final String _str) {
		respo = _str;
		aid = respo.substring((int)(0), (int)(respo.indexOf("\"text\":\"MCVIP")));
		respo = respo.replace(aid, "");
		respo = respo.substring((int)(0), (int)(respo.length()));
		respo = respo.substring((int)(0), (int)(respo.indexOf("ENDVIP\"}")));
		respo = decrypted963(respo.replace("\"text\":\"MCVIP", "").replace("", ""),"♕");
		return new String(respo);
	}
	
	
	public void _lib_enc() {
	}
	private javax.crypto.SecretKey generateKey(String pwd) throws Exception {
		
		final java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
		
		byte[] b = pwd.getBytes(StandardCharsets.UTF_8);
		
		digest.update(b,0,b.length);
		
		byte[] key = digest.digest();
		
		javax.crypto.spec.SecretKeySpec sec = new javax.crypto.spec.SecretKeySpec(key, "AES");
		
		return sec;
		
	}
	
	public String encrypted963(String s,String _key) {
		try {
			
			javax.crypto.SecretKey key = generateKey(_key);
			
			javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
			
			c.init(javax.crypto.Cipher.ENCRYPT_MODE, key);
			
			byte[] encVal = c.doFinal(s.getBytes(StandardCharsets.UTF_8));
			
			return android.util.Base64.encodeToString(encVal,android.util.Base64.DEFAULT);
			
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	
	
	public String decrypted963(String s, String _key) {
		
		try {
			
			javax.crypto.spec.SecretKeySpec key = (javax.crypto.spec.SecretKeySpec) generateKey(_key);
			
			javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
			
			c.init(javax.crypto.Cipher.DECRYPT_MODE,key);
			
			byte[] decode = android.util.Base64.decode(s,android.util.Base64.DEFAULT);
			
			byte[] decval = c.doFinal(decode);
			
			return new String(decval, StandardCharsets.UTF_8);
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		
	}
	{
	}
	
	
	public String _Time(final String _st, final double _nu) {
		liststring = new ArrayList<String>(Arrays.asList(_st.split(":")));
		if (liststring.size() == 3) {
			str = liststring.get((int)(_nu));
		} else {
			str = "ffffffff";
		}
		return str ;
	}
	
	
	public void _mynewadpter() {
		cus = new AlertDialog.Builder(getActivity()).create();
		LayoutInflater cusLI = getActivity().getLayoutInflater();
		View cusCV = (View) cusLI.inflate(R.layout.select_server, null);
		cus.setView(cusCV);
		final ListView lists = (ListView)
		cusCV.findViewById(R.id.lists);
		final LinearLayout bg = (LinearLayout)
		cusCV.findViewById(R.id.bg);
		lists.setAdapter(new ListsAdapter(dailoglists));
		((BaseAdapter)lists.getAdapter()).notifyDataSetChanged();
		cus.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		cus.setCancelable(true);
		cus.show();
	}
	
	
	public void _test() {
	}
	
	public String getCountdownTo(String targetTime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
		
		try {
			Date targetDate = format.parse(targetTime);
			if (targetDate == null) return "Invalid date";
			
			long currentMillis = System.currentTimeMillis();
			long targetMillis = targetDate.getTime();
			long diff = targetMillis - currentMillis;
			
			if (diff <= 0) return "جارية الأن";
			
			long seconds = diff / 1000;
			long hours = seconds / 3600;
			long minutes = (seconds % 3600) / 60;
			long sec = seconds % 60;
			
			return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, sec);
			
		} catch (Exception e) {
			return "Parse error";
		}
		
	}
	
	
	public void _Add(final String _Colour, final ImageView _Imageview) {
		_Imageview.getDrawable().setColorFilter(Color.parseColor(_Colour), PorterDuff.Mode.SRC_IN);
	}
	
	public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {
		
		ArrayList<HashMap<String, Object>> _data;
		
		public Recyclerview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}
		
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater _inflater = getActivity().getLayoutInflater();
			View _v = _inflater.inflate(R.layout.match_view, null);
			RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			_v.setLayoutParams(_lp);
			return new ViewHolder(_v);
		}
		
		@Override
		public void onBindViewHolder(ViewHolder _holder, final int _position) {
			View _view = _holder.itemView;
			
			final LinearLayout main = _view.findViewById(R.id.main);
			final LinearLayout lin_main = _view.findViewById(R.id.lin_main);
			final LinearLayout lin = _view.findViewById(R.id.lin);
			final LinearLayout lin_color = _view.findViewById(R.id.lin_color);
			final LinearLayout lin_dawri_channel = _view.findViewById(R.id.lin_dawri_channel);
			final LinearLayout lin_1 = _view.findViewById(R.id.lin_1);
			final LinearLayout lin_2 = _view.findViewById(R.id.lin_2);
			final LinearLayout lin_3 = _view.findViewById(R.id.lin_3);
			final ImageView img2 = _view.findViewById(R.id.img2);
			final TextView name2 = _view.findViewById(R.id.name2);
			final LinearLayout posi = _view.findViewById(R.id.posi);
			final TextView time = _view.findViewById(R.id.time);
			final TextView status = _view.findViewById(R.id.status);
			final ImageView img1 = _view.findViewById(R.id.img1);
			final TextView name1 = _view.findViewById(R.id.name1);
			final LinearLayout lin_channel = _view.findViewById(R.id.lin_channel);
			final LinearLayout linear1 = _view.findViewById(R.id.linear1);
			final LinearLayout lin_dawri = _view.findViewById(R.id.lin_dawri);
			final TextView channel = _view.findViewById(R.id.channel);
			final ImageView icc_channel = _view.findViewById(R.id.icc_channel);
			final TextView dawri = _view.findViewById(R.id.dawri);
			final ImageView icc_dawri = _view.findViewById(R.id.icc_dawri);
			
			_Add("#ffffff", icc_channel);
			_Add("#ffffff", icc_dawri);
			name1.setText(_data.get((int)_position).get("name1").toString());
			name2.setText(_data.get((int)_position).get("name2").toString());
			if (_data.get((int)_position).get("img1").toString().equals("")) {
				img1.setImageResource(R.drawable.mrstr);
			} else {
				Picasso.get()
				.load(_data.get((int)_position).get("img1").toString())
				.placeholder(R.drawable.mrstr)
				.error(R.drawable.mrstr)
				.into(img1);
			}
			if (_data.get((int)_position).get("img2").toString().equals("")) {
				img2.setImageResource(R.drawable.mrstr);
			} else {
				Picasso.get()
				.load(_data.get((int)_position).get("img2").toString())
				.placeholder(R.drawable.mrstr)
				.error(R.drawable.mrstr)
				.into(img2);
			}
			time.setText(_data.get((int)_position).get("time").toString().substring(_data.get((int)_position).get("time").toString().indexOf(" ")));
			status.setText(_data.get((int)_position).get("status").toString());
			dawri.setText(_data.get((int)_position).get("dawri").toString());
			channel.setText(_data.get((int)_position).get("channel").toString());
			final String
			mytime = _data.get((int)_position).get("time").toString();
			final TextView text = status;
			
			if (_view.getTag() != null && _view.getTag() instanceof TimerTask) {
				((TimerTask) _view.getTag()).cancel();
			}
			
			
			TimerTask d = new TimerTask() {
				@Override
				public void run() {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if(getCountdownTo(mytime).contains("جارية الأن")){
								if (_data.get((int)_position).get("status").toString().equals("انتهت")) {
									status.setText("انتهت");
								} else {
									status.setText("جارية الأن");
									lin_main.setOnClickListener(new View.OnClickListener(){
										@Override
										public void onClick(View _view){
											if (status.getText().toString().equals("لم تبدأ")) {
												SketchwareUtil.showMessage(getContext().getApplicationContext(), "لم تبدأ المباراة بعد");
											} else {
												if (status.getText().toString().equals("جارية الأن")) {
													dailoglists = new Gson().fromJson(match_list.get((int)_position).get("servers").toString(), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
													if (dailoglists.size() > 0) {
														_mynewadpter();
													} else {
														SketchwareUtil.showMessage(getContext().getApplicationContext(), "لا يوجد رابط للبث حاليا");
													}
												} else {
													if (status.getText().toString().equals("انتهت")) {
														SketchwareUtil.showMessage(getContext().getApplicationContext(), "انتهت المباراة");
													} else {
														dailoglists = new Gson().fromJson(match_list.get((int)_position).get("servers").toString(), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
														if (dailoglists.size() > 0) {
															_mynewadpter();
														} else {
															SketchwareUtil.showMessage(getContext().getApplicationContext(), "لا يوجد رابط للبث حاليا");
														}
													}
												}
											}
										}
									});
								}
							}else{
								text.setText(getCountdownTo(mytime));
							}
						}
					});
				}
			};
			
			
			_view.setTag(d);
			
			
			_timer.scheduleAtFixedRate(d, 0, 1000);
			lin_main.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View _view){
					if (_data.get((int)_position).get("status").toString().equals("لم تبدأ")) {
						SketchwareUtil.showMessage(getContext().getApplicationContext(), "لم تبدأ المباراة بعد");
					} else {
						if (_data.get((int)_position).get("status").toString().equals("جارية الأن")) {
							dailoglists = new Gson().fromJson(match_list.get((int)_position).get("servers").toString(), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
							if (dailoglists.size() > 0) {
								_mynewadpter();
							} else {
								SketchwareUtil.showMessage(getContext().getApplicationContext(), "لا يوجد رابط للبث حاليا");
							}
						} else {
							if (_data.get((int)_position).get("status").toString().equals("انتهت")) {
								SketchwareUtil.showMessage(getContext().getApplicationContext(), "انتهت المباراة");
							} else {
								dailoglists = new Gson().fromJson(match_list.get((int)_position).get("servers").toString(), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
								if (dailoglists.size() > 0) {
									_mynewadpter();
								} else {
									SketchwareUtil.showMessage(getContext().getApplicationContext(), "لا يوجد رابط للبث حاليا");
								}
							}
						}
					}
				}
			});
			android.graphics.drawable.GradientDrawable CTVSUB= new android.graphics.drawable.GradientDrawable();
			CTVSUB.setCornerRadius(8);
			CTVSUB.setColor(Color.parseColor("#FF468B97")); 
			img1.setBackground(CTVSUB);
			img1.setClipToOutline(true);
			img2.setBackground(CTVSUB);
			img2.setClipToOutline(true);
		}
		
		@Override
		public int getItemCount() {
			return _data.size();
		}
		
		public class ViewHolder extends RecyclerView.ViewHolder {
			public ViewHolder(View v) {
				super(v);
			}
		}
	}
}