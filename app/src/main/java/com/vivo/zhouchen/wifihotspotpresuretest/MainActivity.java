package com.vivo.zhouchen.wifihotspotpresuretest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vivo.zhouchen.wifihotspotpresuretest.UIComponent.StateFAB;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    StateFAB stateFAB;
    String tag = "MainActivity";
    HotSpotPresureTest hotSpotPresureTest;
    TextView mHint;
    int mTimes = 0;
    HotSpotReceiver hotSpotReceiver;

    Button mSubmitStopTimes;

    EditText mStopTimes;
    Handler mHander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSubmitStopTimes = (Button) findViewById(R.id.submitStopTime);
        mStopTimes = (EditText) findViewById(R.id.stopTimes);


        mHander = new Handler(getMainLooper());
        mSubmitStopTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int delaytime  = Integer.parseInt(mStopTimes.getText().toString());
                    hotSpotPresureTest.mDelayTimes = delaytime;
                    Toast.makeText(getApplicationContext(), "成功设置延迟等待时间为"+ delaytime, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e(tag, "exception is " + e);
                    Toast.makeText(getApplicationContext(), "请输入一个正整数", Toast.LENGTH_SHORT).show();

                }
            }
        });

        stateFAB = (StateFAB) findViewById(R.id.fab);
        stateFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(tag, "stateFAB state is " + stateFAB.getActionState());
                if (stateFAB.getActionState()){
                    startTask();
                }else {
                    stopTask();
                }
            }
        });

        mHint = (TextView) findViewById(R.id.textview_softap_times);
        hotSpotReceiver = new HotSpotReceiver();
        mHint.setText("压力测试次数：" + 0);



        String sdcard  = Environment.getExternalStorageDirectory().toString();
        File sd = Environment.getExternalStorageDirectory();

        Log.e(tag, "sd.canWrite() is " + sd.canWrite());

        Log.e(tag, "sdcard file is " + sdcard);
        File file = new File(sd + "/tetherDebug.file");
        Log.e(tag ,"file exist() " + file.exists());

    }

    private void stopTask() {
        stateFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_black_36dp));
        stateFAB.setActionState(true);
        getApplicationContext().unregisterReceiver(hotSpotReceiver);
        hotSpotPresureTest.stopTestForcely();

        new Runnable() {
            @Override
            public void run() {

            }
        };
    }



    private void startTask() {
        mHint.setText("压力测试次数："+0);

        stateFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_36dp));
        stateFAB.setActionState(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                hotSpotPresureTest = new HotSpotPresureTest(getApplicationContext());
                hotSpotPresureTest.execute();
            }
        }).start();
        mTimes = 0;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        getApplicationContext().registerReceiver(hotSpotReceiver, intentFilter);

    }

    class HotSpotReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {

                int wifiApStat = intent.getIntExtra("wifi_state", -1);
                int preWifiApState = intent.getIntExtra("previous_wifi_state", -1);
                if (wifiApStat == 13 && preWifiApState != 13) {

                    mHint.setText("压力测试次数：" +  (++mTimes));
                }
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
