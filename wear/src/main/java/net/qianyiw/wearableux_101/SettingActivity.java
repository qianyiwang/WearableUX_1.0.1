package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SettingActivity extends Activity implements View.OnClickListener, DataApi.DataListener {

    SharedPreferences prefs;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    SharedPreferences.Editor editor;

    Boolean hear_rate_status, isRegistered, gestureOn = false;
    boolean adas_on, adas_demo_on;

    ImageView heartrate_setting, adas_setting, homeBT, gesture_setting;
    TextView heart_rate_value;
    Vibrator vibrator;
    MessageServer myMessage;

    GoogleApiClient apiClient;

    BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        homeBT = (ImageView)findViewById(R.id.backButton);
        homeBT.setOnClickListener(this);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(intent);
            }
        };
//        registerReceiver(broadcastReceiver, new IntentFilter(HeartRateService.BROADCAST_ACTION));
        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//        editor.clear().commit();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        hear_rate_status = prefs.getBoolean("hear_rate_status", false);
        gestureOn = prefs.getBoolean("gesture_status", false);

        heartrate_setting = (ImageView) findViewById(R.id.heartrate_setting);
        heartrate_setting.setOnClickListener(this);

        gesture_setting = (ImageView) findViewById(R.id.gesture_setting);
        gesture_setting.setOnClickListener(this);

        if(hear_rate_status)
        {
            heartrate_setting.setImageResource(R.drawable.heart_color_big);
        }

        if(gestureOn){
            gesture_setting.setImageResource(R.drawable.gesture_on);
        }
        else{
            gesture_setting.setImageResource(R.drawable.gesture_off);
        }

        adas_setting = (ImageView) findViewById(R.id.adas_setting);
        adas_setting.setOnClickListener(this);
        heart_rate_value = (TextView) findViewById(R.id.heart_rate_value);

        int adas_status = prefs.getInt("adas_status", 0);
        if (adas_status == 1) {
            adas_setting.setBackgroundResource(R.drawable.adas_on);
            adas_on = true;
        } else {
            adas_setting.setBackgroundResource(R.drawable.adas_off);
            adas_on = false;
        }

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        apiClient.connect();
        Wearable.DataApi.addListener(apiClient, this);

        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(HeartRateService.BROADCAST_ACTION));
        isRegistered = true;
        Log.v("isRegistered", String.valueOf(isRegistered));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adas_setting:
                vibrator.vibrate(50);
                if (!adas_on) {
                    adas_on = true;
                    adas_setting.setBackgroundResource(R.drawable.adas_on);
                    myMessage.sendMessage("adas_on");
                    editor.putInt("adas_status", 1).commit();
                } else {
                    adas_on = false;
                    adas_setting.setBackgroundResource(R.drawable.adas_off);
                    myMessage.sendMessage("adas_off"); // turn off the notification
                    editor.putInt("adas_status", 0).commit();
                }
                break;
            case R.id.heartrate_setting:
                vibrator.vibrate(50);

                if (hear_rate_status) {
                    myMessage.sendMessage("stop");
                    hear_rate_status = false;
                    editor.putBoolean("hear_rate_status", hear_rate_status).commit();
                    heartrate_setting.setImageResource(R.drawable.heart_rate_off);
                    heart_rate_value.setText("");
                    if(isRegistered){
                        unregisterReceiver(broadcastReceiver);
                        isRegistered = false;
                        Log.v("isRegistered", String.valueOf(isRegistered));
                    }
                    stopService(new Intent(getBaseContext(), HeartRateService.class));
                    heartrate_setting.clearAnimation();
                } else {
                    hear_rate_status = true;
                    if(!isRegistered){
                        registerReceiver(broadcastReceiver, new IntentFilter(HeartRateService.BROADCAST_ACTION));
                        isRegistered = true;
                        Log.v("isRegistered", String.valueOf(isRegistered));
                    }
                    editor.putBoolean("hear_rate_status", hear_rate_status).commit();
                    heartrate_setting.setImageResource(R.drawable.heart_color_big);
                    startService(new Intent(getBaseContext(), HeartRateService.class));
                }

                break;
            case R.id.backButton:
                vibrator.vibrate(50);
                if(isRegistered){
                    unregisterReceiver(broadcastReceiver);
                    isRegistered = false;
                    Log.v("isRegistered", String.valueOf(isRegistered));
                }
                myMessage.sendMessage("home");
                finish();
                break;
            case R.id.gesture_setting:
                vibrator.vibrate(50);
//                editor.clear().commit();
                gestureOn = !gestureOn;
                editor.putBoolean("gesture_status", gestureOn).commit();
                if(gestureOn){
                    gesture_setting.setImageResource(R.drawable.gesture_on);
                }
                else{
                    gesture_setting.setImageResource(R.drawable.gesture_off);
                }

                break;
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    private void updateUI(Intent intent) {
        String hr = intent.getStringExtra("HR");
        Log.v("received HR",hr);
        heartrate_setting.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation));
        heart_rate_value.setText(hr);
    }
}
