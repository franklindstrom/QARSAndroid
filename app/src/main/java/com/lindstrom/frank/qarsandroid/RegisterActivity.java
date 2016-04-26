package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by golden on 2/23/2016.
 */
public class RegisterActivity extends Activity {

    public RelativeLayout progress_pan;
    public LinearLayout survey_container;
    public TextView demo_title;
    public RadioGroup demo_group;
    public RadioButton demo_button;

    public int session_id;
    public String server_url = "https://q-ars.com/api/survey.php?";
    public String request_param;
    public Connection connection;

    // for get connectivity information
    public ConnectivityManager cm;
    public NetworkInfo info;

    public String[] answers = new String[]{"ans1", "ans2", "ans3", "ans4", "ans5"};
    public String answer, question_id, user_id;
    public JSONObject answer_data;
    public JSONArray answerAry;

    public String logout_request_param;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_register);

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        info = cm.getActiveNetworkInfo();

        connection = new Connection();

        // check the internet connection of device
        if (info == null || !info.isAvailable()) {
            AlertDialog alert = new AlertDialog.Builder(this)
                    .setTitle("Q-ARS Questions says")
                    .setMessage("Please check the internet connection of your device")
                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
            alert.show();
            return;
        }

        survey_container = (LinearLayout) findViewById(R.id.survey_container);
        demo_title = (TextView) findViewById(R.id.question_title);
        demo_group = (RadioGroup) findViewById(R.id.radio_group);
        demo_button = (RadioButton) findViewById(R.id.radio_answer);
        progress_pan = (RelativeLayout) findViewById(R.id.connection_loading_layout);

        Bundle extra = getIntent().getExtras();
        user_id = extra.getString("user_id").toString();
        session_id = Integer.parseInt(extra.getString("session_id").toString());
        request_param = "sesID=" + session_id;
        logout_request_param = "logout&sesID=" + session_id + "&uuid=" + user_id;

        new HttpAsyncTask().execute(server_url + request_param);
    }

    // to get the survey from the server
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getResponse(urls[0], request_param);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                progress_pan.setVisibility(View.INVISIBLE);
                answer_data = new JSONObject();
                answerAry = new JSONArray();
                if (!result.equals("failed")) {
                    try {
                        final JSONObject json = new JSONObject(result);
                        System.out.println(json.toString());
                        JSONArray ary = json.getJSONArray("questiondetails");
                        for (int i = 0; i < ary.length(); i++) {
                            final JSONObject question = ary.getJSONObject(i);

                            TextView question_title = new TextView(RegisterActivity.this);
                            question_title.setText(question.getString("question").toString());
                            question_title.setTextColor(Color.BLACK);
                            question_title.setTypeface(demo_title.getTypeface(), Typeface.BOLD);
                            question_title.setLayoutParams(demo_title.getLayoutParams());
                            survey_container.addView(question_title);

                            RadioGroup answer_items = new RadioGroup(RegisterActivity.this);
                            answer_items.setLayoutParams(demo_group.getLayoutParams());
                            answer_items.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    answer = answers[checkedId];
                                    try {
                                        question_id = question.getString("qid");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    JSONObject obj = new JSONObject();
                                    try {
                                        obj.put("uid", user_id);
                                        obj.put("qid", question_id);
                                        obj.put("answer", answer);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    answerAry.put(obj);
                                }
                            });
                            survey_container.addView(answer_items);

                            for (int j = 1; j <= 5; j++) {
                                if (!question.getString("ans" + j).toString().equals("")) {
                                    RadioButton answer_ = new RadioButton(RegisterActivity.this);
                                    answer_.setText(question.getString("ans" + j).toString());
                                    answer_.setLayoutParams(demo_button.getLayoutParams());
                                    answer_.setId(j - 1);

                                    answer_items.addView(answer_);
                                }
                            }
                        }

                        demo_button.setVisibility(View.INVISIBLE);
                        demo_group.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Done button clicking event
    public void onSurveyDone(View view) {
        progress_pan.setVisibility(View.VISIBLE);
        try {
            String survey_answer_url = "https://q-ars.com/api/surveyAnswers.php";
            answer_data.put("SurveyAnswers", answerAry);
            System.out.println(survey_answer_url + answer_data.toString());
            new SurveyAnswerTask().execute(survey_answer_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Skip button clicking event
    public void onSurveySkip(View vew) {
        gotoQuestionActivity();
    }

    // logout from server
    public void onLogout(View view) {
        progress_pan.setVisibility(View.VISIBLE);
        new onLogoutAsyncTask().execute("https://q-ars.com/polling.php?" + logout_request_param);
    }

    // to get the survey from the server
    private class onLogoutAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getResponse(urls[0], logout_request_param);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress_pan.setVisibility(View.INVISIBLE);
            if (result != null) {
                if (!result.equals("failed")) {
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // to get the survey from the server
    private class SurveyAnswerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getPostResponse(urls[0], answer_data.toString());
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                progress_pan.setVisibility(View.INVISIBLE);
                answer_data = new JSONObject();
                answerAry = new JSONArray();
                if (!result.equals("failed")) {
                    try {
                        final JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 1 && json.getString("msg").equals("OK")) {
                            gotoQuestionActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // go to question activity
    public void gotoQuestionActivity() {
        Intent intent = new Intent(RegisterActivity.this, QuestionActivity.class);
        System.out.println(String.valueOf(session_id));
        System.out.println(session_id);
        intent.putExtra("session_id", String.valueOf(session_id));
        intent.putExtra("user_id", user_id);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Q-ARS Questions Says")
                .setMessage("Are you sure to logout?")
                .setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progress_pan.setVisibility(View.VISIBLE);
                        new onLogoutAsyncTask().execute(server_url + logout_request_param);
                    }
                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        return;
                    }
                }).create();
        alert.show();
    }
}