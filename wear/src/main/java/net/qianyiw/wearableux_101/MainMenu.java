package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

public class MainMenu extends Activity implements MessageApi.MessageListener, View.OnClickListener{

    private static final String PHONE_PATH = "/from-phone";
    GoogleApiClient apiClient;

    Boolean climate_control_on, mcs_control_watch_on, mcs_control_hmi_on, switch_phone_hmi_on,
            dest_set_hmi_on, bluetooth_audio_watch_on, bluetooth_audio_hmi_on, switch_temp_watch_on, switch_temp_hmi_on;
    Boolean temp_switch_celsius, driver_model_on;
    ImageButton mcs_control, switch_phone_control, climate_control, dest_set, bluetooth_audio_control, switch_temp, setting_button;
    Vibrator vibrator;

    SharedPreferences.Editor button_editor, setting_editor;
    SharedPreferences button_prefs, setting_prefs;
    public static final String BUTTON_STATUS_FILE = "ButtonStatusFile";
    private static final String SETTING_STATUS_FILE = "SettingFile";

    MessageServer myMessage;

    int adas_demo_on;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        apiClient.connect();
        Wearable.MessageApi.addListener(apiClient, this);//very important
        button_editor = getSharedPreferences(BUTTON_STATUS_FILE, MODE_PRIVATE).edit();
        button_prefs = getSharedPreferences(BUTTON_STATUS_FILE, MODE_PRIVATE);
        setting_editor = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE).edit();
        setting_prefs = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE);
        temp_switch_celsius = button_prefs.getBoolean("temp_switch_celsius", true);
        driver_model_on = setting_prefs.getBoolean("driver_mode",false);
        adas_demo_on = setting_prefs.getInt("adas_demo",0);
        initialButtonStatus();
        initialButton();
        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Toast.makeText(this,"OnResume::"+myMessage.myApiClient.isConnected(),0).show();
//
//    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(PHONE_PATH))
        {
            String msg = new String(messageEvent.getData());
            Log.v(PHONE_PATH,msg);
            updateButtonStatus(msg);
        }

    }

    private void updateButtonStatus(String msg) {
        switch (msg)
        {
            case "climate_control_off":
                climate_control_on = false;
                button_editor.putBoolean("climate_button",false).commit();
                climate_control.setVisibility(View.GONE);
                break;
            case "climate_control_on":
                climate_control_on = true;
                button_editor.putBoolean("climate_button",true).commit();
                climate_control.setVisibility(View.VISIBLE);
                break;
            case "mcs_watch_off":
                mcs_control_watch_on = false;
                button_editor.putBoolean("mcs_button_watch",false).commit();
                if(!mcs_control_hmi_on){
                    mcs_control.setVisibility(View.GONE);
                }
                break;
            case "mcs_watch_on":
                mcs_control_watch_on = true;
                button_editor.putBoolean("mcs_button_watch",true).commit();
                if(!mcs_control_hmi_on){
                    mcs_control.setVisibility(View.VISIBLE);
                }
                break;
            case "mcs_hmi_off":
                mcs_control_hmi_on = false;
                button_editor.putBoolean("mcs_button_hmi",false).commit();
                if(!mcs_control_watch_on){
                    mcs_control.setVisibility(View.GONE);
                }
                break;
            case "mcs_hmi_on":
                mcs_control_hmi_on = true;
                button_editor.putBoolean("mcs_button_hmi",true).commit();
                if(!mcs_control_watch_on){
                    mcs_control.setVisibility(View.VISIBLE);
                }
                break;
            case "dest_off":
                dest_set_hmi_on = false;
                button_editor.putBoolean("dest_button",false).commit();
                dest_set.setVisibility(View.GONE);
                break;
            case "dest_on":
                dest_set_hmi_on = true;
                button_editor.putBoolean("dest_button",true).commit();
                dest_set.setVisibility(View.VISIBLE);
                break;
            case "bluetooth_audio_watch_off":
                bluetooth_audio_watch_on = false;
                button_editor.putBoolean("bluetooth_audio_watch",false).commit();
                if(!bluetooth_audio_hmi_on){
                    bluetooth_audio_control.setVisibility(View.GONE);
                }
                break;
            case "bluetooth_audio_watch_on":
                bluetooth_audio_watch_on = true;
                button_editor.putBoolean("bluetooth_audio_watch",true).commit();
                if(!bluetooth_audio_hmi_on){
                    bluetooth_audio_control.setVisibility(View.VISIBLE);
                }
                break;
            case "bluetooth_audio_hmi_off":
                bluetooth_audio_hmi_on = false;
                button_editor.putBoolean("bluetooth_audio_hmi",false).commit();
                if(!bluetooth_audio_watch_on){
                    bluetooth_audio_control.setVisibility(View.GONE);
                }
                break;
            case "bluetooth_audio_hmi_on":
                bluetooth_audio_hmi_on = true;
                button_editor.putBoolean("bluetooth_audio_hmi",true).commit();
                if(!bluetooth_audio_watch_on){
                    bluetooth_audio_control.setVisibility(View.VISIBLE);
                }
                break;
            case "switch_phone_hmi_off":
                switch_phone_hmi_on = false;
                button_editor.putBoolean("switch_phone",false).commit();
                switch_phone_control.setVisibility(View.GONE);
                break;
            case "switch_phone_hmi_on":
                switch_phone_hmi_on = true;
                button_editor.putBoolean("switch_phone",true).commit();
                switch_phone_control.setVisibility(View.VISIBLE);
                break;
            case "switch_temp_watch_off":
                switch_temp_watch_on = false;
                button_editor.putBoolean("switch_temp_watch",false).commit();
                if(!switch_temp_hmi_on){
                    switch_temp.setVisibility(View.GONE);
                }
                break;
            case "switch_temp_watch_on":
                switch_temp_watch_on = true;
                button_editor.putBoolean("switch_temp_watch",true).commit();
                if(!switch_temp_hmi_on){
                    switch_temp.setVisibility(View.VISIBLE);
                }
                break;
            case "switch_temp_hmi_off":
                switch_temp_hmi_on = false;
                button_editor.putBoolean("switch_temp_hmi",false).commit();
                if(!switch_temp_watch_on){
                    switch_temp.setVisibility(View.GONE);
                }
                break;
            case "switch_temp_hmi_on":
                switch_temp_hmi_on = true;
                button_editor.putBoolean("switch_temp_hmi",true).commit();
                if(!switch_temp_watch_on){
                    switch_temp.setVisibility(View.VISIBLE);
                }
                break;
            case "driver_mode":
                driver_model_on = true;
                setting_editor.putBoolean("driver_mode",true).commit();
                break;
            case "passenger_mode":
                driver_model_on = false;
                setting_editor.putBoolean("driver_mode",false).commit();
                break;
        }
    }

    private void initialButton() {
        mcs_control = (ImageButton) findViewById(R.id.mcs_control);
        mcs_control.setOnClickListener(this);
        if(!mcs_control_watch_on&&!mcs_control_hmi_on){
            mcs_control.setVisibility(View.GONE);
        }

        switch_phone_control = (ImageButton) findViewById(R.id.switch_phone);
        switch_phone_control.setOnClickListener(this);
        if(!switch_phone_hmi_on){
            switch_phone_control.setVisibility(View.GONE);
        }

        climate_control = (ImageButton) findViewById(R.id.climate_control);
        climate_control.setOnClickListener(this);
        if(!climate_control_on){
            climate_control.setVisibility(View.GONE);
        }

        dest_set = (ImageButton) findViewById(R.id.dest_set);
        dest_set.setOnClickListener(this);
        if(!dest_set_hmi_on){
            dest_set.setVisibility(View.GONE);
        }

        bluetooth_audio_control = (ImageButton) findViewById(R.id.bluetooth_audio_button);
        bluetooth_audio_control.setOnClickListener(this);
        if(!bluetooth_audio_watch_on&&!bluetooth_audio_hmi_on){
            bluetooth_audio_control.setVisibility(View.GONE);
        }

        switch_temp = (ImageButton)findViewById(R.id.switch_temp);
        switch_temp.setOnClickListener(this);
        if(!switch_temp_watch_on&&!switch_temp_hmi_on){
            switch_temp.setVisibility(View.GONE);
        }

        setting_button = (ImageButton)findViewById(R.id.setting_button);
        setting_button.setOnClickListener(this);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void initialButtonStatus() {
        climate_control_on = button_prefs.getBoolean("climate_button", true);
        mcs_control_watch_on = button_prefs.getBoolean("mcs_button_watch", true);
        mcs_control_hmi_on = button_prefs.getBoolean("mcs_button_hmi", true);
        dest_set_hmi_on = button_prefs.getBoolean("dest_button", true);
        bluetooth_audio_watch_on = button_prefs.getBoolean("bluetooth_audio_watch", true);
        bluetooth_audio_hmi_on = button_prefs.getBoolean("bluetooth_audio_hmi", true);
        switch_phone_hmi_on = button_prefs.getBoolean("switch_phone", true);
        switch_temp_watch_on = button_prefs.getBoolean("switch_temp_watch", true);
        switch_temp_hmi_on = button_prefs.getBoolean("switch_temp_hmi", false);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId())
        {
            case R.id.climate_control:
                vibrator.vibrate(50);
                intent = new Intent(this, ClimateControlActivity.class);
                intent.putExtra("switch_temp", temp_switch_celsius);
                startActivity(intent);
                break;

            case R.id.mcs_control:
                vibrator.vibrate(50);
                if (mcs_control_watch_on && mcs_control_hmi_on) {
                    myMessage.sendMessage("mcs_control");
                    if(!driver_model_on)
                    {
                        intent = new Intent(this, McsPassengerAdjustActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        intent = new Intent(this, MCSActivity.class);
                        startActivity(intent);
                    }

                } else if (mcs_control_watch_on && !mcs_control_hmi_on) {
                    if(!driver_model_on)
                    {
                        intent = new Intent(this, McsPassengerAdjustActivity.class);
                        startActivity(intent);
                    }
                    else
                    {
                        intent = new Intent(this, MCSActivity.class);
                        startActivity(intent);
                    }

                } else if (!mcs_control_watch_on && mcs_control_hmi_on) {
                    myMessage.sendMessage("mcs_control");
                }
                break;

            case R.id.switch_phone:
                vibrator.vibrate(50);
                myMessage.sendMessage("switch_phone");

                break;

            case R.id.dest_set:
                vibrator.vibrate(50);
                myMessage.sendMessage("dest_set");
                intent = new Intent(this, Dest_SetActivity.class);
                startActivity(intent);
                break;

            case R.id.bluetooth_audio_button:
                vibrator.vibrate(50);
                if (bluetooth_audio_watch_on && bluetooth_audio_hmi_on) {
                    myMessage.sendMessage("bluetooth_audio");
                    intent = new Intent(this, AudioActivity.class);
                    startActivity(intent);
                } else if (bluetooth_audio_watch_on && !bluetooth_audio_hmi_on) {
                    intent = new Intent(this, AudioActivity.class);
                    startActivity(intent);
                } else if (!bluetooth_audio_watch_on && bluetooth_audio_hmi_on) {
                    myMessage.sendMessage("bluetooth_audio");
                }
                break;

            case R.id.switch_temp:
                vibrator.vibrate(50);
            if (switch_temp_watch_on && switch_temp_hmi_on) {
                myMessage.sendMessage("temp_switch_hmi");
                if(temp_switch_celsius)
                {
                    Toast.makeText(this, "settint to F", 0).show();
                    temp_switch_celsius = false;
                    button_editor.putBoolean("temp_switch_celsius",temp_switch_celsius).commit();
                    myMessage.sendMessage("temp_switch_fahrenheit");
                }
                else
                {
                    Toast.makeText(this, "setting to C",0).show();
                    temp_switch_celsius = true;
                    button_editor.putBoolean("temp_switch_celsius",temp_switch_celsius).commit();
                    myMessage.sendMessage("temp_switch_celsius");
                }

            } else if (switch_temp_watch_on && !switch_temp_hmi_on) {
                if(temp_switch_celsius)
                {
                    Toast.makeText(this, "setting to F",0).show();
                    temp_switch_celsius = false;
                    button_editor.putBoolean("temp_switch_celsius",temp_switch_celsius).commit();
                    myMessage.sendMessage("temp_switch_fahrenheit");
                }
                else
                {
                    Toast.makeText(this, "setting to C",0).show();
                    temp_switch_celsius = true;
                    button_editor.putBoolean("temp_switch_celsius",temp_switch_celsius).commit();
                    myMessage.sendMessage("temp_switch_celsius");
                }
            } else if (!switch_temp_watch_on && switch_temp_hmi_on) {
                myMessage.sendMessage("temp_switch_hmi");
            }

            break;
            case R.id.setting_button:
                vibrator.vibrate(50);
                intent = new Intent(getBaseContext(), SettingActivity.class);
                intent.putExtra("adas_mode", adas_demo_on);
                startActivity(intent);
                break;
        }
    }
}
