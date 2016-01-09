package com.vivo.zhouchen.wifihotspotpresuretest;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


/**
 * Created by vivo on 2015/5/29.
 */
public class NetUtils {
    WifiManager wifiManager;
    ConnectivityManager connectivityManager;
    Context context;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    NetworkInfo networkInfoWifi;

    String tag = "NetUtils";


    public NetUtils(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        //bluetoothAdapter = bluetoothManager.getAdapter();
        networkInfoWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        this.context = context;

    }

    public int getIpAddr() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (wifiInfo == null) {
            return -1;
        } else {
            return wifiInfo.getIpAddress();

        }
    }

    public String getStringIpAddr() {

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return null;
        } else {
            int ipAddr = wifiInfo.getIpAddress();
            StringBuffer ipString = new StringBuffer();
            ipString.append(ipAddr & 0xff).append('.').
                    append((ipAddr >>>= 8) & 0xff).append('.').
                    append((ipAddr >>>= 8) & 0xff).append('.').
                    append((ipAddr >>>= 8) & 0xff);
            return ipString.toString();
        }
    }

    // 将从wifiinfo中得到int类型的ip地址转换成常用的ip形式。
    public static String IpAddrChangeIntToString(int ipAddr) {
        StringBuffer ipString = new StringBuffer();
        ipString.append(ipAddr & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff).append('.').
                append((ipAddr >>>= 8) & 0xff);

        return ipString.toString();
    }

    // 判断一个指定的wifi是否已经保存过；
    // 这个理论上应该是
    public boolean isStoredWifiConfig(WifiConfiguration configuration) {
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration1 : list) {
            if (configuration.equals(configuration1)) {
                return true;
            }
        }
        return false;
    }

    // 配置wifi参数
    public WifiConfiguration setWifiCofig(String ssid, String password, int keyMgmt) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = ssid;

        switch (keyMgmt) {
            case 0:
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
            case 1:
                wifiConfiguration.preSharedKey = password;
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                break;

            case 2:
                Log.e(tag, "create the eap keyMgmt");
                return null;
            case 3:
                Log.e(tag, "create the IEEE8021X keyMgmt");
                return null;
            default:
                break;
        }
        return wifiConfiguration;
    }

    // 获取wifiManager的hide方法
    public Method getWMHideMethod(String method) {

        return null;
    }

    // 开启个人热点
    public boolean openSoftAp(WifiConfiguration configuration) {
        //检测配置信息是否有效
        if (configuration == null) {
            return false;
        } else {
            Method apEnableMthd;
            try {
                apEnableMthd = wifiManager.getClass().getMethod("setWifiApEnabled", configuration.getClass(), boolean.class);
                wifiManager.setWifiEnabled(false);
                Log.e(tag, "apenablemethod " + apEnableMthd.getName().toString());
                apEnableMthd.invoke(wifiManager, configuration, true);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                Log.e(tag, Log.getStackTraceString(e));
                return false;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Log.e(tag, Log.getStackTraceString(e));
                return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Log.e(tag, Log.getStackTraceString(e));
                return false;
            }
            return true;
        }
    }

    //关闭个人热点。
    public boolean closeSoftAp(WifiConfiguration configuration) {
        Method apEnableMthd;
        try {
            apEnableMthd = wifiManager.getClass().getMethod("setWifiApEnabled", configuration.getClass(), boolean.class);
//            wifiManager.setWifiEnabled(false);
            Log.e(tag, "apenablemethod " + apEnableMthd.getName().toString());
            apEnableMthd.invoke(wifiManager, configuration, false);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.e(tag, Log.getStackTraceString(e));
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(tag, Log.getStackTraceString(e));
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(tag, Log.getStackTraceString(e));
            return false;
        }
        return true;
    }



    //查看tetherAble的设备
//    public List<WifiD>


    // 打开wifi
    public boolean openWifi() {
        Log.e(tag, wifiManager.isWifiEnabled() + "");
        if (!wifiManager.isWifiEnabled()) {
            Log.e(tag, "setwifiEnabled true");
            return wifiManager.setWifiEnabled(true);
        }
        return false;
    }

    // 关闭WIFI
    public boolean closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            return wifiManager.setWifiEnabled(false);
        }
        return false;
    }


    public int getWifiState() {
        return wifiManager.getWifiState();
    }

    //连接到指定配置的接入点上
    public int connect2SavedAp(String ssid, String password, int keyType) {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        List<WifiConfiguration> configurationList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration configuration : configurationList) {
            if (configuration.SSID.replace("\"", "").equals(ssid) &&
                    configuration.preSharedKey.equals(password)) {

                wifiManager.disconnect();
                wifiManager.enableNetwork(configuration.networkId, true);
                break;
            }
        }
        return 0;
    }

    public boolean isWifiEnabled() {
        return networkInfoWifi.isAvailable();
    }

    public boolean isWifiConnected() {
        //return wifiManager.getConnectionInfo().getBSSID().equals("00:00:00:00:00:00") ? false : true;
        return networkInfoWifi.isConnected();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean isBtConnected() {
        return bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET)
                != BluetoothProfile.STATE_DISCONNECTED ? true : false;
    }

    // 播放提示音
    public boolean play() {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        mediaPlayer.start();
//        mediaPlayer.
        return true;
    }

    //获取当前网络的广播地址：
    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {

        if (isWifiApEnabled(context)) {
            // 当前手机作为热点使用
            return InetAddress.getByName("192.168.43.1");
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }


    protected static Boolean isWifiApEnabled(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            return (Boolean) method.invoke(manager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {

        } catch (IllegalArgumentException e) {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return false;
    }
}

//class CONSTANT{
//
//}