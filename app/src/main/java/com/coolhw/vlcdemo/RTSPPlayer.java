package com.coolhw.vlcdemo;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

/**
 * @author Anqiren
 * @package com.tencent.rtsp.Logic
 * @create date 2018/8/16 4:14 PM
 * @describe TODO
 * @email anqirens@qq.com
 */
public class RTSPPlayer implements IVLCVout.OnNewVideoLayoutListener  {
    private static final String TAG = "RTSPPlayer";

    private LibVLC mLibVLC = null;
    private MediaPlayer mMediaPlayer = null;
    private Media media;
    private Handler mHandler;

    private FrameLayout mVideoSurfaceFrame;

    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private SurfaceView mVideoSurface = null;

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;

    private int videoHeight = 0;
    private int videoWidth = 0;

    private String mRtspUri;
    private int mNetworkCaching;

    private Context mContext;

    public interface StateChangeListener {
        void onstart();
        void onStop();
        void onStateChange(String state);
    }

    private StateChangeListener stateChangeListener;

    public RTSPPlayer(Context context, String rtspUri, int mNetworkCaching, StateChangeListener stateChangeListener, boolean rtpOverRtsp) {
        mContext = context;
        mRtspUri = rtspUri;
        this.stateChangeListener = stateChangeListener;
        final ArrayList<String> options = new ArrayList<>();
        options.add("-vvv"); // verbosity
        options.add("--drop-late-frames");
        options.add("--skip-frames");
        if(rtpOverRtsp){
            Log.v(TAG,  "Use RTP over RTSP (TCP)");
            options.add("--rtsp-tcp");
        }

        mLibVLC = new LibVLC(context, options);
        mMediaPlayer = new MediaPlayer(mLibVLC);

        media = new Media(mLibVLC, Uri.parse(mRtspUri));
        String networkcaching = ":network-caching=" + mNetworkCaching;
        Log.d(TAG, "networkcaching:" + networkcaching);
        media.setHWDecoderEnabled(true, true);
        media.addOption(networkcaching);
        media.addOption(":drop-late-frames");
        media.addOption(":skip-frames");
        media.addOption(":codec=mediacodec,iomx,all");
        media.addOption(":demux=h264");
        Log.d(TAG, "duration:" + media.getDuration() + "," + media.getType());
        mMediaPlayer.setEventListener(listener);

        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.play();
    }

    private MediaPlayer.EventListener listener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            switch (event.type) {
                case MediaPlayer.Event.ESSelected:
                    Log.d(TAG, "event ESSelected");
                    break;
                case MediaPlayer.Event.ESDeleted:
                    Log.d(TAG, "event ESDeleted");
                    break;
                case MediaPlayer.Event.ESAdded:
                    Log.d(TAG, "event ESAdded");
                    break;
                case MediaPlayer.Event.Vout:
                    Log.d(TAG, "event Vout");
                    break;
                case MediaPlayer.Event.PausableChanged:
                    Log.d(TAG, "event PausableChanged");
                    break;
                case MediaPlayer.Event.SeekableChanged:
                    Log.d(TAG, "event SeekableChanged");
                    break;
                case MediaPlayer.Event.PositionChanged:
//                    Log.d(TAG,"position:" + event.getPositionChanged());
                    break;
                case MediaPlayer.Event.TimeChanged:
//                    Log.d(TAG, "time:" + event.getTimeChanged());
                    break;
                case MediaPlayer.Event.EncounteredError:
                    Log.d(TAG, "event EncounteredError");
                    break;
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "event EndReached");
                    break;
                case MediaPlayer.Event.Stopped:
                    Log.d(TAG, "event Stopped");
                    if(stateChangeListener != null){
                        stateChangeListener.onStop();
                    }
                    if(stateChangeListener != null){
                        stateChangeListener.onStateChange("Stopped");
                    }
                    break;
                case MediaPlayer.Event.Paused:
                    Log.d(TAG, "event Paused");
                    break;
                case MediaPlayer.Event.Playing:
                    Log.d(TAG, "event Playing");
                    if(stateChangeListener != null){
                        stateChangeListener.onstart();
                    }
                    if(stateChangeListener != null){
                        stateChangeListener.onStateChange("Playing");
                    }
                    break;
                case MediaPlayer.Event.Buffering:
                    Log.d(TAG, "event Buffering");
                    event.getBuffering();
                    if(stateChangeListener != null){
                        stateChangeListener.onStateChange("buffering:" + event.getBuffering());
                    }
                    break;
                case MediaPlayer.Event.Opening:
                    Log.d(TAG, "event Opening");
                    if(stateChangeListener != null){
                        stateChangeListener.onStateChange("Opening...");
                    }
                    break;
                case MediaPlayer.Event.MediaChanged:
                    Log.d(TAG, "event MediaChanged");
                    break;
            }
        }
    };


    public void attactView(SurfaceView surfaceView,FrameLayout frameLayout, Handler handler) {
        final IVLCVout vlcVout = mMediaPlayer.getVLCVout();
        vlcVout.setVideoView(surfaceView);
        vlcVout.attachViews(this);

        mVideoSurface = surfaceView;

        mHandler = handler;
        mVideoSurfaceFrame = frameLayout;

        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        updateVideoSurfaces();
                    }
                };

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
    }

    public void stop(){
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }
        try{
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mLibVLC.release();
        } catch (Exception e){
            e.printStackTrace();
        }

        if(stateChangeListener != null){
            stateChangeListener.onStateChange("Stopped");
        }

    }

    private void updateVideoSurfaces() {
        int sw = mVideoSurfaceFrame.getWidth();
        int sh = mVideoSurfaceFrame.getHeight();
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        if (mMediaPlayer == null){
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = mVideoSurface.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            //* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API *//*
            mVideoSurface.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            //* We handle the placement of the video using Android View LayoutParams *//*
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            //* No indication about the density, assuming 1:1 *//*
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            //* Use the specified aspect ratio *//*
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }
        // compute the display aspect ratio
        double dar = dw / dh;
        if (dar < ar)
            dh = dw / ar;
        else
            dw = dh * ar;
        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        mVideoSurface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        mVideoSurface.invalidate();
    }

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        mMediaPlayer.setAspectRatio(null);
        mMediaPlayer.setScale(0);
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth,
                                 int visibleHeight, int sarNum, int sarDen) {
        Log.v(TAG,"onNewVideoLayout width:" + width + ",height:" + height + ",visibleWidth:" + visibleHeight + ",visibleHeight:" + visibleHeight
                + ",sarNum:" + sarNum + ",sarDen:" + sarDen);
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }
}
