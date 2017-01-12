package net.qianyiw.wearableux_101;

import android.animation.TimeInterpolator;
import android.app.Dialog;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PhoneMainActivity extends AppCompatActivity implements View.OnClickListener, MessageApi.MessageListener, AdapterView.OnItemSelectedListener {

    ImageButton mcs_control_watch, mcs_control_hmi, switch_phone_hmi,
            climate_control_watch, dest_set_hmi,
            bluetooth_audio_watch, bluetooth_audio_hmi, switch_temp_watch, switch_temp_hmi;

    ImageButton laneKeepAlertButton, headAlertButton, speedAlertButton, blindSpotAlertButton;

    int laneKeepVibLevel, headwayVibLevel, speedVibLevel, blindSpotVibLevel;
    Boolean laneKeepAlert, headAlert, speedAlert, blindSpot;
    ImageView laneKeepVibTest, headwayVibTest, speedVibTest, blindSpotVibTest;
    Animation shake_animation;

    ToggleButton modeSwitch, adasSwitch;
    MessageServer myMessage;
    Boolean climate_control_on, mcs_control_watch_on, mcs_control_hmi_on, switch_phone_hmi_on,
            dest_set_hmi_on, bluetooth_audio_watch_on, bluetooth_audio_hmi_on, switch_temp_watch_on, switch_temp_hmi_on;
    Boolean driver_mode_on, adas_demo_on;
    SharedPreferences.Editor button_editor, setting_editor;
    SharedPreferences button_prefs, setting_prefs;
    public static final String BUTTON_STATUS_FILE = "ButtonStatusFile";
    private static final String SETTING_STATUS_FILE = "SettingFile";
    EditText controlIpAddress, hrIpAddress;
    Button controlConnectBT, hrConnectBT;
    TcpSocketConnect myTcpSocket, myTcpSocket_2;
    String hrIp = "", gps = "";
    BroadcastReceiver broadcastReceiver;
    Vibrator vibrator;
    GoogleApiClient apiClient;
    private static final String WEAR_PATH = "/from-watch";

    int[] value;
    int[] position;
    CommandQueue mCommandQueue;

    ImageView bluetooth_connect;
    private BluetoothService bluetoothServiceReferenece;
    int bluetooth_status;

    // ********************** Bluetooth variables **************************
    BroadcastReceiver deviceReceiver;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<String> pairedDevices;
    private BluetoothService_v2 bluetoothServiceReferenece_v2;
    public static final String DEVICE_ADDRESS = "5C:F3:70:6C:7F:4E";//"A0:A8:CD:B3:F2:67";//"5C:F3:70:6C:7F:7B";
    Boolean isReceiverRegistered, isDeviceRegistered ;
    public static final String BROADCAST_ACTION = "net.qianyiw.wearableux_101.broadcasttest.PhoneMain";
    Intent broadCastIntent;

    // ********************** End Bluetooth variables **************************

    // ********************** Spinner variable **************************
    // stores the image database icons
    private static Integer[] spinnerIconDatabase_lk = {R.drawable.lanekeep_alert_default_icon, R.drawable.heading_alert_icon, R.drawable.speed_alert_icon, 0, 0};
    private static Integer[] spinnerIconDatabase_hw = {R.drawable.lanekeep_alert_icon, R.drawable.heading_alert_default_icon, R.drawable.speed_alert_icon, 0, 0};
    private static Integer[] spinnerIconDatabase_sp = {R.drawable.lanekeep_alert_icon, R.drawable.heading_alert_icon, R.drawable.speed_alert_default_icon, 0, 0};
    private static Integer[] spinnerIconDatabase_bs = {R.drawable.bs_alert_default_icon, 0, 0};


    // stores the image database names
    private String[] spinnerTextDatabase = { "","","","Custom","Edit Custom"};
    private String[] spinnerTextDatabase_bs = {"","Custom","Edit Custom"};

    Spinner laneKeepingSpinner, headwaySpinner, speedingSpinner, blindSpotSpinner;
    int laneKeepingSpinnerPosition, headwaySpinnerPosition, speedingSpinnerPosition, blindSpotSpinnerPosition;
    // ********************** End of Spinner variable **************************
    // haptics patterns
    long[] lowVibrate = {55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};
    //{100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
    long[] midVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
    //{50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
    long[] highVibrate = {100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
    //{55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};
    long[] bsVibrate = {100, 100, 96, 96, 92, 92, 88, 88, 84, 84, 80, 80, 76, 76, 72, 72, 68, 68, 64, 64, 60, 60, 56, 56};
    // customize vibration
    ArrayList laneKeepCustomArray, headwayCustomArray, speedCustomArray, blindSpotCustomArray;
    long timeFin, timeInt, timePrev, timeDur;

    // timer progress bar draw
    MyProgressBar progressBar;
    MyCountDownTimer myCountDownTimer;
    Boolean firstTouch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);

        // initial message listener
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);;
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        apiClient.connect();
        Wearable.MessageApi.addListener(apiClient, this);//very important
        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveInfo(intent);
            }
        };

        deviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveBluetoothDevice(intent);
            }
        };

        button_editor = getSharedPreferences(BUTTON_STATUS_FILE, MODE_PRIVATE).edit();
        button_prefs = getSharedPreferences(BUTTON_STATUS_FILE, MODE_PRIVATE);
        setting_editor = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE).edit();
        setting_prefs = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE);
        driver_mode_on = setting_prefs.getBoolean("driver_mode", false);
        adas_demo_on = setting_prefs.getBoolean("adas_demo", false);

        initialButtonStatus();
        initialButton();

        // toggle buttons
        modeSwitch = (ToggleButton) findViewById(R.id.mode_switch);
        adasSwitch = (ToggleButton) findViewById(R.id.adas_switch);
        modeSwitch.setChecked(driver_mode_on);
        adasSwitch.setChecked(adas_demo_on);

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    driver_mode_on = true;
                    setting_editor.putBoolean("driver_mode", true).commit();
                    myMessage.sendMessage("driver_mode");
                } else {
                    driver_mode_on = false;
                    setting_editor.putBoolean("driver_mode", false).commit();
                    myMessage.sendMessage("passenger_mode");
                }
            }
        });

        adasSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ArrayList adasMode = new ArrayList();
                if (isChecked) {
                    adas_demo_on = true;
                    setting_editor.putBoolean("adas_demo", true).commit();
                } else {
                    adas_demo_on = false;
                    setting_editor.putBoolean("adas_demo", false).commit();
                }
            }
        });

        // initial tcp socket
        controlConnectBT = (Button) findViewById(R.id.controlConnectBT);
        controlConnectBT.setOnClickListener(this);
        hrConnectBT = (Button) findViewById(R.id.hrConnectBT);
        hrConnectBT.setOnClickListener(this);
        controlIpAddress = (EditText) findViewById(R.id.controlIpAddress);
        String ip = String.valueOf(controlIpAddress.getText());
        Log.v("ip address", ip);
        myTcpSocket = new TcpSocketConnect(ip, "1024", this);


//        hrIpAddress = (EditText)findViewById(R.id.hrIpAddress);
//        hrIp = String.valueOf(hrIpAddress.getText());
//        myTcpSocket_2 = new TcpSocketConnect(hrIp,"1025", this);

        // initial MCS command queue
        mCommandQueue = CommandQueue.getInstance();
        mCommandQueue.initialize(getApplicationContext());

        // initial adas control
        bluetooth_connect = (ImageView) findViewById(R.id.bluetooth_connect);
        bluetooth_connect.setOnClickListener(this);
        bluetooth_status = setting_prefs.getInt("bluetooth_status", 0);

        bluetooth_connect.setImageResource(R.drawable.bluetooth_icon_black);
        if (bluetooth_status == 1) {
            bluetooth_connect.setVisibility(View.VISIBLE);
        } else {
            bluetooth_connect.setVisibility(View.INVISIBLE);
        }

//        bluetoothServiceReferenece = new BluetoothService();
        bluetoothServiceReferenece_v2 = new BluetoothService_v2();

        initialBluetooth();
        broadCastIntent = new Intent(BROADCAST_ACTION);

        laneKeepVibTest = (ImageView)findViewById(R.id.laneKeepVibTest);
        laneKeepVibTest.setOnClickListener(this);
        headwayVibTest = (ImageView)findViewById(R.id.headwayVibTest);
        headwayVibTest.setOnClickListener(this);
        speedVibTest = (ImageView)findViewById(R.id.speedVibTest);
        speedVibTest.setOnClickListener(this);
        blindSpotVibTest = (ImageView)findViewById(R.id.blindSpotVibTest);
        blindSpotVibTest.setOnClickListener(this);

        shake_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

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

        laneKeepAlert = button_prefs.getBoolean("LaneKeepAlert", true);
        headAlert = button_prefs.getBoolean("HeadAlert", true);
        speedAlert = button_prefs.getBoolean("SpeedAlert", true);
        blindSpot = button_prefs.getBoolean("BlindSpot", true);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReceiverRegistered)
        {
            Log.v("isReceiverRegistered",isReceiverRegistered.toString());
            unregisterReceiver(broadcastReceiver);
            isReceiverRegistered = false;
        }
        else
        {
            Log.v("isReceiverRegistered","BroadcastReceiver is not registered");
        }
        if (isDeviceRegistered)
        {
            Log.v("isDeviceRegistered",isDeviceRegistered.toString());
            unregisterReceiver(deviceReceiver);
            isDeviceRegistered = false;
        }
        else
        {
            Log.v("isDeviceRegistered","deviceReceiver is not registered");
        }
    }

    private void initialButton() {

        // initial spinner

        laneKeepingSpinner = (Spinner)findViewById(R.id.laneKeepingSpinner);
        headwaySpinner = (Spinner)findViewById(R.id.headwaySpinner);
        speedingSpinner = (Spinner)findViewById(R.id.speedSpinner);
        blindSpotSpinner = (Spinner)findViewById(R.id.blindSpotSpinner);
        SpinnerAdapter adapter_lk = new SpinnerAdapter(this,R.layout.adas_spinner_layout, spinnerTextDatabase, spinnerIconDatabase_lk);
        SpinnerAdapter adapter_hw = new SpinnerAdapter(this,R.layout.adas_spinner_layout, spinnerTextDatabase, spinnerIconDatabase_hw);
        SpinnerAdapter adapter_sp = new SpinnerAdapter(this,R.layout.adas_spinner_layout, spinnerTextDatabase, spinnerIconDatabase_sp);
        SpinnerAdapter adapter_bs = new SpinnerAdapter(this,R.layout.adas_spinner_layout, spinnerTextDatabase_bs, spinnerIconDatabase_bs);
        laneKeepingSpinner.setAdapter(adapter_lk);
        laneKeepingSpinner.setOnItemSelectedListener(this);
//        laneKeepingSpinner.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                v.setBackgroundResource(R.drawable.click_button_on);
//                return false;
//            }
//        });
        headwaySpinner.setAdapter(adapter_hw);
        headwaySpinner.setOnItemSelectedListener(this);
        speedingSpinner.setAdapter(adapter_sp);
        speedingSpinner.setOnItemSelectedListener(this);
        blindSpotSpinner.setAdapter(adapter_bs);
        blindSpotSpinner.setOnItemSelectedListener(this);

        climate_control_watch = (ImageButton) findViewById(R.id.climate_control_watch);
        climate_control_watch.setOnClickListener(this);
        if (climate_control_on) {
            climate_control_watch.setBackgroundResource(R.drawable.click_button_on);
        } else {
            climate_control_watch.setBackgroundResource(R.drawable.click_button_off);
        }

        mcs_control_watch = (ImageButton) findViewById(R.id.mcs_control_watch);
        mcs_control_watch.setOnClickListener(this);
        if (mcs_control_watch_on) {
            mcs_control_watch.setBackgroundResource(R.drawable.click_button_on);
        } else {
            mcs_control_watch.setBackgroundResource(R.drawable.click_button_off);
        }

        mcs_control_hmi = (ImageButton) findViewById(R.id.mcs_control_hmi);
        mcs_control_hmi.setOnClickListener(this);
        if (mcs_control_hmi_on) {
            mcs_control_hmi.setBackgroundResource(R.drawable.click_button_on);
        } else {
            mcs_control_hmi.setBackgroundResource(R.drawable.click_button_off);
        }

        dest_set_hmi = (ImageButton) findViewById(R.id.dest_set_hmi);
        dest_set_hmi.setOnClickListener(this);
        if (dest_set_hmi_on) {
            dest_set_hmi.setBackgroundResource(R.drawable.click_button_on);
        } else {
            dest_set_hmi.setBackgroundResource(R.drawable.click_button_off);
        }

        switch_phone_hmi = (ImageButton) findViewById(R.id.switch_phone_hmi);
        switch_phone_hmi.setOnClickListener(this);
        if (switch_phone_hmi_on) {
            switch_phone_hmi.setBackgroundResource(R.drawable.click_button_on);
        } else {
            switch_phone_hmi.setBackgroundResource(R.drawable.click_button_off);
        }

        switch_temp_watch = (ImageButton) findViewById(R.id.switch_temp_watch);
        switch_temp_watch.setOnClickListener(this);
        if (switch_temp_watch_on) {
            switch_temp_watch.setBackgroundResource(R.drawable.click_button_on);
        } else {
            switch_temp_watch.setBackgroundResource(R.drawable.click_button_off);
        }

        switch_temp_hmi = (ImageButton) findViewById(R.id.switch_temp_hmi);
        switch_temp_hmi.setOnClickListener(this);
        if (switch_temp_hmi_on) {
            switch_temp_hmi.setBackgroundResource(R.drawable.click_button_on);
        } else {
            switch_temp_hmi.setBackgroundResource(R.drawable.click_button_off);
        }

        bluetooth_audio_watch = (ImageButton) findViewById(R.id.bluetooth_audio_watch);
        bluetooth_audio_watch.setOnClickListener(this);
        if (bluetooth_audio_watch_on) {
            bluetooth_audio_watch.setBackgroundResource(R.drawable.click_button_on);
        } else {
            bluetooth_audio_watch.setBackgroundResource(R.drawable.click_button_off);
        }

        bluetooth_audio_hmi = (ImageButton) findViewById(R.id.bluetooth_audio_hmi);
        bluetooth_audio_hmi.setOnClickListener(this);
        if (bluetooth_audio_hmi_on) {
            bluetooth_audio_hmi.setBackgroundResource(R.drawable.click_button_on);
        } else {
            bluetooth_audio_hmi.setBackgroundResource(R.drawable.click_button_off);
        }

        laneKeepAlertButton = (ImageButton)findViewById(R.id.laneKeepingAlertButton);
        laneKeepAlertButton.setOnClickListener(this);
        if(laneKeepAlert){
            laneKeepAlertButton.setBackgroundResource(R.drawable.click_button_on);
            laneKeepingSpinner.setEnabled(true);
            laneKeepingSpinner.setBackgroundResource(R.drawable.click_button_on);
        }else{
            laneKeepAlertButton.setBackgroundResource(R.drawable.click_button_off);
            laneKeepingSpinner.setEnabled(false);
            laneKeepingSpinner.setBackgroundResource(R.drawable.click_button_off);
        }

        headAlertButton = (ImageButton)findViewById(R.id.headwayAlertButton);
        headAlertButton.setOnClickListener(this);
        if(headAlert){
            headAlertButton.setBackgroundResource(R.drawable.click_button_on);
            headwaySpinner.setEnabled(true);
            headwaySpinner.setBackgroundResource(R.drawable.click_button_on);
        }else{
            headAlertButton.setBackgroundResource(R.drawable.click_button_off);
            headwaySpinner.setEnabled(false);
            headwaySpinner.setBackgroundResource(R.drawable.click_button_off);
        }

        speedAlertButton = (ImageButton)findViewById(R.id.speedAlertButton);
        speedAlertButton.setOnClickListener(this);
        if(speedAlert){
            speedAlertButton.setBackgroundResource(R.drawable.click_button_on);
            speedingSpinner.setEnabled(true);
            speedingSpinner.setBackgroundResource(R.drawable.click_button_on);
        }else{
            speedAlertButton.setBackgroundResource(R.drawable.click_button_off);
            speedingSpinner.setEnabled(false);
            speedingSpinner.setBackgroundResource(R.drawable.click_button_off);
        }

        blindSpotAlertButton = (ImageButton)findViewById(R.id.blindSpotAlertButton);
        blindSpotAlertButton.setOnClickListener(this);
        if(blindSpot){
            blindSpotAlertButton.setBackgroundResource(R.drawable.click_button_on);
            blindSpotSpinner.setEnabled(true);
            blindSpotSpinner.setBackgroundResource(R.drawable.click_button_on);
        }else{
            blindSpotAlertButton.setBackgroundResource(R.drawable.click_button_off);
            blindSpotSpinner.setEnabled(false);
            blindSpotSpinner.setBackgroundResource(R.drawable.click_button_off);
        }

        // initial spinners

        laneKeepingSpinnerPosition = button_prefs.getInt("lkSpinnerPosition",1);
        headwaySpinnerPosition = button_prefs.getInt("headwaySpinnerPosition",1);
        speedingSpinnerPosition = button_prefs.getInt("speedingSpinnerPosition",1);
        blindSpotSpinnerPosition = button_prefs.getInt("blindSpotSpinnerPosition",1);
        laneKeepingSpinner.setSelection(laneKeepingSpinnerPosition);
        headwaySpinner.setSelection(headwaySpinnerPosition);
        speedingSpinner.setSelection(speedingSpinnerPosition);
        blindSpotSpinner.setSelection(blindSpotSpinnerPosition);
//
//        updateVibButton("laneKeep", laneKeepVibLevel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.climate_control_watch:
                if (climate_control_on) {
                    climate_control_watch.setBackgroundResource(R.drawable.click_button_off);
                    climate_control_on = false;
                    button_editor.putBoolean("climate_button", false).commit();
                    myMessage.sendMessage("climate_control_off");
                } else {
                    climate_control_watch.setBackgroundResource(R.drawable.click_button_on);
                    climate_control_on = true;
                    button_editor.putBoolean("climate_button", true).commit();
                    myMessage.sendMessage("climate_control_on");
                }
                break;
            case R.id.mcs_control_watch:
                if (mcs_control_watch_on) {
                    mcs_control_watch.setBackgroundResource(R.drawable.click_button_off);
                    mcs_control_watch_on = false;
                    button_editor.putBoolean("mcs_button_watch", false).commit();
                    myMessage.sendMessage("mcs_watch_off");
                } else {
                    mcs_control_watch.setBackgroundResource(R.drawable.click_button_on);
                    mcs_control_watch_on = true;
                    button_editor.putBoolean("mcs_button_watch", true).commit();
                    myMessage.sendMessage("mcs_watch_on");
                }
                break;
            case R.id.mcs_control_hmi:
                if (mcs_control_hmi_on) {
                    mcs_control_hmi.setBackgroundResource(R.drawable.click_button_off);
                    mcs_control_hmi_on = false;
                    button_editor.putBoolean("mcs_button_hmi", false).commit();
                    myMessage.sendMessage("mcs_hmi_off");
                } else {
                    mcs_control_hmi.setBackgroundResource(R.drawable.click_button_on);
                    mcs_control_hmi_on = true;
                    button_editor.putBoolean("mcs_button_hmi", true).commit();
                    myMessage.sendMessage("mcs_hmi_on");
                }
                break;
            case R.id.dest_set_hmi:
                if (dest_set_hmi_on) {
                    dest_set_hmi.setBackgroundResource(R.drawable.click_button_off);
                    dest_set_hmi_on = false;
                    button_editor.putBoolean("dest_button", false).commit();
                    myMessage.sendMessage("dest_off");
                } else {
                    dest_set_hmi.setBackgroundResource(R.drawable.click_button_on);
                    dest_set_hmi_on = true;
                    button_editor.putBoolean("dest_button", true).commit();
                    myMessage.sendMessage("dest_on");
                }
                break;
            case R.id.bluetooth_audio_watch:
                if (bluetooth_audio_watch_on) {
                    bluetooth_audio_watch.setBackgroundResource(R.drawable.click_button_off);
                    bluetooth_audio_watch_on = false;
                    button_editor.putBoolean("bluetooth_audio_watch", false).commit();
                    myMessage.sendMessage("bluetooth_audio_watch_off");
                } else {
                    bluetooth_audio_watch.setBackgroundResource(R.drawable.click_button_on);
                    bluetooth_audio_watch_on = true;
                    button_editor.putBoolean("bluetooth_audio_watch", true).commit();
                    myMessage.sendMessage("bluetooth_audio_watch_on");
                }
                break;
            case R.id.bluetooth_audio_hmi:
                if (bluetooth_audio_hmi_on) {
                    bluetooth_audio_hmi.setBackgroundResource(R.drawable.click_button_off);
                    bluetooth_audio_hmi_on = false;
                    button_editor.putBoolean("bluetooth_audio_hmi", false).commit();
                    myMessage.sendMessage("bluetooth_audio_hmi_off");
                } else {
                    bluetooth_audio_hmi.setBackgroundResource(R.drawable.click_button_on);
                    bluetooth_audio_hmi_on = true;
                    button_editor.putBoolean("bluetooth_audio_hmi", true).commit();
                    myMessage.sendMessage("bluetooth_audio_hmi_on");
                }
                break;
            case R.id.switch_phone_hmi:
                if (switch_phone_hmi_on) {
                    switch_phone_hmi.setBackgroundResource(R.drawable.click_button_off);
                    switch_phone_hmi_on = false;
                    button_editor.putBoolean("switch_phone", false).commit();
                    myMessage.sendMessage("switch_phone_hmi_off");
                } else {
                    switch_phone_hmi.setBackgroundResource(R.drawable.click_button_on);
                    switch_phone_hmi_on = true;
                    button_editor.putBoolean("switch_phone", true).commit();
                    myMessage.sendMessage("switch_phone_hmi_on");
                }
                break;
            case R.id.switch_temp_watch:
                if (switch_temp_watch_on) {
                    switch_temp_watch.setBackgroundResource(R.drawable.click_button_off);
                    switch_temp_watch_on = false;
                    button_editor.putBoolean("switch_temp_watch", false).commit();
                    myMessage.sendMessage("switch_temp_watch_off");
                } else {
                    switch_temp_watch.setBackgroundResource(R.drawable.click_button_on);
                    switch_temp_watch_on = true;
                    button_editor.putBoolean("switch_temp_watch", true).commit();
                    myMessage.sendMessage("switch_temp_watch_on");
                }
                break;
            case R.id.switch_temp_hmi:
                if (switch_temp_hmi_on) {
                    switch_temp_hmi.setBackgroundResource(R.drawable.click_button_off);
                    switch_temp_hmi_on = false;
                    button_editor.putBoolean("switch_temp_hmi", false).commit();
                    myMessage.sendMessage("switch_temp_hmi_off");
                } else {
                    switch_temp_hmi.setBackgroundResource(R.drawable.click_button_on);
                    switch_temp_hmi_on = true;
                    button_editor.putBoolean("switch_temp_hmi", true).commit();
                    myMessage.sendMessage("switch_temp_hmi_on");
                }
                break;
            case R.id.hrConnectBT:
                hrIpAddress = (EditText) findViewById(R.id.hrIpAddress);
                hrIp = String.valueOf(hrIpAddress.getText());
                myTcpSocket_2 = new TcpSocketConnect(hrIp, "1025", this);
                break;
            case R.id.controlConnectBT:
                controlIpAddress = (EditText) findViewById(R.id.controlIpAddress);
                String conIp = String.valueOf(controlIpAddress.getText());
                myTcpSocket = new TcpSocketConnect(conIp, "1024", this);
                break;

            case R.id.laneKeepingAlertButton:
                if(laneKeepAlert){
                    laneKeepAlertButton.setBackgroundResource(R.drawable.click_button_off);
                    laneKeepAlert = false;
                    button_editor.putBoolean("LaneKeepAlert", false).commit();
                    laneKeepingSpinner.setEnabled(false);
                    View spinnerView = (View)laneKeepingSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_off);
                    updateVibButton("laneKeep",0);
                }
                else{
                    laneKeepAlertButton.setBackgroundResource(R.drawable.click_button_on);
                    laneKeepAlert = true;
                    laneKeepingSpinner.setEnabled(true);
                    View spinnerView = (View)laneKeepingSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_on);
                    button_editor.putBoolean("LaneKeepAlert", true).commit();
                    int level = button_prefs.getInt("laneKeepLastVibLevel",1);
                    updateVibButton("laneKeep", level);
                }
                broadCastIntent.putExtra("LaneKeepAlert",laneKeepAlert);
                sendBroadcast(broadCastIntent);
                broadCastIntent.removeExtra("LaneKeepAlert");
                break;
            case R.id.headwayAlertButton:
                if(headAlert){
                    headAlertButton.setBackgroundResource(R.drawable.click_button_off);
                    headwaySpinner.setEnabled(false);
                    View spinnerView = (View)headwaySpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_off);
                    headAlert = false;
                    button_editor.putBoolean("HeadAlert", false).commit();
                    updateVibButton("headway", 0);
                }else{
                    headAlertButton.setBackgroundResource(R.drawable.click_button_on);
                    headAlert = true;
                    headwaySpinner.setEnabled(true);
                    View spinnerView = (View)headwaySpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_on);
                    button_editor.putBoolean("HeadAlert", true).commit();
                    int level = button_prefs.getInt("headwayLastVibLevel",1);
                    updateVibButton("headway", level);
                }
                broadCastIntent.putExtra("HeadAlert",headAlert);
                sendBroadcast(broadCastIntent);
                broadCastIntent.removeExtra("HeadAlert");
                break;
            case R.id.speedAlertButton:
                if(speedAlert){
                    speedAlertButton.setBackgroundResource(R.drawable.click_button_off);
                    speedAlert = false;
                    speedingSpinner.setEnabled(false);
                    View spinnerView = (View)speedingSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_off);
                    button_editor.putBoolean("SpeedAlert", false).commit();
                    updateVibButton("speed", 0);
                }else{
                    speedAlertButton.setBackgroundResource(R.drawable.click_button_on);
                    speedAlert = true;
                    speedingSpinner.setEnabled(true);
                    View spinnerView = (View)speedingSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_on);
                    button_editor.putBoolean("SpeedAlert", true).commit();
                    int level = button_prefs.getInt("speedLastVibLevel",1);
                    updateVibButton("speed", level);
                }
                broadCastIntent.putExtra("SpeedAlert",speedAlert);
                sendBroadcast(broadCastIntent);
                broadCastIntent.removeExtra("SpeedAlert");
                break;
            case R.id.blindSpotAlertButton:
                if(blindSpot){
                    blindSpotAlertButton.setBackgroundResource(R.drawable.click_button_off);
                    blindSpot = false;
                    blindSpotSpinner.setEnabled(false);
                    View spinnerView = (View)blindSpotSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_off);
                    button_editor.putBoolean("BlindSpot", false).commit();
                    updateVibButton("blindSpot", 0);
                }else{
                    blindSpotAlertButton.setBackgroundResource(R.drawable.click_button_on);
                    blindSpot = true;
                    blindSpotSpinner.setEnabled(true);
                    View spinnerView = (View)blindSpotSpinner.findViewById(R.id.spinnerContainer);
                    spinnerView.setBackgroundResource(R.drawable.click_button_on);
                    button_editor.putBoolean("BlindSpot", true).commit();
                    int level = button_prefs.getInt("blindSpotLastVibLevel",1);
                    updateVibButton("blindSpot", level);
                }
                broadCastIntent.putExtra("BlindSpot",blindSpot);
                sendBroadcast(broadCastIntent);
                broadCastIntent.removeExtra("BlindSpot");
                break;

            // click vib test
            case R.id.laneKeepVibTest:
                if(laneKeepAlert){
                    laneKeepVibTest.startAnimation(shake_animation);
                    Log.v("laneKeepVibLevel", String.valueOf(laneKeepVibLevel));
                    switch(laneKeepVibLevel){
                        case 1:
                            postNotifications(getApplicationContext(), lowVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                            break;
                        case 2:
                            postNotifications(getApplicationContext(), midVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                            break;
                        case 3:
                            postNotifications(getApplicationContext(), highVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                            break;
                        case 4:
                            String str = setting_prefs.getString("laneKeepCustomVib",null);
                            String[] strArr = str.split(",");

                            if(!strArr.equals(null)){
                                long[] laneKeepCustomVib = new long[strArr.length];
                                for (int i=0; i<strArr.length; i++){
                                    laneKeepCustomVib[i] = Long.parseLong(strArr[i]);
                                }
                                postNotifications(getApplicationContext(), laneKeepCustomVib, R.mipmap.lane_keeping_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                            }
                            break;
                    }
                }
                break;
            case R.id.headwayVibTest:
                if(headAlert){
                    headwayVibTest.startAnimation(shake_animation);
                    switch(headwayVibLevel){
                        case 1:
                            postNotifications(getApplicationContext(), lowVibrate, R.mipmap.headway_alert, "Headway Alert Test", "", adas_demo_on);
                            break;
                        case 2:
                            postNotifications(getApplicationContext(), midVibrate, R.mipmap.headway_alert, "Headway Alert Test", "", adas_demo_on);
                            break;
                        case 3:
                            postNotifications(getApplicationContext(), highVibrate, R.mipmap.headway_alert, "Headway Alert Test", "", adas_demo_on);
                            break;
                        case 4:
                            String str = setting_prefs.getString("headwayCustomVib",null);
                            String[] strArr = str.split(",");

                            if(!strArr.equals(null)){
                                long[] headwayCustomVib = new long[strArr.length];
                                for (int i=0; i<strArr.length; i++){
                                    headwayCustomVib[i] = Long.parseLong(strArr[i]);
                                }
                                postNotifications(getApplicationContext(), headwayCustomVib, R.mipmap.headway_alert, "Headway Alert Test", "", adas_demo_on);
                            }
                            break;
                    }
                }
                break;
            case R.id.speedVibTest:
                if(speedAlert){
                    speedVibTest.startAnimation(shake_animation);
                    switch(speedVibLevel){
                        case 1:
                            postNotifications(getApplicationContext(), lowVibrate, R.mipmap.speed_alert, "Speed Alert Test", "", adas_demo_on);
                            break;
                        case 2:
                            postNotifications(getApplicationContext(), midVibrate, R.mipmap.speed_alert, "Speed Alert Test", "", adas_demo_on);
                            break;
                        case 3:
                            postNotifications(getApplicationContext(), highVibrate, R.mipmap.speed_alert, "Speed Alert Test", "", adas_demo_on);
                            break;
                        case 4:
                            String str = setting_prefs.getString("speedingCustomVib",null);
                            String[] strArr = str.split(",");

                            if(!strArr.equals(null)){
                                long[] speedingCustomVib = new long[strArr.length];
                                for (int i=0; i<strArr.length; i++){
                                    speedingCustomVib[i] = Long.parseLong(strArr[i]);
                                }
                                postNotifications(getApplicationContext(), speedingCustomVib, R.mipmap.speed_alert, "Speeding Alert Test", "", adas_demo_on);
                            }
                            break;
                    }
                }
                break;
            case R.id.blindSpotVibTest:
                if(blindSpot){
                    blindSpotVibTest.startAnimation(shake_animation);
                    switch(blindSpotVibLevel){
                        case 1:
                            postNotifications(getApplicationContext(), bsVibrate, R.mipmap.blis_alert, "BlindSpot Alert Test", "", adas_demo_on);
                            break;
                        case 2:
                            postNotifications(getApplicationContext(), midVibrate, R.mipmap.blis_alert, "BlindSpot Alert Test", "", adas_demo_on);
                            break;
                        case 3:
                            postNotifications(getApplicationContext(), highVibrate, R.mipmap.blis_alert, "BlindSpot Alert Test", "", adas_demo_on);
                            break;
                        case 4:
                            String str = setting_prefs.getString("blindSpotCustomVib",null);
                            String[] strArr = str.split(",");

                            if(!strArr.equals(null)){
                                long[] blindSpotCustomVib = new long[strArr.length];
                                for (int i=0; i<strArr.length; i++){
                                    blindSpotCustomVib[i] = Long.parseLong(strArr[i]);
                                }
                                postNotifications(getApplicationContext(), blindSpotCustomVib, R.mipmap.blis_alert, "BlindSpot Alert Test", "", adas_demo_on);
                            }
                            break;
                    }
                }
                break;
        }
    }

    public void updateVibButton(String buttonName, int level){
        Intent intent = new Intent(BROADCAST_ACTION);
        switch(buttonName){
            case "laneKeep":
                button_editor.putInt("laneKeepVibLevel",level).commit();
                laneKeepVibLevel = level;

                if(level!=0){
                    button_editor.putInt("laneKeepLastVibLevel",level).commit();
                    intent.putExtra("laneKeepVibLevel", level);
                    sendBroadcast(intent);
                    intent.removeExtra("laneKeepVibLevel");
                }
                break;
            case "headway":
                button_editor.putInt("headwayVibLevel",level).commit();
                headwayVibLevel = level;
                if(level!=0){
                    button_editor.putInt("headwayLastVibLevel",level).commit();
                    intent.putExtra("headwayVibLevel", level);
                    sendBroadcast(intent);
                    intent.removeExtra("headwayVibLevel");
                }
                break;
            case "speed":
                button_editor.putInt("speedVibLevel",level).commit();
                speedVibLevel = level;
                if(level!=0){
                    button_editor.putInt("speedLastVibLevel",level).commit();
                    intent.putExtra("speedVibLevel", level);
                    sendBroadcast(intent);
                    intent.removeExtra("speedVibLevel");
                }
                break;
            case "blindSpot":
                button_editor.putInt("blindSpotVibLevel",level).commit();
                blindSpotVibLevel = level;
                if(level!=0){
                    button_editor.putInt("blindSpotLastVibLevel",level).commit();
                    intent.putExtra("blindSpotVibLevel", level);
                    sendBroadcast(intent);
                    intent.removeExtra("blindSpotVibLevel");
                }
                break;
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_PATH)) {
                    String msg = new String(messageEvent.getData());
                    Log.v("message from watch", msg);
                    // adas control
                    if (msg.equals("adas_on")) {
                        bluetooth_connect.setVisibility(View.VISIBLE);
                        bluetooth_status = 1;
                        registerReceiver(deviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
                        isDeviceRegistered = true;
                        setting_editor.putInt("bluetooth_status", 1).commit();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                getPairedDevices();
                                discoverDevice(); // find devices around
                            }
                        }).start();
                        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothService_v2.BROADCAST_ACTION));
                        isReceiverRegistered = true;

//                        if (adas_demo_on) {
//                            Intent intent = new Intent(getBaseContext(), ADASAlertMainActivity.class);
////                            apiClient.disconnect();
//                            intent.putExtra("adas_demo", 1);
//                            startActivity(intent);
//
//                        } else {
//                            Intent intent = new Intent(getBaseContext(), ADASAlertMainActivity.class);
////                            apiClient.disconnect();
//                            intent.putExtra("adas_demo", 0);
//                            startActivity(intent);
//                        }
                    } else if (msg.equals("adas_off")) {
                        bluetooth_connect.setVisibility(View.INVISIBLE);
                        bluetooth_status = 0;
                        if(bluetoothAdapter.isDiscovering()){
                            bluetoothAdapter.cancelDiscovery();
                        }
                        if (isReceiverRegistered)
                        {
                            Log.v("isReceiverRegistered",isReceiverRegistered.toString());
                            unregisterReceiver(broadcastReceiver);
                            isReceiverRegistered = false;
                        }
                        else
                        {
                            Log.v("isReceiverRegistered","BroadcastReceiver is not registered");
                        }
                        if (isDeviceRegistered)
                        {
                            Log.v("isDeviceRegistered",isDeviceRegistered.toString());
                            unregisterReceiver(deviceReceiver);
                            isDeviceRegistered = false;
                        }
                        else
                        {
                            Log.v("isDeviceRegistered","deviceReceiver is not registered");
                        }

                        Intent intent = new Intent(getApplication(), BluetoothService_v2.class);
                        bluetoothServiceReferenece_v2.disconnectAll();
                        stopService(intent);
//                        unbindService(intent);
                        setting_editor.remove("bluetooth_status").commit();
                    }

                    // smart model control
                    else if (msg.equals("massage low")) {
                        position = new int[]{0, 0xff, 0xff};
                        value = new int[]{1, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value); //Turn massage OFF
                    } else if (msg.equals("massage high")) {
                        position = new int[]{0, 0xff, 0xff};
                        value = new int[]{2, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value); //Turn massage OFF
                    } else if (msg.equals("massage close")) {
                        position = new int[]{0, 0xff, 0xff};
                        value = new int[]{0, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value); //Turn massage OFF
                    } else if (msg.contains("low_bolster")) {
                        int pos = msg.indexOf(':');
                        int val = Integer.parseInt(msg.substring(pos + 1)) * 10;
                        Log.v("low bolster val", "idx:" + pos + " val:" + val);
                        position = new int[]{1, 0xff, 0xff};
                        value = new int[]{val, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value);
                    } else if (msg.contains("high_bolster")) {
                        int pos = msg.indexOf(':');
                        int val = Integer.parseInt(msg.substring(pos + 1)) * 10;
                        Log.v("low bolster val", "idx:" + pos + " val:" + val);
                        position = new int[]{2, 0xff, 0xff};
                        value = new int[]{val, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value);
                    } else if (msg.contains("lumbar")) {
                        int pos = msg.indexOf(':');
                        int val = Integer.parseInt(msg.substring(pos + 1)) * 10;
                        Log.v("low bolster val", "idx:" + pos + " val:" + val);
                        position = new int[]{3, 0xff, 0xff};
                        value = new int[]{val, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value);

                        position = new int[]{4, 0xff, 0xff};
                        value = new int[]{val, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value);

                        position = new int[]{5, 0xff, 0xff};
                        value = new int[]{val, 0xff, 0xff};
                        mCommandQueue.addCommand((byte) 0x0f, position, value);
                    }
                    // heart rate data transfer
                    else if (msg.contains("MyHeartRate")) {

                        if (myTcpSocket_2 == null) {
                            Toast.makeText(getApplicationContext(), "Please join the HR network first", 0).show();
                        } else {
                            if (bluetooth_status == 1) {
                                myTcpSocket_2.writeSocket(msg + "_" + gps);
                            } else
                                myTcpSocket_2.writeSocket(msg + "_");
                        }
                    } else if (msg.equals("stop")) {

                        if (myTcpSocket_2 == null) {
                            Toast.makeText(getApplicationContext(), "Please join the HR network first", 0).show();
                        } else {
                            myTcpSocket_2.writeSocket(msg);
                        }
                    }
                    // other control
                    else {
                        myTcpSocket.writeSocket(msg);
                    }

                }
            }
        });
    }

    private void receiveInfo(Intent intent) {
        gps = intent.getStringExtra("POS");
//        Log.v("POS",gps);
    }


    // ************************** Bluetooth Methods *********************************************
    private void initialBluetooth() {

        isReceiverRegistered = false;
        isDeviceRegistered = false;

        pairedDevices = new ArrayList<String>();
        // discover bluetooth device
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT);
        }
        else
        {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void getPairedDevices() {
        devicesArray = bluetoothAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
//                pairedDevices.add(device.getName());
                Log.v("device", device.getName() + ":" + device.getAddress());
            }
        }
    }

    private void discoverDevice() {
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();
    }

    private void receiveBluetoothDevice(Intent intent) {

        // When discovery finds a device
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            // Get the BluetoothDevice object from the Intent
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.v("Bluetooth Device", device.getName()+device.getAddress());

            if(device.getAddress().equals(DEVICE_ADDRESS))
            {
                Toast.makeText(getApplicationContext(), "Device found!!!", Toast.LENGTH_SHORT).show();
                bluetoothAdapter.cancelDiscovery();
                Intent i = new Intent(getBaseContext(), BluetoothService_v2.class);
                i.putExtra("adas_demo", adas_demo_on);
                startService(i);
                bluetoothServiceReferenece_v2.connectDevice(device, getApplicationContext());
            }
        }
    }

// ************************* End Bluetooth Methods ******************************************

//******************************** NOTIFICATION CODE *******************************************
    public static void postNotifications(Context context, long[] vibration, int image, String title, String text, boolean adas) {
        NotificationTest test = new NotificationTest(adas);
        Notification[] notifications = test.buildNotifications(context, vibration, image, title, text);
        for (int i = 0; i < notifications.length; i++) {
            Notification not = notifications[i];
            NotificationManagerCompat.from(context).notify(i, not);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.laneKeepingSpinner:

                switch(position){
                    case 0:
                        button_editor.putInt("lkSpinnerPosition",0).commit();
                        updateVibButton("laneKeep",1);
                        break;
                    case 1:
                        button_editor.putInt("lkSpinnerPosition",1).commit();
                        updateVibButton("laneKeep",2);
                        break;
                    case 2:
                        button_editor.putInt("lkSpinnerPosition",2).commit();
                        updateVibButton("laneKeep",3);
                        break;
                    case 3:
                        button_editor.putInt("lkSpinnerPosition",3).commit();
                        updateVibButton("laneKeep",4);

                        break;
                    case 4:
                        dialogDisplay("laneKeeping alert");
                        parent.setSelection(3);
                        break;
                }
                break;
            case R.id.headwaySpinner:

                switch(position){
                    case 0:
                        button_editor.putInt("headwaySpinnerPosition",0).commit();
                        updateVibButton("headway",1);
                        break;
                    case 1:
                        button_editor.putInt("headwaySpinnerPosition",1).commit();
                        updateVibButton("headway",2);
                        break;
                    case 2:
                        button_editor.putInt("headwaySpinnerPosition",2).commit();
                        updateVibButton("headway",3);
                        break;
                    case 3:
                        button_editor.putInt("headwaySpinnerPosition",3).commit();
                        updateVibButton("headway",4);

                        break;
                    case 4:
                        dialogDisplay("headway alert");
                        parent.setSelection(3);
                        break;
                }
                break;
            case R.id.speedSpinner:
//                spinnerView = (View)speedingSpinner.findViewById(R.id.spinnerContainer);
//                spinnerView.setBackgroundResource(R.drawable.click_button_on);
                switch(position){
                    case 0:
                        button_editor.putInt("speedingSpinnerPosition",0).commit();
                        updateVibButton("speed",1);
                        break;
                    case 1:
                        button_editor.putInt("speedingSpinnerPosition",1).commit();
                        updateVibButton("speed",2);
                        break;
                    case 2:
                        button_editor.putInt("speedingSpinnerPosition",2).commit();
                        updateVibButton("speed",3);
                        break;
                    case 3:
                        button_editor.putInt("speedingSpinnerPosition",3).commit();
                        updateVibButton("speed",4);

                        break;
                    case 4:
                        dialogDisplay("speeding alert");
                        parent.setSelection(3);
                        break;
                }
                break;
            case R.id.blindSpotSpinner:
//                spinnerView = (View)blindSpotSpinner.findViewById(R.id.spinnerContainer);
//                spinnerView.setBackgroundResource(R.drawable.click_button_on);
                switch(position){
                    case 0:
                        button_editor.putInt("blindSpotSpinnerPosition",0).commit();
                        updateVibButton("blindSpot",1);
                        break;
                    case 1:
                        button_editor.putInt("blindSpotSpinnerPosition",1).commit();
                        updateVibButton("blindSpot",4);
                        break;
                    case 2:
                        dialogDisplay("blind spot alert");
                        parent.setSelection(1);
                        break;
//                    case 3:
//                        button_editor.putInt("blindSpotSpinnerPosition",3).commit();
//                        updateVibButton("blindSpot",4);
//                        break;
//                    case 4:
//                        dialogDisplay("blind spot alert");
//                        parent.setSelection(3);
//                        break;
                }
                break;
        }
    }

    private void initialCustomVib(){
        timeFin = 0;
        timeInt = 0;
        timePrev = 0;
        timeDur = 0;
        laneKeepCustomArray = new ArrayList();
        laneKeepCustomArray.add(0l);
        headwayCustomArray = new ArrayList();
        headwayCustomArray.add(0l);
        speedCustomArray = new ArrayList();
        speedCustomArray.add(0l);
        blindSpotCustomArray = new ArrayList();
        blindSpotCustomArray.add(0l);
    }

    private void dialogDisplay(final String name) {
//        final Dialog dialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_customize);
        // initial graph

        TextView title = (TextView)dialog.findViewById(R.id.title);
        title.setText(name);
        initialCustomVib();
        final Button start = (Button)dialog.findViewById(R.id.start);
        final Button save = (Button)dialog.findViewById(R.id.save);
        final Button test = (Button)dialog.findViewById(R.id.test);
        final Button cancel = (Button)dialog.findViewById(R.id.cancel);
        final ImageView touchPad = (ImageView)dialog.findViewById(R.id.touchPad);
        progressBar = (MyProgressBar)dialog.findViewById(R.id.progressBar);

        touchPad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                start.setEnabled(true);
                test.setEnabled(true);
                save.setEnabled(true);


                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    int x1 = progressBar.getProgress();
                    int y1 = progressBar.getHeight()/2;
                    progressBar.graphics.add(new PointF(x1 / 4, y1));
                    progressBar.invalidate();
                }


                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    progressBar.setDown();
                    if(progressBar.getProgress()<4950){
                        if(firstTouch){
                            myCountDownTimer = new MyCountDownTimer(5000,5);
                            myCountDownTimer.start();
                            firstTouch = false;
                        }

                        int x1 = progressBar.getProgress();
                        int y1 = progressBar.getHeight()/2;
                        progressBar.graphics.add(new PointF(x1 / 4, y1));
                        progressBar.invalidate();

                        vibrator.vibrate(50);
                        timeInt = System.currentTimeMillis();
                        timePrev = timeInt - timeFin;
                        if(timeFin != 0){
                            switch(name){
                                case "laneKeeping alert":
                                    laneKeepCustomArray.add(timePrev);
                                    break;
                                case "headway alert":
                                    headwayCustomArray.add(timePrev);
                                    break;
                                case "speeding alert":
                                    speedCustomArray.add(timePrev);
                                    break;
                                case "blind spot alert":
                                    blindSpotCustomArray.add(timePrev);
                                    break;
                            }

                        }
                    }
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
//                    touchDown = false;
//                    progressBar.setUp();
                    if(progressBar.getProgress()<4950){
                        timeFin = System.currentTimeMillis();
                        timeDur = timeFin - timeInt;
                        switch(name){
                            case "laneKeeping alert":
                                laneKeepCustomArray.add(timeDur);
                                break;
                            case "headway alert":
                                headwayCustomArray.add(timeDur);
                                break;
                            case "speeding alert":
                                speedCustomArray.add(timeDur);
                                break;
                            case "blind spot alert":
                                blindSpotCustomArray.add(timeDur);
                                break;
                        }
                    }
                }
                return false;
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myCountDownTimer!=null){
                    myCountDownTimer.cancel();
                    progressBar.xPosition1.clear();
                    progressBar.xPosition2.clear();
                }
                firstTouch = true;
                dialog.cancel();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str;
                switch(name){
                    case "laneKeeping alert":
                        str = "";
                        for(int i=0;i<laneKeepCustomArray.size();i++){
                            str = str+laneKeepCustomArray.get(i)+",";
                        }
                        setting_editor.putString("laneKeepCustomVib",str).commit();
                        break;
                    case "headway alert":
                        str = "";
                        for(int i=0;i<headwayCustomArray.size();i++){
                            str = str+headwayCustomArray.get(i)+",";
                        }
                        setting_editor.putString("headwayCustomVib",str).commit();
                        break;
                    case "speeding alert":
                        str = "";
                        for(int i=0;i<speedCustomArray.size();i++){
                            str = str+speedCustomArray.get(i)+",";
                        }
                        setting_editor.putString("speedingCustomVib",str).commit();
                        break;
                    case "blind spot alert":
                        str = "";
                        for(int i=0;i<blindSpotCustomArray.size();i++){
                            str = str+blindSpotCustomArray.get(i)+",";
                        }
                        setting_editor.putString("blindSpotCustomVib",str).commit();
                        break;
                }

                myCountDownTimer.cancel();
                firstTouch = true;
                progressBar.xPosition1.clear();
                progressBar.xPosition2.clear();
                dialog.cancel();
            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save.setEnabled(true);
                switch(name){
                    case "laneKeeping alert":
                        long[] laneKeepCustomVib = new long[laneKeepCustomArray.size()];
                        for (int i=0; i<laneKeepCustomArray.size(); i++){
                            laneKeepCustomVib[i] = (long) laneKeepCustomArray.get(i);
                            Log.v("array element", String.valueOf(laneKeepCustomVib[i]));
                        }
                        postNotifications(getApplicationContext(), laneKeepCustomVib, R.mipmap.lane_keeping_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                        break;
                    case "headway alert":
                        long[] headCustomVib = new long[headwayCustomArray.size()];
                        for (int i=0; i<headwayCustomArray.size(); i++){
                            headCustomVib[i] = (long) headwayCustomArray.get(i);
                        }
                        postNotifications(getApplicationContext(), headCustomVib, R.mipmap.headway_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                        break;
                    case "speeding alert":
                        long[] speedCustomVib = new long[speedCustomArray.size()];
                        for (int i=0; i<speedCustomArray.size(); i++){
                            speedCustomVib[i] = (long) speedCustomArray.get(i);
                        }
                        postNotifications(getApplicationContext(), speedCustomVib, R.mipmap.speed_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                        break;
                    case "blind spot alert":
                        long[] blindSpotCustomVib = new long[blindSpotCustomArray.size()];
                        for (int i=0; i<blindSpotCustomArray.size(); i++){
                            blindSpotCustomVib[i] = (long) blindSpotCustomArray.get(i);
                        }
                        postNotifications(getApplicationContext(), blindSpotCustomVib, R.mipmap.blis_alert, "Lane Keeping Alert Test", "", adas_demo_on);
                        break;
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save.setEnabled(false);
                test.setEnabled(false);
                initialCustomVib();
                myCountDownTimer.cancel();
                progressBar.setProgress(0);
                firstTouch = true;
                progressBar.graphics.clear();
                progressBar.graphics.clear();
                progressBar.invalidate();
            }
        });
        dialog.setTitle("Customize vibration");
        dialog.show();

    }

    // my count down timer class
    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

//            int progress = (int) (millisUntilFinished/1000);
//            Log.v("millisUntilFinished", String.valueOf(millisUntilFinished));

            progressBar.setProgress(progressBar.getMax()-(int)millisUntilFinished);
        }

        @Override
        public void onFinish() {
//            finish();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private static class NotificationTest {

        boolean adas_demo_on;

        public NotificationTest(boolean adas)
        {
            this.adas_demo_on = adas;
            Log.v("constructor", String.valueOf(adas_demo_on));
        }

        public Notification[] buildNotifications(Context context, long[] vibration, int image, String title, String text) {


            String colorStr = "<font color=\"red\"><b>"+title+"</b></font>";

            NotificationCompat.Builder summaryBuilder = new
                    NotificationCompat.Builder(context)
                    .setContentTitle(Html.fromHtml(colorStr))
                    .setContentText(text)
                    .setSmallIcon(image)
                    .setVibrate(vibration);
            return new Notification[]{summaryBuilder.build()};
            }

    }
}