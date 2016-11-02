package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;

public class ClimateControlActivity extends FragmentActivity implements View.OnClickListener {

    GridViewPager pager;
    ImageView backBT;
    Vibrator vibrator;

    MessageServer myMessage;
    SharedPreferences.Editor editor;
    SharedPreferences prefs;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate_control);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Bundle bundle = getIntent().getExtras();

        pager = (GridViewPager) findViewById(R.id.pager);
//        PagerAdapter pagerAdapter = new PagerAdapter(getFragmentManager());
        net.qianyiw.wearableux_101.PagerAdapter pagerAdapter = new net.qianyiw.wearableux_101.PagerAdapter(getFragmentManager());
        pager.setAdapter(pagerAdapter);

        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);

        backBT = (ImageView)findViewById(R.id.backButton);
        backBT.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

        editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId())
        {
            case R.id.backButton:
                vibrator.vibrate(50);
                myMessage.sendMessage("home");
                finish();
                break;
        }
    }
}
