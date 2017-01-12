package net.qianyiw.wearableux_101;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService_v2 extends Service {

    //Objects for Service **************************************************************************
    private int NOTIFICATION_ID = 102;
    boolean adas_demo_on;

    //Objects for Bluetooth ************************************************************************
    long BSthisTime = 3500;
    long BSlastTime = 0;
    long HWthisTime = 3500;
    long HWlastTime = 0;
    long LKthisTime = 3500;
    long LKlastTime = 0;
    long SPthisTime = 3500;
    long SPlastTime = 0;
    long SCthisTime = 3500;
    long SClastTime = 0;

//    long[] bsVibrate = {100, 100, 96, 96, 92, 92, 88, 88, 84, 84, 80, 80, 76, 76, 72, 72, 68, 68, 64, 64, 60, 60, 56, 56};
//    // long[] spVibrate = {50,50,50,50,50,50,50,50};
//    long[] spVibrate = {100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
//    long[] hwVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
//    // long[] lkVibrate = {50,50,50,50,50,50,50,50};
//    long[] lkVibrate = {55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};
//    long[] scVibrate = {50,50,50,50,50,50,50,50};
//    long[] tnVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};

    long[] lowVibrate = {55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};
    //{100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
    long[] midVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
    //{50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
    long[] highVibrate = {100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
    //{55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};


    BluetoothAdapter btAdapter = null;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String BROADCAST_ACTION = "net.qianyiw.wearableux_101.broadcasttest.BluetoothServie";
    protected final int SUCCESS_CONNECT = 0;
    protected final int MESSAGE_READ = 1;
    protected final int FAIL_CONNECT = 2;

    String position = "";
    static boolean laneKeepingAlert, speedAlert, headwayAlert, blindSpotAlert;
    static int laneKeepVibLevel, headwayVibLevel, speedVibLevel, blindSpotVibLevel;

    Timer timer;
    TimerTask timerTask;
    boolean timerStart = false;

    Context context;
    BroadcastReceiver broadcastReceiver;
    SharedPreferences alert_prefs;
    static SharedPreferences setting_prefs;
    public static final String BUTTON_STATUS_FILE = "ButtonStatusFile";
    private static final String SETTING_STATUS_FILE = "SettingFile";

    static PhoneMainActivity phoneMainActivity;
    //**************************** HANDLER *********************************************************
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    //Do Something when connect occurs
                    Log.d("Steve", "Handler - SUCCESS_CONNECT");
                    Toast.makeText(context, "Device CONNECTED", Toast.LENGTH_SHORT).show();
                    String s = "Successfully Connected";
                    break;

                case FAIL_CONNECT:
                    Toast.makeText(context, "Device FAILED to Connect! RUN SIMULATOR FIRST", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_READ:
                    //Log.d("Steve", "Message Received");
                    byte[] readBuf = (byte[]) msg.obj;
                    long[] vibrate = {50, 100, 50, 100, 50, 100, 50, 100};
                    String title = "ADAS Alert";
                    String alertText = new String(readBuf);
                    //terminal.append(alertText);

                    if (alertText.contains("LaneKeeping")&&laneKeepingAlert) {

                        Log.v("LaneKeeping",String.valueOf(laneKeepingAlert));
                        LKthisTime = System.currentTimeMillis();
                        if (LKthisTime - LKlastTime > 10000) {
                            String LKstring = alertText.replaceAll("[^.0123456789]", "");
                            LKlastTime = System.currentTimeMillis();
//                            postNotifications(context, lkVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                            switch(laneKeepVibLevel){
                                case 1:
                                    postNotifications(context, lowVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                                    break;
                                case 2:
                                    postNotifications(context, midVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                                    break;
                                case 3:
                                    postNotifications(context, highVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                                    break;
                                case 4:
                                    String str = setting_prefs.getString("laneKeepCustomVib",null);
                                    if(str!=null){
                                        String[] strArr = str.split(",");

                                        if(!strArr.equals(null)){
                                            long[] laneKeepCustomVib = new long[strArr.length];
                                            for (int i=0; i<strArr.length; i++){
                                                laneKeepCustomVib[i] = Long.parseLong(strArr[i]);
                                            }
                                            postNotifications(context, laneKeepCustomVib, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                                        }
                                    }
                                    break;
                            }
                        }
                    }

                    if (alertText.contains("Headway")&&headwayAlert) {

                        HWthisTime = System.currentTimeMillis();
                        if (HWthisTime - HWlastTime > 1000) {
                            String HWstring = alertText.replaceAll("[^.0123456789]", "");
                            HWlastTime = System.currentTimeMillis();
//                            postNotifications(context, hwVibrate, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                            switch(headwayVibLevel){
                                case 1:
                                    postNotifications(context, lowVibrate, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                                    break;
                                case 2:
                                    postNotifications(context, midVibrate, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                                    break;
                                case 3:
                                    postNotifications(context, highVibrate, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                                    break;

                                case 4:
                                    String str = setting_prefs.getString("headwayCustomVib",null);
                                    String[] strArr = str.split(",");

                                    if(!strArr.equals(null)){
                                        long[] headwayCustomVib = new long[strArr.length];
                                        for (int i=0; i<strArr.length; i++){
                                            headwayCustomVib[i] = Long.parseLong(strArr[i]);
                                        }
                                        postNotifications(context, headwayCustomVib, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                                    }
                                    break;
                            }
                        }
                    }

                    if (alertText.contains("BlindSpot")&&blindSpotAlert) {

                        BSthisTime = System.currentTimeMillis();

                        if (BSthisTime - BSlastTime > 1000) {
                            String BSstring = alertText.replaceAll("[^.0123456789]", "");
                            BSlastTime = System.currentTimeMillis();
//                            postNotifications(context, bsVibrate, R.mipmap.blis_alert, "Blind Spot Warning", "", adas_demo_on);
                            switch(blindSpotVibLevel){
                                case 1:
                                    postNotifications(context, lowVibrate, R.mipmap.blis_alert, "BlindSpot Alert", "", adas_demo_on);
                                    break;
                                case 2:
                                    postNotifications(context, midVibrate, R.mipmap.blis_alert, "BlindSpot Alert", "", adas_demo_on);
                                    break;
                                case 3:
                                    postNotifications(context, highVibrate, R.mipmap.blis_alert, "BlindSpot Alert", "", adas_demo_on);
                                    break;
                                case 4:
                                    String str = setting_prefs.getString("headwayCustomVib",null);
                                    String[] strArr = str.split(",");

                                    if(!strArr.equals(null)){
                                        long[] headwayCustomVib = new long[strArr.length];
                                        for (int i=0; i<strArr.length; i++){
                                            headwayCustomVib[i] = Long.parseLong(strArr[i]);
                                        }
                                        postNotifications(getApplicationContext(), headwayCustomVib, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                                    }
                                    break;
                            }
                        }
                    }

                    if (alertText.contains("Speed")&&speedAlert) {

                        SPthisTime = System.currentTimeMillis();
                        if (SPthisTime - SPlastTime > 1000) {
                            String SPstring = alertText.replaceAll("[^.0123456789]", "");
                            SPlastTime = System.currentTimeMillis();
//                            postNotifications(context, spVibrate, R.mipmap.speed_alert, "Speed Warning", "", adas_demo_on);
                            switch(speedVibLevel){
                                case 1:
                                    postNotifications(context, lowVibrate, R.mipmap.speed_alert, "Speed Alert", "", adas_demo_on);
                                    break;
                                case 2:
                                    postNotifications(context, midVibrate, R.mipmap.speed_alert, "Speed Alert", "", adas_demo_on);
                                    break;
                                case 3:
                                    postNotifications(context, highVibrate, R.mipmap.speed_alert, "Speed Alert", "", adas_demo_on);
                                    break;

                                case 4:
                                    String str = setting_prefs.getString("speedingCustomVib",null);
                                    String[] strArr = str.split(",");

                                    if(!strArr.equals(null)){
                                        long[] speedingCustomVib = new long[strArr.length];
                                        for (int i=0; i<strArr.length; i++){
                                            speedingCustomVib[i] = Long.parseLong(strArr[i]);
                                        }
                                        postNotifications(context, speedingCustomVib, R.mipmap.speed_alert, "Speeding Alert", "", adas_demo_on);
                                    }
                                    break;
                            }
                        }
                    }

                    if (alertText.contains("SharpCurve")) {

                        SCthisTime = System.currentTimeMillis();

                        if (SCthisTime - SClastTime > 3000) {
                            String SCstring = alertText.replaceAll("[^.0123456789]", "");
                            SClastTime = System.currentTimeMillis();
//                            postNotifications(context, scVibrate, R.mipmap.sharp_curve_alert, "Sharp Curve Ahead", "", adas_demo_on);
                        }
                    }

                    if (alertText.contains("Position")) {
                        int pos = alertText.indexOf('P');
                        position = alertText.substring(pos, pos + 36);
                        if(!timerStart){
                            timerStart = true;
                            startTimer();
                        }
                    }

                    break;
            }
        }
    };
    //************************************ END HANDLER *********************************************


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receiveBroadcastInfo(intent);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(PhoneMainActivity.BROADCAST_ACTION));

        alert_prefs = getSharedPreferences(BUTTON_STATUS_FILE, MODE_PRIVATE);
        setting_prefs = getSharedPreferences(SETTING_STATUS_FILE, MODE_PRIVATE);
    }

    private void receiveBroadcastInfo(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                Log.v("KEY",key);
                if(key.equals("LaneKeepAlert")){
                    laneKeepingAlert = intent.getBooleanExtra("LaneKeepAlert",true);
                    Log.v("laneKeepingAlert", String.valueOf(laneKeepingAlert));
                    Log.v("HeadAlert", String.valueOf(headwayAlert));
                    Log.v("SpeedAlert", String.valueOf(speedAlert));
                    Log.v("BlindSpot", String.valueOf(blindSpotAlert));
                }
                else if(key.equals("HeadAlert")){
                    headwayAlert = intent.getBooleanExtra("HeadAlert",true);
                    Log.v("laneKeepingAlert", String.valueOf(laneKeepingAlert));
                    Log.v("HeadAlert", String.valueOf(headwayAlert));
                    Log.v("SpeedAlert", String.valueOf(speedAlert));
                    Log.v("BlindSpot", String.valueOf(blindSpotAlert));
                }
                else if(key.equals("SpeedAlert")){
                    speedAlert = intent.getBooleanExtra("SpeedAlert",true);
                    Log.v("laneKeepingAlert", String.valueOf(laneKeepingAlert));
                    Log.v("HeadAlert", String.valueOf(headwayAlert));
                    Log.v("SpeedAlert", String.valueOf(speedAlert));
                    Log.v("BlindSpot", String.valueOf(blindSpotAlert));
                }
                else if(key.equals("BlindSpot")){
                    blindSpotAlert = intent.getBooleanExtra("BlindSpot", true);
                    Log.v("laneKeepingAlert", String.valueOf(laneKeepingAlert));
                    Log.v("HeadAlert", String.valueOf(headwayAlert));
                    Log.v("SpeedAlert", String.valueOf(speedAlert));
                    Log.v("BlindSpot", String.valueOf(blindSpotAlert));
                }
                else if(key.equals("laneKeepVibLevel")){
                    laneKeepVibLevel = intent.getIntExtra("laneKeepVibLevel", 1);
                    Log.v("laneKeepVibLevel", String.valueOf(laneKeepVibLevel));
                }
                else if(key.equals("headwayVibLevel")){
                    headwayVibLevel = intent.getIntExtra("headwayVibLevel",1);
                    Log.v("headwayVibLevel", String.valueOf(headwayVibLevel));
                }
                else if(key.equals("speedVibLevel")){
                    speedVibLevel = intent.getIntExtra("speedVibLevel",1);
                    Log.v("speedVibLevel", String.valueOf(speedVibLevel));
                }
                else if(key.equals("blindSpotVibLevel")){
                    blindSpotVibLevel = intent.getIntExtra("blindSpotVibLevel",1);
                    Log.v("blindSpotVibLevel", String.valueOf(blindSpotVibLevel));
                }
            }
        }

//        laneKeepingAlert = intent.getBooleanExtra("LaneKeepAlert",true);
//        Log.v("BroadcastInfo",String.valueOf(laneKeepingAlert));
//        headwayAlert = intent.getBooleanExtra("HeadAlert",true);
//        speedAlert = intent.getBooleanExtra("SpeedAlert",true);
//        blindSpotAlert = intent.getBooleanExtra("BlindSpot", true);
//
//        laneKeepVibLevel = intent.getIntExtra("laneKeepVibLevel", 1);
//        Log.v("LKlastLevel", String.valueOf(laneKeepVibLevel));
//        headwayVibLevel = intent.getIntExtra("headwayVibLevel",1);
//        speedVibLevel = intent.getIntExtra("speedVibLevel",1);
//        blindSpotVibLevel = intent.getIntExtra("blindSpotVibLevel",1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(),"Service started",0).show();
        Log.d("BT SERVICE", "SERVICE STARTED");
        adas_demo_on = intent.getBooleanExtra("adas_demo_mode", false);
        Log.v("adas extra", String.valueOf(adas_demo_on));
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        initialAlertStatus();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initialAlertStatus() {
        laneKeepingAlert = alert_prefs.getBoolean("LaneKeepAlert", true);
        headwayAlert = alert_prefs.getBoolean("HeadAlert", true);
        speedAlert = alert_prefs.getBoolean("SpeedAlert", true);
        blindSpotAlert = alert_prefs.getBoolean("BlindSpot", true);

        // initial vibration status
        laneKeepVibLevel = alert_prefs.getInt("laneKeepVibLevel", 1);
        headwayVibLevel = alert_prefs.getInt("headwayVibLevel", 1);
        speedVibLevel = alert_prefs.getInt("speedVibLevel", 1);
        blindSpotVibLevel = alert_prefs.getInt("blindSpotVibLevel", 1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Service Stop",0).show();
        unregisterReceiver(broadcastReceiver);
        stopTimerTask();
        timerStart = false;
        mHandler.removeCallbacksAndMessages(null);
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        if (connectThread != null) {
            connectThread.cancel();
        }
        Log.d("SERVICE", "onDestroy");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i("Service", "Cancelling notification");
        notificationManager.cancel(NOTIFICATION_ID);
    }


    //**********************************************************************************************
    //*************************** BLUETOOTH METHODS ************************************************
    //**********************************************************************************************
    public void connectDevice(BluetoothDevice device, Context context) {
        this.context = context;
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public void disconnectAll() {
        mHandler.removeCallbacksAndMessages(null);
        if (connectedThread != null) {
            connectedThread.cancel();
        }
        if (connectThread != null) {
            connectThread.cancel();
        }
    }


    //********************************* CONNECT THREAD *********************************************
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(BTMODULEUUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
//            btAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.v("SOCKET","SOCKET GOOD");
//                editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
//                editor.putInt("bluetooth_status", 1);
                // Do work to manage the connection (in a separate thread)
                mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.run();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
//                Toast.makeText(getApplicationContext(), "Device Failed to Connect", Toast.LENGTH_SHORT).show();
                Log.d("Steve", String.valueOf(connectException));
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                // Do work to manage the connection (in a separate thread)
                mHandler.obtainMessage(FAIL_CONNECT, mmSocket).sendToTarget();
                return;
            }

//            // Do work to manage the connection (in a separate thread)
//            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
//            connectedThread = new ConnectedThread(mmSocket);
//            connectedThread.run();
        }


        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
    //********************************* END CONNECT THREAD *****************************************

    //******************************** CONNECTED THREAD ********************************************
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    //Log.d("Steve", "Receiving...");
                    buffer = new byte[256];
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer,0,bytes);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
    //***************************** END CONNECTED THREAD *******************************************


    //**********************************************************************************************
    //******************************** NOTIFICATION CODE *******************************************
    //**********************************************************************************************
    public static void postNotifications(Context context, long[] vibration, int image, String title, String text, boolean adas) {
        NotificationTest test = new NotificationTest(adas);
        Notification[] notifications = test.buildNotifications(context, vibration, image, title, text);
        for (int i = 0; i < notifications.length; i++) {
            Notification not = notifications[i];
            NotificationManagerCompat.from(context).notify(i, not);
        }
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

            if(!adas_demo_on)
            {
                NotificationCompat.Builder summaryBuilder = new
                        NotificationCompat.Builder(context)
                        .setContentTitle(Html.fromHtml(colorStr))
                        .setContentText(text)
                        .setSmallIcon(image)
                        .setVibrate(vibration);
                return new Notification[]{summaryBuilder.build()};
            }
            else
            {
                // Summary
                NotificationCompat.Builder summaryBuilder = new
                        NotificationCompat.Builder(context)
                        .setGroup("Group 1")
                        .setGroupSummary(true)
                        .setContentTitle(Html.fromHtml(colorStr))
                        .setContentText(text)
                        .setSmallIcon(image)
                        .setVibrate(vibration)
                        .extend(new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(context.getResources(), image)));

                // Child 1
                NotificationCompat.Builder childBuilder1 = new
                        NotificationCompat.Builder(context)
                        .setContentTitle(Html.fromHtml(colorStr))
                        .setContentText(text)
                        .setSmallIcon(image)
                        .setLocalOnly(false)
                        .setGroup("Group 1")
                        .extend(new NotificationCompat.WearableExtender().setBackground(BitmapFactory.decodeResource(context.getResources(), image)));

                return new Notification[]{summaryBuilder.build(), childBuilder1.build()};
            }
        }
    }


    public void testNotification(){
        Log.v("test notification", String.valueOf(adas_demo_on));
//        postNotifications(getApplicationContext(), tnVibrate, R.mipmap.blis_alert, "Test Alert", "", adas_demo_on);
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 0ms the TimerTask will run every 5000ms
        timer.schedule(timerTask, 0, 1000); //
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {
        final Intent broadcastIntent = new Intent(BROADCAST_ACTION);
        timerTask = new TimerTask() {
            public void run() {
                broadcastIntent.putExtra("POS", position);
                context.sendBroadcast(broadcastIntent);
            }
        };
    }
}

