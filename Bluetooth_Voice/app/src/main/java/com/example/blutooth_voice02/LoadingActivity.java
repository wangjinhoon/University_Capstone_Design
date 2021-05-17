package com.example.blutooth_voice02;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class LoadingActivity extends Activity {
    ImageView load;
    AnimationDrawable loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);

        load = (ImageView)findViewById(R.id.loading);
        loading = (AnimationDrawable)load.getDrawable();

        load.setImageResource(R.drawable.haruloading);
        loading = (AnimationDrawable)load.getDrawable();
        loading.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){;
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }, 4500);
    }

    public void onPause(){
        super.onPause();
        finish();
    }
}