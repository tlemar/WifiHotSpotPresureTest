package com.vivo.zhouchen.wifihotspotpresuretest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.Map;
import java.util.Queue;

/**
 * Created by zhouchen on 2016/1/9.
 */
public class HotSpotPresureTest implements ITestPlaner {

    private WifiManager mWifiManager;
    private TestParas mTestParas;
    private int mTimes = 0;
    NetUtils mNetUtils;
    WifiConfiguration mWifiConfig = null;
    private String tag = "HotSpotPresure";
    Context mContext;

    Object hotspotLock = new Object();
    private boolean isForceStop = false;

    HotSpotPresureTest(Context context) {

        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mNetUtils = new NetUtils(context);
        mTestParas = TestParas.createTestParasFromFile();
        mWifiConfig = mNetUtils.setWifiCofig("wifiTest", "12345678", 1);

    }

    @Override
    public Map<String, String> checkConditions() {
        return null;
    }

    @Override
    public Queue<Runnable> getTestActions() {
        return null;
    }


    @Override
    public boolean execute() {

        HotSpotReceiver hotSpotReceiver = new HotSpotReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        mContext.registerReceiver(hotSpotReceiver, intentFilter);
        while (mTimes < mTestParas.Times) {
//            Log.e(tag, "wifi config is " + mWifiConfig.toString());

            if (isForceStop) {
                mContext.unregisterReceiver(hotSpotReceiver);
                return false;
            }
            mNetUtils.openSoftAp(mWifiConfig);
            synchronized (hotspotLock) {
                try {
                    Log.e(tag, "lock waited");
                    hotspotLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        mContext.unregisterReceiver(hotSpotReceiver);
        return false;
    }

    @Override
    public boolean reportResults() {
        return false;
    }

    @Override
    public boolean stopTestForcely() {
        isForceStop = true;
        return false;
    }

    @Override
    public int getTestType() {
        return 0;
    }

    @Override
    public boolean setTestType(int testType) {
        return false;
    }

    class HotSpotReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(tag, " get action :" + action);
            if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {

                CountDownTimer hotspotTimer = new CountDownTimer(10 * 1000, 10 * 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }
                    @Override
                    public void onFinish() {
                        Log.e(tag, "on Finished ");
                        synchronized (hotspotLock) {
                            hotspotLock.notifyAll();
                        }
                    }
                };

                hotspotTimer.start();
                int wifiApStat = intent.getIntExtra("wifi_state", -1);
                //int preWifiApState = intent.getIntExtra("previous_wifi_state", -1);
                if (wifiApStat == 13 /* && preWifiApState != 13*/ ) {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (hotspotLock) {
                        hotspotLock.notifyAll();
                    }

                    mNetUtils.closeSoftAp(mNetUtils.setWifiCofig("wifiTest", "12345678",1));
                    mTimes++;

                }
                //hotspotTimer.cancel();

            }
        }
    }
}
