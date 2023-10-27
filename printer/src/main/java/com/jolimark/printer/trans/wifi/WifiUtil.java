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

import com.jolimark.printer.trans.wifi.search.DeviceInfo;
import com.jolimark.printer.trans.wifi.search.SearchDeviceCallback;
import com.jolimark.printer.trans.wifi.search.SearchDeviceThread;
import com.jolimark.printer.trans.wifi.search.SearchDeviceThread1;

@SuppressLint("MissingPermission")
public class WifiUtil {

    private final String TAG = getClass().getSimpleName();

    private SearchDeviceThread searchDeviceThread;
    private SearchDeviceThread1 searchDeviceThread1;

    private boolean flag_searching;
    private MainHandler mainHandler;

    private final int HANDLER_DEVICE_FOUND = 1;
    private final int HANDLER_SEARCH_END = 2;
    private SearchDeviceCallback searchDeviceCallback;

    private boolean flag_searchEnd;
    private boolean flag_searchEnd1;

    public WifiUtil() {
        mainHandler = new MainHandler();
    }

    public void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterReceiver(Context context) {
        context.unregisterReceiver(broadcastReceiver);
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


    public void searchPrinter(final SearchDeviceCallback callback) {
        synchronized (WifiUtil.class) {
            if (flag_searching) {
                return;
            }
            flag_searching = true;
        }
        searchDeviceCallback = callback;

        flag_searchEnd = false;
        flag_searchEnd1 = false;

        searchDeviceThread = new SearchDeviceThread(this.searchCallback);
        searchDeviceThread.start();
        searchDeviceThread1 = new SearchDeviceThread1(this.searchCallback1);
        searchDeviceThread1.start();
    }

    public void stopSearchPrinter() {
        if (searchDeviceThread != null) {
            searchDeviceThread.stopSearching();
            searchDeviceThread = null;
        }
        if (searchDeviceThread1 != null) {
            searchDeviceThread1.stopSearching();
            searchDeviceThread1 = null;
        }
    }


    private SearchDeviceThread.Callback searchCallback = new SearchDeviceThread.Callback() {
        @Override
        public void onDeviceFound(DeviceInfo info) {
            mainHandler.obtainMessage(HANDLER_DEVICE_FOUND, info).sendToTarget();
        }

        @Override
        public void onSearchEnd() {
            flag_searchEnd = true;
            checkSearchFinish();
        }
    };
    private SearchDeviceThread1.Callback searchCallback1 = new SearchDeviceThread1.Callback() {
        @Override
        public void onDeviceFound(DeviceInfo info) {
            mainHandler.obtainMessage(HANDLER_DEVICE_FOUND, info).sendToTarget();
        }

        @Override
        public void onSearchEnd() {
            flag_searchEnd1 = true;
            checkSearchFinish();
        }
    };


    private void checkSearchFinish() {
        if (flag_searchEnd && flag_searchEnd1) {
            mainHandler.sendEmptyMessage(HANDLER_SEARCH_END);

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
                        searchDeviceCallback.deviceFound(deviceInfo);
                    break;
                case HANDLER_SEARCH_END: {
                    if (searchDeviceCallback != null)
                        searchDeviceCallback.searchFinish();
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


//    public void enableWiFi(Context context) {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled())
//            wifiManager.setWifiEnabled(true);
//    }

//    public List getAPList(Context context) {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        return wifiManager.getScanResults();
//    }
//
//    public void setApConnectStateListener(APConnectStateListener apConnectStateListener) {
//        if (wifiReceiver == null)
//            throw new WifiReceiverNotRegisterException();
//        wifiReceiver.setApConnectStateListener(apConnectStateListener);
//    }
//
//    public void setWifiStateListener(WifiStateListener wifiStateListener) {
//        if (wifiReceiver == null)
//            throw new WifiReceiverNotRegisterException();
//        wifiReceiver.setWifiStateListener(wifiStateListener);
//    }
//
//    private WifiReceiver wifiReceiver;
//
//    public void registerAPConnectReceiver(Context context) {
//        if (wifiReceiver != null) {
//            return;
//        }
//        wifiReceiver = new WifiReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        context.registerReceiver(wifiReceiver, intentFilter);
//    }
//
//    public void unregisterWifiReceiver(Context context) {
//        context.unregisterReceiver(wifiReceiver);
//        wifiReceiver = null;
//    }

//    /**
//     * 判断热点是否需要密码
//     *
//     * @param scanResult
//     * @return
//     */
//    public boolean isNeedPwd(ScanResult scanResult) {
//        if (scanResult == null)
//            return false;
//        String capabilities = scanResult.capabilities.trim();
//        if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
//            LogUtil.i("AP ssid: " + scanResult.SSID + "need password.");
//            return true;
//        } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
//            LogUtil.i("AP ssid: " + scanResult.SSID + "need password.");
//            return true;
//        }
//        LogUtil.i("AP ssid: " + scanResult.SSID + "need no password.");
//        return false;
//    }
//
//    /**
//     * 加密类型枚举
//     */
//    public enum EncryptionType {
//        None, Wep, Wap
//    }
//
//
//    /**
//     * 连接指定wifi热点
//     *
//     * @param ssid     热点名字
//     * @param password 热点密码
//     * @param type     热点加密类型
//     */
//    public static boolean connectHotSpot(Context context, String ssid, String password, EncryptionType type) {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        //初始化WifiConfiguration
//        WifiConfiguration config = new WifiConfiguration();
//        config.allowedAuthAlgorithms.clear();
//        config.allowedGroupCiphers.clear();
//        config.allowedKeyManagement.clear();
//        config.allowedPairwiseCiphers.clear();
//        config.allowedProtocols.clear();
//
//        //指定对应的SSID
//        config.SSID = "\"" + ssid + "\"";
//
//        //如果之前有类似的配置
//        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
//        if (configs != null) {
//            WifiConfiguration tempConfig = null;
//            for (WifiConfiguration tconfig : configs) {
//                if (tconfig.SSID.equals("\"" + ssid + "\"")) {
//                    tempConfig = tconfig;
//                }
//            }
//            if (tempConfig != null) {
//                //则清除旧有配置
//                wifiManager.removeNetwork(tempConfig.networkId);
//            }
//        }
//
//        //不需要密码的场景
//        if (type == None) {
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            //以WEP加密的场景
//        } else if (type == Wep) {
//            config.hiddenSSID = true;
//            config.wepKeys[0] = "\"" + password + "\"";
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
//            //以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
//        } else if (type == Wap) {
//            config.preSharedKey = "\"" + password + "\"";
//            config.hiddenSSID = true;
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.status = WifiConfiguration.Status.ENABLED;
//        }
//
//        //构建目标热点配置完成，加入WiFiManager，等同于在android手机上保存了热点的配置信息
//        //保存热点配置，将为该热点分配networkId
//        int networkId = wifiManager.addNetwork(config);
//        //通过networkId连接目标热点
//        return wifiManager.enableNetwork(networkId, true);
//    }

//    public void closeAP(Context context) {
//        try {
//            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
//            method.setAccessible(true);
//            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
//            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//            method2.invoke(wifiManager, config, false);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }

//    public void closeAP(Context context) {
//        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        Method method = null;
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                method = wifiManager.getClass().getDeclaredMethod("stopSoftAp");
//                method.invoke(wifiManager);
//
//            } else {
//                method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
//                method.invoke(wifiManager, null, false);
//            }
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }


}
