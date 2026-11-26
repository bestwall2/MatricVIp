package com.matricvip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer {
	private Timer timer;
	private SimpleDateFormat sdf;
	private long timeUntilTarget;
	private boolean timerRunning = false;
	private CountdownTimerListener listener;
	
	public interface CountdownTimerListener {
		void onTick(long millisUntilFinished);
		
		void onFinish();
	}
	
	public CountdownTimer(int day, int hour, int minute) {
		sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		
	
		Calendar targetTime = Calendar.getInstance();
		targetTime.add(Calendar.DAY_OF_YEAR, day); 
		targetTime.set(Calendar.HOUR_OF_DAY, hour);
		targetTime.set(Calendar.MINUTE, minute);
		targetTime.set(Calendar.SECOND, 0);
		
		timeUntilTarget = targetTime.getTimeInMillis() - System.currentTimeMillis();
	}
	
	public void setCountdownTimerListener(CountdownTimerListener listener) {
		this.listener = listener;
	}
	
	public void start() {
		if (!timerRunning) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					if (timeUntilTarget > 0) {
						if (listener != null) {
							listener.onTick(timeUntilTarget);
						}
						timeUntilTarget -= 1000;
					} else {
						timer.cancel();
						timerRunning = false;
						if (listener != null) {
							listener.onFinish();
						}
					}
				}
			}, 0, 1000);
			timerRunning = true;
		}
	}
	
	public SimpleDateFormat getSdf() {
		return sdf;
	}
}