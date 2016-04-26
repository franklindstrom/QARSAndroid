package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Created by golden on 2/11/2016.
 */
public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_main);
    }
    public void onButtonClick(View v) {
        if (v.getId() == R.id.btn_speaker_login) {
            Intent intent = new Intent(MainActivity.this, SpeakerLoginActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_audience_login) {
            Intent intent = new Intent(MainActivity.this, AudienceLoginActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}