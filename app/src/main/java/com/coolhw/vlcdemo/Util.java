package com.coolhw.vlcdemo;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * @author Anqiren
 * @package com.tencent.rtsp.Common
 * @create date 2018/8/10 3:42 AM
 * @describe TODO
 * @email anqirens@qq.com
 */

public class Util {
    private static Context mContext;

    public static final String SP_URL = "sp_url";
    public static final String SP_NETWORKCACHING = "sp_networkcaching";
    public static final String SP_RTPOVERRTSP = "sp_rtpoverrtsp";
    public static final String SP_AUDIO_ENCODER = "sp_audio_encoder";

    public static final int DEFAULT_NETWORKCACHING = 1000;
    public static final boolean DEFAULT_RTPOVERRTSP = false;


    public static final String SAMPLE_URL = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";


    private static SharedPreferencesUtil sharedPreferencesUtil;

    public static void setContext(Context context) {
        mContext = context;
        sharedPreferencesUtil = new SharedPreferencesUtil(mContext);
    }

    public static String getLocalIpAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        String ipAddressFormatted = String.format(Locale.ENGLISH, "%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return ipAddressFormatted;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str))
            return true;
        return false;
    }

    public static String getUrl() {
        return sharedPreferencesUtil.getString(SP_URL, SAMPLE_URL);
    }


    public static void saveUrl(String url) {
        if (url != null && url.length() == 0) {
            url = SAMPLE_URL;
        }
        sharedPreferencesUtil.save(SP_URL,url);
    }

    public static int getNetworkCaching() {
        return sharedPreferencesUtil.getInt(SP_NETWORKCACHING, DEFAULT_NETWORKCACHING);
    }

    public static void saveNetworkCaching(int cache) {
        try {
            if (cache <= 0) {
                cache = DEFAULT_NETWORKCACHING;
            }
            sharedPreferencesUtil.save(SP_NETWORKCACHING, cache);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getRtpOverRtsp() {
        return sharedPreferencesUtil.getBoolean(SP_RTPOVERRTSP, DEFAULT_RTPOVERRTSP);
    }

    public static void saveRtpOverRtsp(boolean rtpOverRtsp) {
        sharedPreferencesUtil.save(SP_RTPOVERRTSP, rtpOverRtsp);
    }
}
