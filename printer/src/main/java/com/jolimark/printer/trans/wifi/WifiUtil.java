package com.jolimark.printer.trans.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.jolimark.printer.common.MsgCode;
import com.jolimark.printer.trans.wifi.search.DeviceInfo;
import com.jolimark.printer.trans.wifi.search.SearchCallback;
import com.jolimark.printer.trans.wifi.search.SearchDevice;
import com.jolimark.printer.trans.wifi.search.SearchDevice1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SuppressLint("MissingPermission")
public class WifiUtil {

    private final String TAG = getClass().getSimpleName();

    private SearchDevice searchDevice;
    private SearchDevice1 searchDevice1;

    private boolean flag_searching;
    private MainHandler mainHandler;

    private final int HANDLER_DEVICE_FOUND = 1;
    private final int HANDLER_SEARCH_END = 2;
    private SearchCallback searchDeviceCallback;

    private boolean flag_searchEnd;
    private boolean flag_searchEnd1;

    private boolean flag_searchFail;
    private boolean flag_searchFail1;

    private ExecutorService executorService;
    private ExecutorService executorService1;

    public WifiUtil() {
        mainHandler = new MainHandler();
        executorService = Executors.newSingleThreadExecutor();
        executorService1 = Executors.newSingleThreadExecutor();
    }

    public void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(broadcastReceiver);
    }

    public void destroy() {
        executorService.shutdown();
        executorService1.shutdown();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (state) {
                        case WifiManager.WIFI_STATE_DISABLED: {
                            break;
                        }
                        case WifiManager.WIFI_STATE_ENABLED: {
                            break;
                        }
                    }

                    break;
                }
                case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    NetworkInfo.State state = networkInfo.getState();
                    if (state == NetworkInfo.State.CONNECTED) {
                        if (callback != null) callback.onConnectWifiAp(true);
                    } else if (state == NetworkInfo.State.DISCONNECTED) {
                        if (callback != null) callback.onConnectWifiAp(false);
                    }
                    break;
                }
            }

        }
    };

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onConnectWifiAp(boolean isConnect);
    }


    public void searchPrinter(final SearchCallback callback) {
        synchronized (WifiUtil.class) {
            if (flag_searching) {
                return;
            }
            flag_searching = true;
        }
        searchDeviceCallback = callback;

        flag_searchEnd = false;
        flag_searchEnd1 = false;

        searchDevice = new SearchDevice(this.searchCallback);
        executorService.execute(searchDevice);
        searchDevice1 = new SearchDevice1(this.searchCallback1);
        executorService1.execute(searchDevice1);
    }

    public void stopSearchPrinter() {
        synchronized (WifiUtil.this) {
            if (!flag_searching)
                return;
            flag_searching = false;
        }
        if (searchDevice != null) {
            searchDevice.stopSearching();
            searchDevice = null;
        }
        if (searchDevice1 != null) {
            searchDevice1.stopSearching();
            searchDevice1 = null;
        }
    }


    private SearchCallback searchCallback = new SearchCallback() {
        @Override
        public void onDeviceFound(DeviceInfo info) {
            mainHandler.obtainMessage(HANDLER_DEVICE_FOUND, info).sendToTarget();
        }

        @Override
        public void onSearchEnd() {
            flag_searchEnd = true;
            checkSearchFinish();
        }

        @Override
        public void onSearchFail(String msg) {
            flag_searchEnd = true;
            flag_searchFail = true;
            checkSearchFinish();
        }
    };
    private SearchCallback searchCallback1 = new SearchCallback() {
        @Override
        public void onDeviceFound(DeviceInfo info) {
            mainHandler.obtainMessage(HANDLER_DEVICE_FOUND, info).sendToTarget();
        }

        @Override
        public void onSearchEnd() {
            flag_searchEnd1 = true;
            checkSearchFinish();
        }

        @Override
        public void onSearchFail(String msg) {
            flag_searchEnd1 = true;
            flag_searchFail1 = true;
            checkSearchFinish();
//            mainHandler.obtainMessage(HANDLER_SEARCH_FAIL, msg).sendToTarget();
        }
    };


    private void checkSearchFinish() {
        if (flag_searchEnd && flag_searchEnd1) {
            flag_searching = false;
            String msg = null;
            if (flag_searchFail || flag_searchFail1) {
                msg = MsgCode.getLastErrorMsg();
                MsgCode.clear();
            }
            mainHandler.obtainMessage(HANDLER_SEARCH_END, msg).sendToTarget();
            flag_searching = false;
        }
    }


    class MainHandler extends Handler {

        public MainHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_DEVICE_FOUND:
                    DeviceInfo deviceInfo = (DeviceInfo) msg.obj;
                    if (searchDeviceCallback != null)
                        searchDeviceCallback.onDeviceFound(deviceInfo);
                    break;
                case HANDLER_SEARCH_END: {
                    if (searchDeviceCallback != null) {
                        String errorMsg = (String) msg.obj;
                        if (TextUtils.isEmpty(errorMsg))
                            searchDeviceCallback.onSearchEnd();
                        else
                            searchDeviceCallback.onSearchFail(errorMsg);
                    }
                    break;
                }
            }
        }
    }

    public boolean isWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }


    /**
     * 判断是否已经连接了热点
     *
     * @param context
     * @return
     */
    public boolean isConnectAP(Context context) {
        if (getNetworkType(context) == NetworkType.WIFI)
            return true;
        return false;
    }

    enum NetworkType {
        WIFI, _2G, _3G, _4G, NONE
    }


    /**
     * 获取网络连接类型
     *
     * @param context
     * @return
     */
    public NetworkType getNetworkType(Context context) {
        NetworkType networkType = NetworkType.NONE;
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            switch (activeNetworkInfo.getType()) {
                case 1:
                    networkType = NetworkType.WIFI;
                    break;
                case 0:
                    String subtypeName = activeNetworkInfo.getSubtypeName();
                    switch (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkType()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                            networkType = NetworkType._2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                        case TelephonyManager.NETWORK_TYPE_EHRPD: //api<11 : replace by 12
                        case TelephonyManager.NETWORK_TYPE_HSPAP: //api<13 : replace by 15
                            networkType = NetworkType._3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            networkType = NetworkType._4G;
                            break;
                        default:
                            if (subtypeName.equalsIgnoreCase("TD-SCDMA") || subtypeName.equalsIgnoreCase("WCDMA") || subtypeName.equalsIgnoreCase("CDMA2000")) {
                                networkType = NetworkType._3G;
                            }
                            break;
                    }
                    break;
            }
        }
        return networkType;
    }
}
