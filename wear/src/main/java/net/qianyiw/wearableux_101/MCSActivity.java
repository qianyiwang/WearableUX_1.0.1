package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class MCSActivity extends Activity implements View.OnClickListener{

    GridViewPager pager;
    ImageView backBT;
    Vibrator vibrator;
    MessageServer myMessage;
    BroadcastReceiver broadcastReceiver;
    String gestureRecognition = "";
    int messageIdx = 0;
    int settingIdx = 0;
    MCS_PagerAdapter pagerAdapter;
    MCS_Fragment mcs_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcs);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        pager = (GridViewPager) findViewById(R.id.pager);
        pagerAdapter = new MCS_PagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);

        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        backBT = (ImageView)findViewById(R.id.backButton);
        backBT.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, gestureServicre.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, gestureServicre.class));

    }

    @Override
    public void onClick(View v) {

        Intent intent;
        switch (v.getId()) {
            case R.id.backButton:
                vibrator.vibrate(50);
                myMessage.sendMessage("home");
                finish();
                break;
        }
    }
}
