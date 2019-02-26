package com.coolhw.vlcdemo;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private android.view.SurfaceView mVideoSurface = null;
    private final Handler mHandler = new Handler();
    private ViewStub mRemoteVideoStub;
    private FrameLayout mRemoteVideoFrame;
    private RTSPPlayer mRtspPlayer;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private Button btnPlay, btnStop;
    private EditText etUrl;
    private TextView tvState;
    private Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock
                (PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getName());
        mWakeLock.acquire();
        Util.setContext(this);
        mRemoteVideoFrame = findViewById(R.id.video_surface_frame);
        mRemoteVideoStub = findViewById(R.id.surface_stub);
        etUrl = findViewById(R.id.et_test_url);
        tvState = findViewById(R.id.tv_state);
        sw = findViewById(R.id.sw);

        btnPlay = findViewById(R.id.btn_play);
        btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        mVideoSurface = (android.view.SurfaceView) mRemoteVideoStub.inflate();
        etUrl.setText(Util.getUrl());
        etUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = etUrl.getText().toString();
                Util.saveUrl(url);
                Log.d(TAG, "save:" + url);
                Log.d(TAG, "get:" + Util.getUrl());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        sw.setEnabled(true);
        sw.setChecked(Util.getRtpOverRtsp());
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged is use tcp:" + isChecked);
                Util.saveRtpOverRtsp(isChecked);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (mRtspPlayer != null) {
            mRtspPlayer.stop();
        }
        sw.setEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_play) {
            if (mRtspPlayer != null) return;
            mRtspPlayer = new RTSPPlayer(this, etUrl.getText().toString(), 1000, new RTSPPlayer.StateChangeListener() {
                @Override
                public void onstart() {
                    Log.d(TAG, "onRTSPPlayerStart");
                    sw.setEnabled(false);
                }

                @Override
                public void onStop() {
                    Log.d(TAG, "onRTSPPlayerStop");
                    sw.setEnabled(true);
                }

                @Override
                public void onStateChange(String state) {
                    tvState.setText("state:" + state);
                }
            }, Util.getRtpOverRtsp());
            mRtspPlayer.attactView(mVideoSurface, mRemoteVideoFrame, mHandler);
            sw.setEnabled(false);

        } else if (id == R.id.btn_stop) {
            if (mRtspPlayer != null) {
                mRtspPlayer.stop();
            }
            mRtspPlayer = null;
            sw.setEnabled(true);
        }
    }
}
