package com.matricvip;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChannelFragmentActivity extends Fragment {

    private final ArrayList<Channel> channelList = new ArrayList<>();
    private final AtomicBoolean inFlight = new AtomicBoolean(false);
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());

    private SwipeRefreshLayout swiperefreshlayout1;
    private RecyclerView recyclerview1;
    private Button btnRefresh;
    private Recyclerview1Adapter adapter;

    private boolean subscriptionBlocked = false;

    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (!subscriptionBlocked && isResumed()) {
                fetchChannels();
                refreshHandler.postDelayed(this, Config.REFRESH_S * 1000L);
            }
        }
    };

    private final ActivityResultLauncher<Intent> notSubscribedLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            subscriptionBlocked = false;
                            fetchChannels();
                        }
                    });

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater _inflater, @Nullable ViewGroup _container,
                             @Nullable Bundle _savedInstanceState) {
        View _view = _inflater.inflate(R.layout.channel_fragment, _container, false);
        initialize(_view);
        return _view;
    }

    private void initialize(View _view) {
        swiperefreshlayout1 = _view.findViewById(R.id.swiperefreshlayout1);
        recyclerview1 = _view.findViewById(R.id.recyclerview1);
        btnRefresh = _view.findViewById(R.id.btn_refresh_channels);

        adapter = new Recyclerview1Adapter(channelList);
        recyclerview1.setAdapter(adapter);

        swiperefreshlayout1.setOnRefreshListener(() -> {
            subscriptionBlocked = false;
            fetchChannels();
        });

        btnRefresh.setOnClickListener(v -> {
            subscriptionBlocked = false;
            fetchChannels();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!subscriptionBlocked) {
            fetchChannels();
            refreshHandler.removeCallbacks(refreshRunnable);
            refreshHandler.postDelayed(refreshRunnable, Config.REFRESH_S * 1000L);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void fetchChannels() {
        if (!isAdded()) return;
        String userId = UserIdHelper.getUserId(requireContext());
        if (!UserIdHelper.isValid(userId)) {
            swiperefreshlayout1.setRefreshing(false);
            UserIdHelper.showChangeUserIdDialog(requireContext(), false, this::fetchChannels);
            return;
        }

        if (!inFlight.compareAndSet(false, true)) {
            return;
        }

        swiperefreshlayout1.setRefreshing(true);

        new Thread(() -> {
            SubscriptionClient.Result result;
            try {
                result = SubscriptionClient.fetch(userId);
            } finally {
                inFlight.set(false);
            }

            if (!isAdded()) return;
            Activity act = getActivity();
            if (act == null) return;
            act.runOnUiThread(() -> {
                if (!isAdded()) return;
                swiperefreshlayout1.setRefreshing(false);
                handleResult(result, userId);
            });
        }).start();
    }

    private void handleResult(SubscriptionClient.Result result, String userId) {
        if (result.status == SubscriptionClient.Status.NOT_SUBSCRIBED) {
            subscriptionBlocked = true;
            refreshHandler.removeCallbacks(refreshRunnable);
            channelList.clear();
            adapter.notifyDataSetChanged();

            Intent intent = new Intent(requireContext(), NotSubscribedActivity.class);
            intent.putExtra(NotSubscribedActivity.EXTRA_MASKED_ID, UserIdHelper.mask(userId));
            notSubscribedLauncher.launch(intent);
            return;
        }

        if (result.status == SubscriptionClient.Status.OK && result.channels != null) {
            subscriptionBlocked = false;
            channelList.clear();
            channelList.addAll(result.channels);
            recyclerview1.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            return;
        }

        if (result.status == SubscriptionClient.Status.DECRYPT_FAILURE) {
            String decryptMsg = result.arabicMessage != null
                    ? result.arabicMessage
                    : "فشل فك تشفير القنوات، تحقق من معرف المستخدم";
            SketchwareUtil.showMessage(requireContext().getApplicationContext(), decryptMsg);
            UserIdHelper.showChangeUserIdDialog(requireContext(), true, this::fetchChannels);
            return;
        }

        String msg = result.arabicMessage != null ? result.arabicMessage : "حدث خطأ غير متوقع";
        SketchwareUtil.showMessage(requireContext().getApplicationContext(), msg);
    }

    public class Recyclerview1Adapter extends RecyclerView.Adapter<Recyclerview1Adapter.ViewHolder> {

        private final List<Channel> _data;

        public Recyclerview1Adapter(List<Channel> _arr) {
            _data = _arr;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater _inflater = requireActivity().getLayoutInflater();
            View _v = _inflater.inflate(R.layout.channel_view, null);
            RecyclerView.LayoutParams _lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            _v.setLayoutParams(_lp);
            return new ViewHolder(_v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder _holder, final int _position) {
            View _view = _holder.itemView;

            final FrameLayout frame_lyt = _view.findViewById(R.id.frame_lyt);
            final ImageView desc = _view.findViewById(R.id.desc);
            final TextView txt_category_name = _view.findViewById(R.id.txt_category_name);

            final Channel ch = _data.get(_position);

            if (ch.name.isEmpty()) {
                txt_category_name.setVisibility(View.GONE);
            } else {
                txt_category_name.setVisibility(View.VISIBLE);
                txt_category_name.setText(ch.name);
                txt_category_name.setSingleLine(true);
                txt_category_name.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                txt_category_name.setSelected(true);
            }

            GradientDrawable nameBg = new GradientDrawable();
            nameBg.setCornerRadius(100);
            nameBg.setColor(0xFF616161);
            txt_category_name.setBackground(nameBg);

            if (ch.img.isEmpty()) {
                desc.setImageResource(R.drawable.mrstr);
            } else {
                Picasso.get()
                        .load(ch.img)
                        .placeholder(R.drawable.mrstr)
                        .error(R.drawable.mrstr)
                        .into(desc);
            }

            frame_lyt.setOnClickListener(v -> {
                if (ch.url.isEmpty()) {
                    SketchwareUtil.showMessage(requireContext().getApplicationContext(), "لا يوجد رابط");
                } else {
                    UrlHolder.set(ch.url);
                    Intent play = new Intent(requireContext().getApplicationContext(), PlayerActivity.class);
                    startActivity(play);
                }
            });

            GradientDrawable ctvSub = new GradientDrawable();
            ctvSub.setCornerRadius(12);
            ctvSub.setColor(Color.parseColor("#FF468B97"));
            desc.setBackground(ctvSub);
            desc.setClipToOutline(true);
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
