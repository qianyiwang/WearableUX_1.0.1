package net.qianyiw.wearableux_101;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends Activity implements View.OnClickListener{

    View backgournd_view, ford_logo;
    Boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        backgournd_view = findViewById(R.id.background_view);
        backgournd_view.setOnClickListener(this);
        ford_logo = findViewById(R.id.ford_logo);
        ford_logo.setOnClickListener(this);

        ford_logo.animate()
                .setDuration(1500)
                .alpha(1)
                .rotation(360)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        if(!clicked) {
                            Intent intent = new Intent(getBaseContext(), MainMenu.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_ltr, R.anim.out_ltr);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId())
        {
            case R.id.ford_logo:
                clicked = true;
                ford_logo.animate().cancel();
                intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_ltr, R.anim.out_ltr);
                break;
            case  R.id.background_view:
                clicked = true;
                ford_logo.animate().cancel();
                intent = new Intent(getBaseContext(), MainMenu.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_ltr, R.anim.out_ltr);
                break;
        }
    }
}
