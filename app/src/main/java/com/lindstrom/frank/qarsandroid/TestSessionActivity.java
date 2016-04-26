package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by golden on 2/19/2016.
 */
public class TestSessionActivity extends Activity {
    public Connection connection;

    public Timer presentationTimer, QATimer;
    public TextView time_text;
    public Button btn_play, btn_pause, btn_prev, btn_next;
    public ImageView btn_lamp;
    public EditText txt_go_to_slide;

    public String server_url = "http://q-ars.com:8080/QarsServ/Qars?wsdl";
    public String request_param;
    public int presentation_time, QA_time, uid;
    public boolean stop_time, lamp_on;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_test_session);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        time_text = (TextView) findViewById(R.id.time_text);
        btn_play = (Button) findViewById(R.id.btn_play);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_prev = (Button) findViewById(R.id.btn_prev);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_lamp = (ImageView) findViewById(R.id.btn_lamp);
        txt_go_to_slide = (EditText) findViewById(R.id.txt_go_to_slide);

        Bundle extra = getIntent().getExtras();
        presentation_time = onConvertToSecond(extra.getInt("presentation_time"));
        QA_time = onConvertToSecond(extra.getInt("QA_time"));
        uid = extra.getInt("uid");
        stop_time = false;
        lamp_on = true;

        time_text.setText(onConvertToMinute(presentation_time));

        connection = new Connection();

        onCountDownTime();
    }

    public void onCountDownTime() {
        presentationTimer = new Timer();
        QATimer = new Timer();

        presentationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        presentation_time--;
                        if (presentation_time < 0) {
                            presentationTimer.cancel();
                        } else {
                            time_text.setText(onConvertToMinute(presentation_time));
                        }
                    }
                });
            }
        }, 1000, 1000);

        QATimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        QA_time--;
                        if (QA_time < 0) QATimer.cancel();
                    }
                });
            }
        }, 1000, 1000);

        time_text.setTextColor(Color.RED);
    }

    // edit time
    public void onEditTime(View view) {
        presentationTimer.cancel();
        QATimer.cancel();
        time_text.setTextColor(Color.GRAY);
        AlertDialog.Builder builder = new AlertDialog.Builder(TestSessionActivity.this);
        final EditText pre_time = new EditText(TestSessionActivity.this);
        final EditText qa_time = new EditText(TestSessionActivity.this);
        pre_time.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qa_time.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        pre_time.setHint("Presentation Time");
        qa_time.setHint("QA Time");
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(pre_time);
        ll.addView(qa_time);
        builder.setTitle("Change Time");
        builder.setView(ll);
        builder.setCancelable(false);
        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presentation_time = onConvertToSecond(Integer.parseInt(pre_time.getText().toString().equals("") ? "0" : pre_time.getText().toString()));
                QA_time = onConvertToSecond(Integer.parseInt(qa_time.getText().toString().equals("") ? "0" : qa_time.getText().toString()));
                onCountDownTime();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onCountDownTime();
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    // control timer - stop and start
    public void onControlTime(View view) {
        if (!stop_time) {
            stop_time = true;
            presentationTimer.cancel();
            QATimer.cancel();

            time_text.setTextColor(Color.GRAY);
        } else {
            stop_time = false;
            onCountDownTime();
        }
    }

    // control session
    public void onControlSession(View view) {
        switch (view.getId()) {
            case R.id.btn_play:
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.INVISIBLE);
                request_param = "PS";
                new HttpAsyncTask().execute(server_url);
                break;
            case R.id.btn_pause:
                btn_pause.setVisibility(View.INVISIBLE);
                btn_play.setVisibility(View.VISIBLE);
                request_param = "SS";
                new HttpAsyncTask().execute(server_url);
                break;
            case R.id.btn_prev:
                request_param = "PV";
                new HttpAsyncTask().execute(server_url);
                break;
            case R.id.btn_next:
                request_param = "NT";
                new HttpAsyncTask().execute(server_url);
                break;
        }
    }

    // go to slide
    public void onGoToSlide(View view) {
        if (txt_go_to_slide.getText().toString().equals("")) {
            Toast.makeText(TestSessionActivity.this, "Please enter the slide number", Toast.LENGTH_SHORT).show();
        } else {
            request_param = "GO-" + txt_go_to_slide.getText().toString();
            new HttpAsyncTask().execute(server_url);
        }
    }

    // turn on or off light
    public void onTurnOnOffLight(View view) {
        if (lamp_on) {
            lamp_on = false;
            btn_lamp.setImageResource(R.drawable.lamp_image_dark);
            request_param = "BS";
            new HttpAsyncTask().execute(server_url);
        } else {
            lamp_on = true;
            btn_lamp.setImageResource(R.drawable.lamp_image_light);
            request_param = "UBS";
            new HttpAsyncTask().execute(server_url);
        }
    }

    // convert minute to second
    public int onConvertToSecond(int min) {
        return min * 60;
    }

    // convert second to minute and second
    public String onConvertToMinute(int sec) {
        int min = sec / 60;
        int second = sec % 60;
        return (min < 10 ? "0" + min : min) + " : " + (second < 10 ? "0" + second : second) + " Min";
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.connectToSoapServer(urls[0], uid, request_param);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            System.out.println("result is " + result);
        }
    }

    @Override
    public void onBackPressed() {
        presentationTimer.cancel();
        QATimer.cancel();
        finish();
    }
}
