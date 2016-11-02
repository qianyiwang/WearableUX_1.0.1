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
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BluetoothService extends Service {

    //Objects for Service **************************************************************************
    private final IBinder myBinder = new MyLocalBinder();
    private int NOTIFICATION_ID = 102;
    SharedPreferences.Editor editor;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
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

    long[] bsVibrate = {100, 100, 96, 96, 92, 92, 88, 88, 84, 84, 80, 80, 76, 76, 72, 72, 68, 68, 64, 64, 60, 60, 56, 56};
    // long[] spVibrate = {50,50,50,50,50,50,50,50};
    long[] spVibrate = {100, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 750, 200, 100, 200, 100, 200, 100, 200, 100, 0,0};
    long[] hwVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};
    // long[] lkVibrate = {50,50,50,50,50,50,50,50};
    long[] lkVibrate = {55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45, 55, 45};
    long[] scVibrate = {50,50,50,50,50,50,50,50};
    long[] tnVibrate = {50,50,50,50,50,50,50,50,50,50,50,50,50,50,50,50};


    /*
    * BLIS
    Number 1
    [100, 100, 96, 96, 92, 92, 88, 88, 84, 84, 80, 80, 76, 76, 72, 72, 68, 68, 64, 64, 60, 60, 56, 56]
    Length of Array is 1872 ms.

    Lane
    Number 1
    [37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53, 37, 53]
    Length of Array is 1980 ms.

    Speeding
    Number 1
    [75, 300, 75, 300, 75, 300, 75, 300, 75, 300, 75, 300, 75, 300, 75, 300]
    Length of Array is 3000 ms.

    * */

    BluetoothAdapter btAdapter = null;

    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private boolean stopThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    protected final int SUCCESS_CONNECT = 0;
    protected final int MESSAGE_READ = 1;
    protected final int FAIL_CONNECT = 2;

    String position = "";
    Intent broadcastIntent;
    TcpSocketConnect myTcpSocket_2;

    Timer timer;
    TimerTask timerTask;
    boolean timerStart = false;
    //**************************** HANDLER *********************************************************
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    //Do Something when connect occurs
                    Log.d("Steve", "Handler - SUCCESS_CONNECT");
                    Toast.makeText(getApplicationContext(), "Device CONNECTED", Toast.LENGTH_SHORT).show();
                    String s = "Successfully Connected";


                    break;

                case FAIL_CONNECT:
                    Toast.makeText(getApplicationContext(), "Device FAILED to Connect! RUN SIMULATOR FIRST", Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_READ:
                    //Log.d("Steve", "Message Received");
                    byte[] readBuf = (byte[]) msg.obj;
                    long[] vibrate = {50, 100, 50, 100, 50, 100, 50, 100};
                    String title = "ADAS Alert";
                    String alertText = new String(readBuf);
                    //terminal.append(alertText);

                    if (alertText.contains("LaneKeeping")) {

                        LKthisTime = System.currentTimeMillis();

                        if (LKthisTime - LKlastTime > 2000) {
                            String LKstring = alertText.replaceAll("[^.0123456789]", "");
                            LKlastTime = System.currentTimeMillis();
                            postNotifications(getApplicationContext(), lkVibrate, R.mipmap.lane_keeping_alert, "Lane Keeping Alert", "", adas_demo_on);
                        }
                    }

                    if (alertText.contains("Headway")) {

                        HWthisTime = System.currentTimeMillis();

                        if (HWthisTime - HWlastTime > 1000) {
                            String HWstring = alertText.replaceAll("[^.0123456789]", "");
                            HWlastTime = System.currentTimeMillis();
                            postNotifications(getApplicationContext(), hwVibrate, R.mipmap.headway_alert, "Headway Alert", "", adas_demo_on);
                        }
                    }

                    if (alertText.contains("BlindSpot")) {

                        BSthisTime = System.currentTimeMillis();

                        if (BSthisTime - BSlastTime > 1000) {
                            String BSstring = alertText.replaceAll("[^.0123456789]", "");
                            BSlastTime = System.currentTimeMillis();
                            postNotifications(getApplicationContext(), bsVibrate, R.mipmap.blis_alert, "Blind Spot Warning", "", adas_demo_on);
                        }
                    }

                    if (alertText.contains("Speed")) {

                        SPthisTime = System.currentTimeMillis();

                        if (SPthisTime - SPlastTime > 60000) {
                            String SPstring = alertText.replaceAll("[^.0123456789]", "");
                            SPlastTime = System.currentTimeMillis();
                            postNotifications(getApplicationContext(), spVibrate, R.mipmap.speed_alert, "Speed Warning", "", adas_demo_on);
                        }
                    }

                    if (alertText.contains("SharpCurve")) {

                        SCthisTime = System.currentTimeMillis();

                        if (SCthisTime - SClastTime > 3000) {
                            String SCstring = alertText.replaceAll("[^.0123456789]", "");
                            SClastTime = System.currentTimeMillis();
                            postNotifications(getApplicationContext(), scVibrate, R.mipmap.sharp_curve_alert, "Sharp Curve Ahead", "", adas_demo_on);
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
        Log.i("Service", "onBind called...");
        return myBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");
        stopThread = false;
        broadcastIntent = new Intent(BROADCAST_ACTION);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        adas_demo_on = intent.getBooleanExtra("adas_demo_mode", false);
        Log.v("adas extra", String.valueOf(adas_demo_on));
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        return super.onStartCommand(intent, flags, startId);
    }


    public class MyLocalBinder extends Binder {
        BluetoothService getService() {
            //Return instance of Bluetooth Service so that MainActivity can access public mehtods
            return BluetoothService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimerTask();
        timerStart = false;
        mHandler.removeCallbacksAndMessages(null);
        stopThread = true;
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
    public void connectDevice(BluetoothDevice device) {
        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    public void disconnectAll() {
        mHandler.removeCallbacksAndMessages(null);
        stopThread = true;
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
            btAdapter.cancelDiscovery();

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
        postNotifications(getApplicationContext(), tnVibrate, R.mipmap.blis_alert, "Test Alert", "", adas_demo_on);
    }


    public void updateBSPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        //***Blind Spot*************************************
//        int duration = settings.getInt("BSDuration", 3000);
//        int on1 = settings.getInt("BSParam1", 200);
//        int on2 = settings.getInt("BSParam2", 200);
//        int on3 = settings.getInt("BSParam3", 200);
//        int off1 = settings.getInt("BSParam4", 200);
//        int off2 = settings.getInt("BSParam5", 200);
//        int off3 = settings.getInt("BSParam6", 200);
//        int split1 = settings.getInt("BSSplit1", 50);
//        int split2 = settings.getInt("BSSplit2", 50);
//
//
//        if (settings.getBoolean("BSState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            double div = duration / total;
//            int repeats = (int)Math.round(div);
//
//            bsVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                bsVibrate[0 + (n * 6)] = off1;
//                bsVibrate[1 + (n * 6)] = on1;
//                bsVibrate[2 + (n * 6)] = off2;
//                bsVibrate[3 + (n * 6)] = on2;
//                bsVibrate[4 + (n * 6)] = off3;
//                bsVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = (int)Math.ceil(segment1 / existing1);
//            int repeats2 = (int)Math.ceil(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            bsVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        bsVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        bsVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        bsVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        bsVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        bsVibrate[m + repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        bsVibrate[m + repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        bsVibrate[m + repeats1 * 4 + 1] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        bsVibrate[m + repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
    }

    public void updateSPPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        //**Speeding *************************************
//        int duration = settings.getInt("SPDuration", 3000);
//        int on1 = settings.getInt("SPParam1", 200);
//        int on2 = settings.getInt("SPParam2", 200);
//        int on3 = settings.getInt("SPParam3", 200);
//        int off1 = settings.getInt("SPParam4", 200);
//        int off2 = settings.getInt("SPParam5", 200);
//        int off3 = settings.getInt("SPParam6", 200);
//        int split1 = settings.getInt("SPSplit1", 50);
//        int split2 = settings.getInt("SPSplit2", 50);
//
//
//        if (settings.getBoolean("SPState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            double div = duration / total;
//            int repeats = (int)Math.round(div);
//
//            spVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                spVibrate[0 + (n * 6)] = off1;
//                spVibrate[1 + (n * 6)] = on1;
//                spVibrate[2 + (n * 6)] = off2;
//                spVibrate[3 + (n * 6)] = on2;
//                spVibrate[4 + (n * 6)] = off3;
//                spVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = (int)Math.ceil(segment1 / existing1);
//            int repeats2 = (int)Math.ceil(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            spVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        spVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        spVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        spVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        spVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        spVibrate[m + repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        spVibrate[m + repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        spVibrate[m + repeats1 * 4 + 1] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        spVibrate[m + repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
    }

    public void updateHWPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        //***Head Way*************************************
//        int duration = settings.getInt("HWDuration", 3000);
//        int on1 = settings.getInt("HWParam1", 200);
//        int on2 = settings.getInt("HWParam2", 200);
//        int on3 = settings.getInt("HWParam3", 200);
//        int off1 = settings.getInt("HWParam4", 200);
//        int off2 = settings.getInt("HWParam5", 200);
//        int off3 = settings.getInt("HWParam6", 200);
//        int split1 = settings.getInt("HWSplit1", 50);
//        int split2 = settings.getInt("HWSplit2", 50);
//
//
//        if (settings.getBoolean("HWState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            double div = duration / total;
//            int repeats = (int)Math.round(div);
//
//            hwVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                hwVibrate[0 + (n * 6)] = off1;
//                hwVibrate[1 + (n * 6)] = on1;
//                hwVibrate[2 + (n * 6)] = off2;
//                hwVibrate[3 + (n * 6)] = on2;
//                hwVibrate[4 + (n * 6)] = off3;
//                hwVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = (int)Math.ceil(segment1 / existing1);
//            int repeats2 = (int)Math.ceil(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            hwVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        hwVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        hwVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        hwVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        hwVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        hwVibrate[m + repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        hwVibrate[m + repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        hwVibrate[m + repeats1 * 4 + 1] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        hwVibrate[m + repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
    }


    public void updateLKPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
//
//        //***Lane Keeping*************************************
//        int duration = settings.getInt("LKDuration", 3000);
//        int on1 = settings.getInt("LKParam1", 200);
//        int on2 = settings.getInt("LKParam2", 200);
//        int on3 = settings.getInt("LKParam3", 200);
//        int off1 = settings.getInt("LKParam4", 200);
//        int off2 = settings.getInt("LKParam5", 200);
//        int off3 = settings.getInt("LKParam6", 200);
//        int split1 = settings.getInt("LKSplit1", 50);
//        int split2 = settings.getInt("LKSplit2", 50);
//
//
//        if (settings.getBoolean("LKState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            double div = duration / total;
//            int repeats = (int)Math.round(div);
//
//            lkVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                lkVibrate[0 + (n * 6)] = off1;
//                lkVibrate[1 + (n * 6)] = on1;
//                lkVibrate[2 + (n * 6)] = off2;
//                lkVibrate[3 + (n * 6)] = on2;
//                lkVibrate[4 + (n * 6)] = off3;
//                lkVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = (int)Math.ceil(segment1 / existing1);
//            int repeats2 = (int)Math.ceil(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            lkVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        lkVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        lkVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        lkVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        lkVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        lkVibrate[m + repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        lkVibrate[m + repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        lkVibrate[m + repeats1 * 4 + 1] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        lkVibrate[m + repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
    }

    public void updateSCPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
//
//        //***Sharp Curve *************************************
//        int duration = settings.getInt("SCDuration", 3000);
//        int on1 = settings.getInt("SCParam1", 200);
//        int on2 = settings.getInt("SCParam2", 200);
//        int on3 = settings.getInt("SCParam3", 200);
//        int off1 = settings.getInt("SCParam4", 200);
//        int off2 = settings.getInt("SCParam5", 200);
//        int off3 = settings.getInt("SCParam6", 200);
//        int split1 = settings.getInt("SCSplit1", 50);
//        int split2 = settings.getInt("SCSplit2", 50);
//
//
//        if (settings.getBoolean("SCState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            double div = duration / total;
//            int repeats = (int)Math.round(div);
//
//            scVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                scVibrate[0 + (n * 6)] = off1;
//                scVibrate[1 + (n * 6)] = on1;
//                scVibrate[2 + (n * 6)] = off2;
//                scVibrate[3 + (n * 6)] = on2;
//                scVibrate[4 + (n * 6)] = off3;
//                scVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = (int) Math.ceil(segment1 / existing1);
//            int repeats2 = (int) Math.ceil(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            scVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        scVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        scVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        scVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        scVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        scVibrate[m + repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        scVibrate[m + repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        scVibrate[m + repeats1 * 4 + 1] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        scVibrate[m + repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
    }

    public void updateTNPatterns() {
//        SharedPreferences settings = getSharedPreferences("Settings", Context.MODE_PRIVATE);
//
//        //***Test Notification*************************************
//        int duration = settings.getInt("TNDuration", 3000);
//        int on1 = settings.getInt("TNParam1", 200);
//        int on2 = settings.getInt("TNParam2", 200);
//        int on3 = settings.getInt("TNParam3", 200);
//        int off1 = settings.getInt("TNParam4", 200);
//        int off2 = settings.getInt("TNParam5", 200);
//        int off3 = settings.getInt("TNParam6", 200);
//        int split1 = settings.getInt("TNSplit1", 50);
//        int split2 = settings.getInt("TNSplit2", 50);
//
//
//        if (settings.getBoolean("TNState", true)) {
//            int total = on1 + on2 + on3 + off1 + off2 + off3;
//            int div = duration / total;
//            int repeats = Math.round(div);
//
//            tnVibrate = new long[6 * (int) repeats];
//            for (int n = 0; n < repeats; n++) {
//                tnVibrate[0 + (n * 6)] = off1;
//                tnVibrate[1 + (n * 6)] = on1;
//                tnVibrate[2 + (n * 6)] = off2;
//                tnVibrate[3 + (n * 6)] = on2;
//                tnVibrate[4 + (n * 6)] = off3;
//                tnVibrate[5 + (n * 6)] = on3;
//            }
//        } else {
//            //Doppler Code
//
//            int segment1 = duration * split1 / 100;
//            int segment2 = duration * split2 / 100;
//
//            int existing1 = on1 + on2 + off1 + off2;
//            int existing2 = on2 + on3 + off2 + off3;
//
//            int repeats1 = Math.round(segment1 / existing1);
//            int repeats2 = Math.round(segment2 / existing2);
//
//            int pairs1 = (repeats1 - 1) * 2;
//            int pairs2 = (repeats2 - 1) * 2;
//
//            int firstHalfOn = Math.abs(on2 - on1) / (pairs1 + 1);
//            int firstHalfOff = Math.abs(off2 - off1) / (pairs1 + 1);
//            int secondHalfOn = Math.abs(on2 - on3) / (pairs2 + 1);
//            int secondHalfOff = Math.abs(off2 - off3) / (pairs2 + 1);
//
//            int totalPattern = repeats1 * 4 + repeats2 * 4;
//
//            tnVibrate = new long[(int) totalPattern];
//
//            for (int m = 0; m < (int) totalPattern; m += 2) {
//                if (m < repeats1 * 4) {
//                    if (off2 >= off1) {
//                        tnVibrate[m] = off1 + m * firstHalfOff / 2;
//                    } else {
//                        tnVibrate[m] = off1 - m * firstHalfOff / 2;
//                    }
//                    if (on2 >= on1) {
//                        tnVibrate[m + 1] = on1 + m * firstHalfOn / 2;
//                    } else {
//                        tnVibrate[m + 1] = on1 - m * firstHalfOn / 2;
//                    }
//                }
//                if (m < repeats2 * 4) {
//                    if (off3 <= off2) {
//                        tnVibrate[m + (int) repeats1 * 4] = off2 - m / 2 * secondHalfOff;
//                    } else {
//                        tnVibrate[m + (int) repeats1 * 4] = off2 + m / 2 * secondHalfOff;
//                    }
//                    if (on3 <= on2) {
//                        tnVibrate[m + (int) repeats1 * 4 + 1 ] = on2 - m / 2 * secondHalfOn;
//                    } else {
//                        tnVibrate[m + (int) repeats1 * 4 + 1] = on2 + m / 2 * secondHalfOn;
//                    }
//
//                }
//            }
//        }
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
        timerTask = new TimerTask() {
            public void run() {
                broadcastIntent.putExtra("POS", position);
                sendBroadcast(broadcastIntent);
            }
        };
    }
}



