package net.qianyiw.wearableux_101;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HeartRateService extends Service implements SensorEventListener, DataApi.DataListener{

    Sensor mHeartRateSensor;
    SensorManager mSensorManager;
    GoogleApiClient apiClient;
    private static final String MESSAGE_KEY_2 = "com.example.key.message_2";
    public static final String BROADCAST_ACTION = "com.websmithing.broadcasttest.displayevent";
    private final Handler handler = new Handler();
    Intent intent;
    Timer timer;
    TimerTask timerTask;
    int hrVal;
    MessageServer myMessage;
    public HeartRateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        apiClient.connect();
        Wearable.DataApi.addListener(apiClient, this);
        // start heart rate sensor
        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (mHeartRateSensor == null) {
            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (Sensor sensor1 : sensors) {
                Log.i("Sensor Type", sensor1.getName() + ": " + sensor1.getType());
            }
        }
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);//define frequency
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    private void DisplayHRInfo(String msg) {
        intent.putExtra("HR", msg);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        stopTimerTask();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
            String msg = "" + (int) event.values[0];
            hrVal = (int) event.values[0];
            //sendDataMap(MESSAGE_KEY,(int)event.values[0]);
            Log.d("Sensor:", msg);
//            sendDataMap(MESSAGE_KEY_2, (int) event.values[0]);
            DisplayHRInfo(msg);
        } else
            Log.d("Sensor:", "Unknown sensor type");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    private void sendDataMap(String message_key, int hr) {
        if (apiClient.isConnected()) {
            Log.v("client status", "connected");
            Log.v("HR", String.valueOf(hr));

            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/data_map_path_2");
            putDataMapReq.getDataMap().putInt(message_key, hr);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(apiClient, putDataReq);

            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(final DataApi.DataItemResult result) {
                    if (result.getStatus().isSuccess()) {
                        Log.d("Pending Result", "Data item set: " + result.getDataItem().getUri());
                    } else {
                        Log.d("Pending Result", "Data sync failed ");
                    }
                }
            });
        }
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
                //sendDataMap(MESSAGE_KEY_2, hrVal);
                myMessage.sendMessage("MyHeartRate:"+hrVal);
            }
        };
    }
}
