package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by golden on 2/18/2016.
 */
public class SelectSessionActivity extends Activity {
    public LinearLayout session_container;
    public Button btn_test_session;
    public int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_select_session);

        session_container = (LinearLayout) findViewById(R.id.session_container);
        btn_test_session = (Button) findViewById(R.id.btn_test_session);

        session_container.removeAllViews();
        session_container.removeAllViewsInLayout();
        session_container.clearDisappearingChildren();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btn_test_session.getLayoutParams();

        ArrayList<String> sessionAry = SpeakerLoginActivity.sessionAry;
        for (int i = 0; i < sessionAry.size(); i++) {
            String[] ary = sessionAry.get(i).split(",");
            Button btn_session = new Button(SelectSessionActivity.this);
            btn_session.setId(Integer.parseInt(ary[1]));
            btn_session.setText(ary[3]);
            btn_session.setTextColor(Color.WHITE);
            btn_session.setTypeface(btn_test_session.getTypeface(), Typeface.BOLD);
            Drawable d = getResources().getDrawable(R.drawable.green_radius);
            btn_session.setBackgroundDrawable(d);
            btn_session.setLayoutParams(params);
            btn_session.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTestSession(v.getId());
                }
            });
            session_container.addView(btn_session);
        }

        Bundle extra = getIntent().getExtras();
        uid = extra.getInt("uid");

        btn_test_session.setVisibility(View.INVISIBLE);
    }

    // test session
    public void onTestSession(int sessionId) {
        Intent intent = new Intent(SelectSessionActivity.this, PresentTimeActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    // create new ars
    public void onCreateARS(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://q-ars.com/signin"));
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
