package com.matricvip;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.ui.PlayerView;

/**
 * Full screen video player.
 *
 * <p>Uses Media3 (the current ExoPlayer) and auto-detects the stream type
 * (HLS, DASH, SmoothStreaming or progressive) from the URL, so every server
 * returned by the backend can be played without special handling.</p>
 */
public class PlayerActivity extends AppCompatActivity {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/121.0.6167.184 Mobile Safari/537.36";

    private PlayerView playerView;
    private ProgressBar progressBar;
    private ExoPlayer player;
    private String streamUrl;
    private boolean fullscreen;
    private boolean retried;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progressbar1);

        streamUrl = UrlHolder.getAndClear();
        if (TextUtils.isEmpty(streamUrl)) {
            streamUrl = getIntent().getStringExtra("url");
        }
        if (TextUtils.isEmpty(streamUrl)) {
            Toast.makeText(this, "لا يوجد رابط للبث", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        applyImmersiveMode();
        initializePlayer();
    }

    private void initializePlayer() {
        DefaultHttpDataSource.Factory httpDataSourceFactory =
                new DefaultHttpDataSource.Factory()
                        .setUserAgent(USER_AGENT)
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(15000)
                        .setReadTimeoutMs(25000);

        DefaultDataSource.Factory dataSourceFactory =
                new DefaultDataSource.Factory(this, httpDataSourceFactory);

        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(dataSourceFactory))
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .build();

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        playerView.setFullscreenButtonClickListener(new PlayerView.FullscreenButtonClickListener() {
            @Override
            public void onFullscreenButtonClick(boolean isFullscreen) {
                setFullscreen(isFullscreen);
            }
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (playbackState == Player.STATE_READY
                        || playbackState == Player.STATE_ENDED
                        || playbackState == Player.STATE_IDLE) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                progressBar.setVisibility(View.GONE);
                if (!retried) {
                    retried = true;
                    Toast.makeText(PlayerActivity.this,
                            "تعذر تشغيل البث، جاري إعادة المحاولة...",
                            Toast.LENGTH_SHORT).show();
                    player.prepare();
                    player.setPlayWhenReady(true);
                } else {
                    Toast.makeText(PlayerActivity.this,
                            "تعذر تشغيل هذا الرابط",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        player.setMediaItem(MediaItem.fromUri(Uri.parse(streamUrl)));
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private void setFullscreen(boolean enable) {
        fullscreen = enable;
        if (enable) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        applyImmersiveMode();
    }

    private void applyImmersiveMode() {
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        WindowCompat.setDecorFitsSystemWindows(window, false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        applyImmersiveMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (fullscreen) {
            setFullscreen(false);
            return;
        }
        super.onBackPressed();
    }
}
