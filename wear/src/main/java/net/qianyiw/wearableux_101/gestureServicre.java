package net.qianyiw.wearableux_101;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangqianyi on 2016-10-24.
 */
public class gestureServicre extends Service implements SensorEventListener {

    Sensor senAccelerometer, senGyroscope;
    SensorManager mSensorManager;
    float acc_x, acc_y, acc_z, gry_x, gry_y, gry_z, acc_y_lowpass;
    private float mGry; // acceleration apart from gravity
    private float mGryCurrent; // current acceleration including gravity
    private float mGryLast; // last acceleration including gravity
    private float mAcc; // acceleration apart from gravity
    private float mAccCurrent; // current acceleration including gravity
    private float mAccLast; // last acceleration including gravity
    float thresh_peak = 6; //the threshold to start analysis data
    long thresh_interval = 1000; // threshold of analysis data period
    ArrayList dataArray_acc_x = new ArrayList();
    ArrayList dataGry = new ArrayList();
    boolean findFirstPeak = false;
    boolean gripDect = true;

    public static final String BROADCAST_ACTION = "net.qianyiw.broadcasttest.returnresults";
    Intent intent;
    Vibrator vibrator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"start gesture service",0).show();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        senAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        senGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        mSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        intent = new Intent(BROADCAST_ACTION);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gry_x = event.values[0];
            gry_y = event.values[1];
            gry_z = event.values[2];
            mGryLast = mGryCurrent;
            float omegaMagnitude = (float) Math.sqrt(gry_x * gry_x + gry_y * gry_y + gry_z * gry_z);
            mGryCurrent = omegaMagnitude;
            float delta = mGryCurrent - mGryLast;
            mGry = mGry * 0.9f + delta; // perform low-cut filter

            if(mGry>=thresh_peak) {
                if (!findFirstPeak) {
                    findFirstPeak = true;
                    setFlag();
                } else {
                    //                    dataArr.add(omegaMagnitude);
                    //                    dataArr_x.add(gry_x);
                    dataArray_acc_x.add(acc_y);
                    Log.v("hand moving acc_z", String.valueOf(acc_y_lowpass));
                    dataGry.add(mGry);
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            acc_x = event.values[0];
            acc_y = event.values[1];
            acc_z= event.values[2];
            acc_y_lowpass = acc_y_lowpass * 0.8f + (1 - 0.8f) * event.values[1];

            mAccLast = mAccCurrent;
            mAccCurrent = (float) Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            float delta = mAccCurrent - mAccLast;
            mAcc = mAcc * 0.9f + delta; // perform low-cut filter
            if(acc_z>5&&Math.abs(acc_x)<2&&Math.abs(acc_y)<2&&gripDect){
                gripDect = false;
            }
            Handler handler = new Handler();
            handler.postDelayed(new Runnable(){

                @Override
                public void run() {
                    gripDect = true;
                }
            },1000);

        }
    }

    public void setFlag(){
        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findFirstPeak = false;
                ArrayList peakNum_gry = new ArrayList();
                ArrayList peakNum_acc = new ArrayList();
                peakNum_gry = findPeaks(dataGry);
                peakNum_acc = findPeaks(dataArray_acc_x);
                for (int j=0;j<dataGry.size();j++){
//                    Log.v("dataGry", dataGry.get(j).toString());
                }
//                Log.v("len peakNum_gry", String.valueOf(peakNum_gry.size()));
//                Log.v("len peakNum_acc", String.valueOf(peakNum_acc.size()));

                if(!dataArray_acc_x.isEmpty()){
                    if((float) Collections.min(dataArray_acc_x)<-15) // inside
                    {
                        if(peakNum_gry.size()>=2){
                            vibrator.vibrate(50);
                            intent.putExtra("gesture_result", "DOUBLE INSIDE");
                            sendBroadcast(intent);
                        }
                        else{
                            vibrator.vibrate(50);
                            intent.putExtra("gesture_result", "SINGLE INSIDE");
                            sendBroadcast(intent);
                        }
                    }
                    else // outside
                    {
                        if(peakNum_gry.size()>=2){
                            vibrator.vibrate(50);
                            intent.putExtra("gesture_result", "DOUBLE OUTSIDE");
                            sendBroadcast(intent);
                        }
                        else{
                            vibrator.vibrate(50);
                            intent.putExtra("gesture_result", "SINGLE OUTSIDE");
                            sendBroadcast(intent);
                        }
                    }
                }

                dataArray_acc_x.clear();
                dataGry.clear();
            }
        }, thresh_interval);
    }

    public ArrayList findPeaks(ArrayList dataArr){
        ArrayList peakNum = new ArrayList();

        for (int i=1; i<dataArr.size(); i++){

            if(i<dataArr.size()-1){
                if((float)dataArr.get(i)>(float)dataArr.get(i-1)&&(float)dataArr.get(i)>(float)dataArr.get(i+1)){
                    peakNum.add((float)dataArr.get(i));
                }
            }

        }
        return peakNum;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        Toast.makeText(this,"stop gesture service",0).show();
    }
}
