package net.qianyiw.wearableux_101;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

public class Dest_SetActivity extends Activity implements View.OnClickListener {

    ImageView back_icon, voice_command;
    protected static final int REQUEST_OK = 1;
    Vibrator vibrator;
    MessageServer myMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dest__set);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        voice_command = (ImageView) findViewById(R.id.voice_command);
        voice_command.setOnClickListener(this);
        back_icon = (ImageView) findViewById(R.id.back);
        back_icon.setOnClickListener(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        myMessage = new MessageServer(this);
        myMessage.myApiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("voice result", "here");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_OK  && resultCode==RESULT_OK) {
            ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getBaseContext(), "your commend: "+thingsYouSaid.get(0), Toast.LENGTH_SHORT).show();
            myMessage.sendMessage(thingsYouSaid.get(0));
            Intent intent;
            if(thingsYouSaid.get(0).equals("back"))
            {
                intent = new Intent(this, MainMenu.class);
                startActivity(intent);
            }
        }
        else{
            Toast.makeText(getBaseContext(), "your commend without wifi: Walmart", Toast.LENGTH_SHORT).show();
            myMessage.sendMessage("Walmart");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.voice_command:
                vibrator.vibrate(50);
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
                try {
                    startActivityForResult(i, REQUEST_OK);
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error initializing speech to text engine.", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.back:
                vibrator.vibrate(50);
                myMessage.sendMessage("back");
                finish();
                break;
        }
    }
}
