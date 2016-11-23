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
    private float yAcc; // acceleration apart from gravity
    private float yAccCurrent; // current acceleration including gravity
    private float yAccLast; // last acceleration including gravity
    ArrayList dataArray_acc_y = new ArrayList();
    ArrayList dataGry = new ArrayList();
    boolean trigger = false;

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

            if(mGry>=6) {
                if (!trigger) {
                    trigger = true;
                    excute();
                }
            }

            if(trigger){
                dataGry.add(mGry);
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            acc_x = event.values[0];
            acc_y = event.values[1];
            acc_z= event.values[2];
//            acc_y_lowpass = acc_y_lowpass * 0.8f + (1 - 0.8f) * event.values[1];
            yAccLast = yAccCurrent;
            yAccCurrent = acc_y;//(float) Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
            float delta = yAccCurrent - yAccLast;
            yAcc = yAcc * 0.9f + delta; // perform low-cut filter

            if(trigger){
                dataArray_acc_y.add(yAcc);
            }

        }
    }

    private boolean checkIfOutside(ArrayList<Float> dataArr){
        for(float f: dataArr){
            if(f>20){
                return false;
            }
        }

        return true;
    }

    public void excute(){
        // Execute some code after 2 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                trigger = false;
                ArrayList peakNum_gry = findPeaks(dataGry);
                for (int j=0;j<dataGry.size();j++){
//                    Log.v("dataGry", dataGry.get(j).toString());
                }
//                Log.v("len peakNum_gry", String.valueOf(peakNum_gry.size()));
//                Log.v("len peakNum_acc", String.valueOf(peakNum_acc.size()));

                if(peakNum_gry.size()>=2){
                    if(checkIfOutside(dataArray_acc_y)){
                        vibrator.vibrate(50);
                        intent.putExtra("gesture_result", "DOUBLE OUTSIDE");
                        sendBroadcast(intent);
                    }
                    else{
                        vibrator.vibrate(50);
                        intent.putExtra("gesture_result", "DOUBLE INSIDE");
                        sendBroadcast(intent);
                    }
                }
                else{
                    if(checkIfOutside(dataArray_acc_y)){
                        vibrator.vibrate(50);
                        intent.putExtra("gesture_result", "SINGLE OUTSIDE");
                        sendBroadcast(intent);
                    }
                    else{
                        vibrator.vibrate(50);
                        intent.putExtra("gesture_result", "SINGLE INSIDE");
                        sendBroadcast(intent);
                    }
                }

                dataArray_acc_y.clear();
                dataGry.clear();
            }
        }, 1000);
    }

    private ArrayList findPeaks(ArrayList<Float> dataArr){
        ArrayList<Float> bigVal = new ArrayList();
        ArrayList<Float> peakNum = new ArrayList();

        for (int i=1; i<dataArr.size(); i++){

            if(i<dataArr.size()-1){
                if((float)dataArr.get(i)>(float)dataArr.get(i-1)&&(float)dataArr.get(i)>(float)dataArr.get(i+1)){
                    peakNum.add((float)dataArr.get(i));
                }
            }
        }

        for(float f: peakNum){
            if(f>10){
                bigVal.add(f);
            }
        }
        return bigVal;
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
