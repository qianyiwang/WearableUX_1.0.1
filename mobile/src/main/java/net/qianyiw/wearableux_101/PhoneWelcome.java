package net.qianyiw.wearableux_101;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PhoneWelcome extends AppCompatActivity implements View.OnClickListener{

    View welcome_view;
    View background_color;
    int animationDuration = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_welcome);

        welcome_view = (View)findViewById(R.id.welcome_view);

        welcome_view.animate()
                .setDuration(animationDuration)
                .alpha(1)
                .rotation(360)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Intent intent = new Intent(getApplication(), PhoneMainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_ltr, R.anim.out_ltr);

                    }
                });
    }

    @Override
    public void onClick(View v) {

    }
}
