package net.qianyiw.wearableux_101;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Qianyi on 7/24/2016.
 */
public class McsPassengerAdjustFragment extends Fragment implements View.OnClickListener{

    MessageServer myMessage;
    int msg, adjust_level;

    int m1_lumbarVal, m1_lowBolsterVal, m1_backBolsterVal,
            m2_lumbarVal, m2_lowBolsterVal, m2_backBolsterVal,
            m3_lumbarVal, m3_lowBolsterVal, m3_backBolsterVal;

    String settingStatus;

    SharedPreferences adjustPref;
    SharedPreferences.Editor adjustEdit;
    public static final String MY_PREFS_NAME = "AdjustPrefsFile";

    ImageView seat, setting1, setting2, setting3, massage_high, massage_low, massage_off;
    ImageButton plus, minus;
    TextView nextBT, value, label;

    Vibrator vibrator;
    Typeface custom_font;

    public McsPassengerAdjustFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myMessage = new MessageServer(getActivity());
        myMessage.myApiClient.connect();
        msg = bundle.getInt("count");
        custom_font = Typeface.createFromAsset(getContext().getAssets(), "fonts/FordAntennaWGL-Medium.otf");

        adjustPref = this.getActivity().getSharedPreferences(MY_PREFS_NAME, getContext().MODE_PRIVATE);
        adjustEdit = this.getActivity().getSharedPreferences(MY_PREFS_NAME, getContext().MODE_PRIVATE).edit();
        adjust_level = adjustPref.getInt("adjust_level", 0);
        settingStatus = adjustPref.getString("setting_status", "M1");
        //initialSection(adjust_level);
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(msg==1)
        {
            View view = inflater.inflate(R.layout.mcs_massage_layout, container, false);
            Log.v("page received", "1");
            massage_high = (ImageView)view.findViewById(R.id.massage_high);
            massage_high.setOnClickListener(this);
            massage_low = (ImageView)view.findViewById(R.id.massage_low);
            massage_low.setOnClickListener(this);
            massage_off = (ImageView)view.findViewById(R.id.massage_off);
            massage_off.setOnClickListener(this);
            seat = (ImageView)view.findViewById(R.id.driver_seat);
            label = (TextView)view.findViewById(R.id.label);
            label.setTypeface(custom_font);
            return view;
        }
        else
        {
            View view = inflater.inflate(R.layout.mcs_passenger_adjust, container, false);
            seat = (ImageView)view.findViewById(R.id.seat);
            setting1 = (ImageView)view.findViewById(R.id.setting1);
            setting1.setOnClickListener(this);
            setting1.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    updateRecord(adjust_level,"M1");
                    settingStatus = "M1";
                    adjustEdit.putString("setting_status",settingStatus).commit();
                    initialSection(adjust_level, settingStatus);
                    Toast.makeText(getContext(), "M1 setting updated", 0).show();
                    return true;
                }
            });
            setting2 = (ImageView)view.findViewById(R.id.setting2);
            setting2.setOnClickListener(this);
            setting2.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateRecord(adjust_level,"M2");
                    settingStatus = "M2";
                    adjustEdit.putString("setting_status",settingStatus).commit();
                    initialSection(adjust_level, settingStatus);
                    Toast.makeText(getContext(), "M2 setting updated", 0).show();
                    return true;
                }
            });
            setting3 = (ImageView)view.findViewById(R.id.setting3);
            setting3.setOnClickListener(this);
            setting3.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateRecord(adjust_level,"M3");
                    settingStatus = "M3";
                    adjustEdit.putString("setting_status",settingStatus).commit();
                    initialSection(adjust_level, settingStatus);
                    Toast.makeText(getContext(), "M3 setting updated", 0).show();
                    return true;
                }
            });

            plus = (ImageButton)view.findViewById(R.id.plus);
            plus.setOnClickListener(this);
            minus = (ImageButton)view.findViewById(R.id.minus);
            minus.setOnClickListener(this);
            nextBT = (TextView)view.findViewById(R.id.nextBT);
            nextBT.setOnClickListener(this);
            nextBT.setTypeface(custom_font);
            value = (TextView)view.findViewById(R.id.value);
            value.setTypeface(custom_font);
            initialSection(adjust_level,settingStatus);
            return view;
        }
    }


    @Override
    public void onClick(View v) {
        int myVal;
        switch (v.getId())
        {

            case R.id.massage_high:
                seat.setImageResource(R.drawable.seat_massage);
                myMessage.sendMessage("massage high");
                massage_high.setImageResource(R.drawable.hi_select);
                massage_low.setImageResource(R.drawable.low_unselect);
                massage_off.setImageResource(R.drawable.off_unselect);
                break;
            case R.id.massage_low:

                seat.setImageResource(R.drawable.seat_massage);
                myMessage.sendMessage("massage low");
                massage_high.setImageResource(R.drawable.hi_unselect);
                massage_low.setImageResource(R.drawable.low_select);
                massage_off.setImageResource(R.drawable.off_unselect);
                break;

            case R.id.massage_off:
                seat.setImageResource(R.drawable.mcs_driver);
                myMessage.sendMessage("massage close");
                massage_high.setImageResource(R.drawable.hi_unselect);
                massage_low.setImageResource(R.drawable.low_unselect);
                massage_off.setImageResource(R.drawable.off_select);
                break;

            case R.id.nextBT:
                vibrator.vibrate(50);
                if(adjust_level<2)
                {
                    adjust_level++;
                }
                else
                {
                    adjust_level=0;
                }
                adjustEdit.putInt("adjust_level",adjust_level).commit();
                initialSection(adjust_level, settingStatus);
                break;
            case R.id.setting1:
                setting1.setImageResource(R.drawable.m1_select);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);
                settingStatus = "M1";
                adjustEdit.putString("setting_status",settingStatus).commit();
                initialSection(adjust_level, settingStatus);
                sendCommand(settingStatus);
                break;
            case R.id.setting2:
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_select);
                setting3.setImageResource(R.drawable.m3_unselect);
                settingStatus = "M2";
                adjustEdit.putString("setting_status",settingStatus).commit();
                initialSection(adjust_level, settingStatus);
                sendCommand(settingStatus);
                break;
            case R.id.setting3:
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_select);
                settingStatus = "M3";
                adjustEdit.putString("setting_status",settingStatus).commit();
                initialSection(adjust_level, settingStatus);
                sendCommand(settingStatus);
                break;
            case R.id.plus:
                vibrator.vibrate(50);
                myVal = Integer.parseInt(value.getText().toString());
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);
                if(myVal<10)
                {
                    myVal++;
                    if(adjust_level==0){
                        myMessage.sendMessage("lumbar:"+myVal); // lumbar
                    }
                    else if(adjust_level==1){
                        myMessage.sendMessage("low_bolster:"+myVal); // low_bolster
                    }
                    else if(adjust_level==2){
                        myMessage.sendMessage("high_bolster:"+myVal); // high_bolster
                    }
                }

                value.setText(myVal+"");
                break;
            case R.id.minus:
                vibrator.vibrate(50);
                myVal = Integer.parseInt(value.getText().toString());
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);
                if(myVal>0)
                {
                    myVal--;
                    if(adjust_level==0){
                        myMessage.sendMessage("lumbar:"+myVal); // lumbar
                    }
                    else if(adjust_level==1){
                        myMessage.sendMessage("low_bolster:"+myVal); // low_bolster
                    }
                    else if(adjust_level==2){
                        myMessage.sendMessage("high_bolster:"+myVal); // high_bolster
                    }
                }

                value.setText(myVal+"");
                break;

        }
    }

    private void sendCommand(String settingStatus) {
        switch (settingStatus)
        {
            case "M1":
                int m1_lumbarVal = adjustPref.getInt("m1_lumbarVal",0);
                int m1_lowBolsterVal = adjustPref.getInt("m1_lowBolsterVal",0);
                int m1_backBolsterVal = adjustPref.getInt("m1_backBolsterVal",0);
                myMessage.sendMessage("lumbar:"+m1_lumbarVal);
                myMessage.sendMessage("low_bolster:"+m1_lowBolsterVal);
                myMessage.sendMessage("high_bolster:"+m1_backBolsterVal);

                break;
            case "M2":
                int m2_lumbarVal = adjustPref.getInt("m2_lumbarVal",0);
                int m2_lowBolsterVal = adjustPref.getInt("m2_lowBolsterVal",0);
                int m2_backBolsterVal = adjustPref.getInt("m2_backBolsterVal",0);
                myMessage.sendMessage("lumbar:"+m2_lumbarVal);
                myMessage.sendMessage("low_bolster:"+m2_lowBolsterVal);
                myMessage.sendMessage("high_bolster:"+m2_backBolsterVal);
                break;
            case "M3":
                int m3_lumbarVal = adjustPref.getInt("m3_lumbarVal",0);
                int m3_lowBolsterVal = adjustPref.getInt("m3_lowBolsterVal",0);
                int m3_backBolsterVal = adjustPref.getInt("m3_backBolsterVal",0);
                myMessage.sendMessage("lumbar:"+m3_lumbarVal);
                myMessage.sendMessage("low_bolster:"+m3_lowBolsterVal);
                myMessage.sendMessage("high_bolster:"+m3_backBolsterVal);
                break;
        }
    }

    public void initialSection(int adjust_level, String settingStatus)
    {
        if (adjust_level==0)
        {
            seat.setImageResource(R.drawable.seat_massage);
            if(settingStatus.equals("M1"))
            {
                setting1.setImageResource(R.drawable.m1_select);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);

                m1_lumbarVal = adjustPref.getInt("m1_lumbarVal",0);
                value.setText(m1_lumbarVal+"");
            }
            else if(settingStatus.equals("M2"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_select);
                setting3.setImageResource(R.drawable.m3_unselect);

                m2_lumbarVal = adjustPref.getInt("m2_lumbarVal",0);
                value.setText(m2_lumbarVal+"");
            }
            else if(settingStatus.equals("M3"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_select);

                m3_lumbarVal = adjustPref.getInt("m3_lumbarVal",0);
                value.setText(m3_lumbarVal+"");
            }

        }
        else if (adjust_level==1)
        {
            seat.setImageResource(R.drawable.seat_low_bolster);
            if(settingStatus.equals("M1"))
            {
                setting1.setImageResource(R.drawable.m1_select);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);

                m1_lowBolsterVal = adjustPref.getInt("m1_lowBolsterVal",0);
                value.setText(m1_lowBolsterVal+"");
            }
            else if(settingStatus.equals("M2"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_select);
                setting3.setImageResource(R.drawable.m3_unselect);

                m2_lowBolsterVal = adjustPref.getInt("m2_lowBolsterVal",0);
                value.setText(m2_lowBolsterVal+"");
            }
            else if(settingStatus.equals("M3"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_select);

                m3_lowBolsterVal = adjustPref.getInt("m3_lowBolsterVal",0);
                value.setText(m3_lowBolsterVal+"");
            }
        }
        else if (adjust_level==2)
        {
            seat.setImageResource(R.drawable.mcs_back_bolster);
            if(settingStatus.equals("M1"))
            {
                setting1.setImageResource(R.drawable.m1_select);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_unselect);

                m1_backBolsterVal = adjustPref.getInt("m1_backBolsterVal",0);
                value.setText(m1_backBolsterVal+"");
            }
            else if(settingStatus.equals("M2"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_select);
                setting3.setImageResource(R.drawable.m3_unselect);

                m2_backBolsterVal = adjustPref.getInt("m2_backBolsterVal",0);
                value.setText(m2_backBolsterVal+"");
            }
            else if(settingStatus.equals("M3"))
            {
                setting1.setImageResource(R.drawable.m1_unselect);
                setting2.setImageResource(R.drawable.m2_unselect);
                setting3.setImageResource(R.drawable.m3_select);

                m3_backBolsterVal = adjustPref.getInt("m3_backBolsterVal",0);
                value.setText(m3_backBolsterVal+"");
            }
        }
    }

    public void updateRecord(int adjust_level, String settingStatus)
    {
        int val = Integer.parseInt(value.getText().toString());
        Log.v("update content",adjust_level+"--"+settingStatus+"--"+val);
        switch (adjust_level)
        {
            case 0:
                switch (settingStatus)
                {
                    case "M1":
                        adjustEdit.putInt("m1_lumbarVal",val).commit();
                        Log.v("m1_lumbarVal_saved",val+"");
                        break;
                    case "M2":
                        adjustEdit.putInt("m2_lumbarVal",val).commit();
                        break;
                    case "M3":
                        adjustEdit.putInt("m3_lumbarVal",val).commit();
                        break;
                }
                break;
            case 1:
                switch (settingStatus)
                {
                    case "M1":
                        adjustEdit.putInt("m1_lowBolsterVal",val).commit();
                        break;
                    case "M2":
                        adjustEdit.putInt("m2_lowBolsterVal",val).commit();
                        break;
                    case "M3":
                        adjustEdit.putInt("m3_lowBolsterVal",val).commit();
                        break;
                }
                break;
            case 2:
                switch (settingStatus)
                {
                    case "M1":
                        adjustEdit.putInt("m1_backBolsterVal",val).commit();
                        break;
                    case "M2":
                        adjustEdit.putInt("m2_backBolsterVal",val).commit();
                        break;
                    case "M3":
                        adjustEdit.putInt("m3_backBolsterVal",val).commit();
                        break;
                }
                break;
        }
    }
}
