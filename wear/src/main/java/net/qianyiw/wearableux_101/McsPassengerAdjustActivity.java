package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class McsPassengerAdjustActivity extends Activity implements View.OnClickListener{

    GridViewPager pager;
    ImageView homeBT;
    Vibrator vibrator;
    MessageServer myMessage;
    BroadcastReceiver broadcastReceiver;
    String gestureRecognition = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcs_passenger_adjust);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pager = (GridViewPager) findViewById(R.id.pager);
        McsPassengerAdjustPagerAdapter pagerAdapter = new McsPassengerAdjustPagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

        homeBT = (ImageView)findViewById(R.id.home_button);
        homeBT.setOnClickListener(this);

        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                gestureRecognition = intent.getStringExtra("gesture_result");
                Log.v("gesture_result", gestureRecognition);
                updateUI(gestureRecognition);
            }
        };
    }

    private void updateUI(String s) {
        switch (s){
            case "SINGLE OUTSIDE":

                break;
            case "SINGLE INSIDE":

                break;
            case "DOUBLE OUTSIDE":

                break;
            case "DOUBLE INSIDE":
                vibrator.vibrate(50);
                myMessage.sendMessage("home");
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(getBaseContext(), gestureServicre.class));
        registerReceiver(broadcastReceiver, new IntentFilter(gestureServicre.BROADCAST_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        stopService(new Intent(getBaseContext(), gestureServicre.class));
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.home_button:
                vibrator.vibrate(50);
                myMessage.sendMessage("home");
//                intent = new Intent(this, MainMenu.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                finish();
                break;
        }
    }
}
