package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by golden on 2/19/2016.
 */
public class PresentTimeActivity extends Activity {
    public EditText txt_presentation_time, txt_qa_time;
    public int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_present_time);

        txt_presentation_time = (EditText) findViewById(R.id.txt_presentation_time);
        txt_qa_time = (EditText) findViewById(R.id.txt_qa_time);

        Bundle extra = getIntent().getExtras();
        uid = extra.getInt("uid");
    }

    // test the session
    public void testSession(View view) {
        if (checkValuesOfTextBox()) {
            Intent intent = new Intent(PresentTimeActivity.this, TestSessionActivity.class);
            intent.putExtra("presentation_time", Integer.parseInt(txt_presentation_time.getText().toString()));
            intent.putExtra("QA_time", Integer.parseInt(txt_qa_time.getText().toString()));
            intent.putExtra("uid", uid);
            startActivity(intent);
            finish();
        }
    }

    // check the values of edit text
    public boolean checkValuesOfTextBox() {
        if (txt_presentation_time.getText().toString().equals("")) {
            Toast.makeText(PresentTimeActivity.this, "Please enter the Presentation time...", Toast.LENGTH_SHORT).show();
            return false;
        } else if (txt_qa_time.getText().toString().equals("")) {
            Toast.makeText(PresentTimeActivity.this, "Please enter the QA time...", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
