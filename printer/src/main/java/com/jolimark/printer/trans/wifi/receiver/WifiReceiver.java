package com.jolimark.printer.trans.wifi.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.jolimark.printer.util.LogUtil;


public class WifiReceiver extends BroadcastReceiver {

    private final String TAG = "WifiReceiver";


    private APConnectStateListener apConnectStateListener;
    private WifiStateListener wifiStateListener;

    private boolean flag_ap_connect_state;
    private boolean flag_wifi_state;

    public void setApConnectStateListener(APConnectStateListener apConnectStateListener) {
        this.apConnectStateListener = apConnectStateListener;
        flag_ap_connect_state = apConnectStateListener == null ? false : true;
    }

    public void setWifiStateListener(WifiStateListener wifiStateListener) {
        this.wifiStateListener = wifiStateListener;
        flag_wifi_state = wifiStateListener == null ? false : true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
            if (!flag_ap_connect_state)
                return;

            int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
            LogUtil.i(TAG, "supplicant state extra_supplicant_error -> " + linkWifiResult);

            if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                LogUtil.i(TAG, "supplicant state -> error_authenticating");
                if (apConnectStateListener != null)
                    apConnectStateListener.onConnectFail();
            }

        } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            if (!flag_wifi_state)
                return;

            int currentWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (currentWifiState) {
                case WifiManager.WIFI_STATE_DISABLING:
                    LogUtil.i(TAG, "wifi state -> disabling");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    LogUtil.i(TAG, "wifi state -> disabled");

                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    LogUtil.i(TAG, "wifi state -> enabling");
                    if (wifiStateListener != null)
                        wifiStateListener.onEnabling();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    LogUtil.i(TAG, "wifi state -> enabled");
                    if (wifiStateListener != null)
                        wifiStateListener.onEnabled();
                    break;

                default:
                    break;
            }
        } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (!flag_ap_connect_state)
                return;

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();

            switch (state) {
                case CONNECTING:
                    LogUtil.i(TAG, "wifi network state -> connecting");
                    if (apConnectStateListener != null)
                        apConnectStateListener.onConnecting();
                    break;
                case CONNECTED:
                    LogUtil.i(TAG, "wifi network state -> connected");
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    String ssid = wifiInfo == null ? "" : wifiInfo.getSSID();
                    if (apConnectStateListener != null)
                        apConnectStateListener.onConnected(ssid);
                    break;
                case SUSPENDED:
                    LogUtil.i(TAG, "wifi network state -> suspended");

                    break;
                case DISCONNECTING:
                    LogUtil.i(TAG, "wifi network state -> disconnecting");

                    break;
                case DISCONNECTED:
                    LogUtil.i(TAG, "wifi network state -> disconnected");

                    break;
                case UNKNOWN:
                    LogUtil.i(TAG, "wifi network state -> unknown");

                    break;
                default:
                    break;
            }
        }
    }
}
