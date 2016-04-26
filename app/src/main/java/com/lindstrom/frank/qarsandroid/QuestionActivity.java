package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
public class QuestionActivity extends Activity {
    public RelativeLayout progress_pan;
    public LinearLayout question_container;
    public TextView demo_title;
    public RadioGroup demo_group;
    public RadioButton demo_button;
    public Button btn_next_question, btn_try_again, btn_answer;

    public int session_id;
    public String server_url = "https://q-ars.com/polling.php?";
    public String question_request_param, answer_request_param, logout_request_param;
    public Connection connection;

    // for get connectivity information
    public ConnectivityManager cm;
    public NetworkInfo info;

    public String answer, question_id, user_id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_question);

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

        question_container = (LinearLayout) findViewById(R.id.question_container);
        demo_title = (TextView) findViewById(R.id.question_title);
        demo_group = (RadioGroup) findViewById(R.id.radio_group);
        demo_button = (RadioButton) findViewById(R.id.radio_answer);
        btn_next_question = (Button) findViewById(R.id.btn_next_question);
        btn_try_again = (Button) findViewById(R.id.btn_try_again);
        btn_answer = (Button) findViewById(R.id.btn_answer);
        progress_pan = (RelativeLayout) findViewById(R.id.connection_loading_layout);
        progress_pan.setVisibility(View.INVISIBLE);

        Bundle extra = getIntent().getExtras();
        user_id = extra.getString("user_id").toString();
        session_id = Integer.parseInt(extra.getString("session_id").toString());
        question_request_param = "get_qs&sesID=" + session_id + "&uuid=" + user_id;
        logout_request_param = "logout&sesID=" + session_id + "&uuid=" + user_id;

        demo_button.setVisibility(View.INVISIBLE);
        btn_next_question.setVisibility(View.INVISIBLE);
        btn_answer.setVisibility(View.INVISIBLE);
        new getQuestionTask().execute(server_url + question_request_param);
    }

    // get the next question
    public void onNextQuestion(View view) {
        question_container.removeAllViewsInLayout();
        progress_pan.setVisibility(View.VISIBLE);
        new getQuestionTask().execute(server_url + question_request_param);
    }

    // try again to get question from server
    public void onTryAgain(View view) {
        question_container.removeAllViewsInLayout();
        progress_pan.setVisibility(View.VISIBLE);
        new getQuestionTask().execute(server_url + question_request_param);
    }

    // vote to answer to server
    public void onAnswer(View view) {
        if (answer == null || answer.equals("")) {
            Toast.makeText(QuestionActivity.this, "Please check your answer", Toast.LENGTH_SHORT).show();
        } else {
            progress_pan.setVisibility(View.VISIBLE);
            answer_request_param = "sesID=" + session_id + "&qID=" + question_id + "&vote1=" + answer + "&uuid=" + user_id;
            new sendAnswerTask().execute(server_url + answer_request_param);
        }
    }

    // logout from the survey
    public void onLogout(View view) {
        progress_pan.setVisibility(View.VISIBLE);
        new onLogoutAsyncTask().execute(server_url + logout_request_param);
    }

    // to get the survey from the server
    private class getQuestionTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getResponse(urls[0], question_request_param);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress_pan.setVisibility(View.INVISIBLE);
            if (result != null) {
                if (!result.equals("failed")) {
                    try {
                        final JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 1 && json.getString("msg").equals("OK")) {
                            btn_next_question.setVisibility(View.INVISIBLE);
                            btn_try_again.setVisibility(View.INVISIBLE);
                            btn_answer.setVisibility(View.VISIBLE);
                            TextView question_title = new TextView(QuestionActivity.this);
                            question_title.setText(json.getString("question").toString());
                            question_title.setTextColor(Color.BLACK);
                            question_title.setTypeface(demo_title.getTypeface(), Typeface.BOLD);
                            question_title.setLayoutParams(demo_title.getLayoutParams());
                            question_container.addView(question_title);

                            RadioGroup answer_items = new RadioGroup(QuestionActivity.this);
                            answer_items.setLayoutParams(demo_group.getLayoutParams());
                            answer_items.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(RadioGroup group, int checkedId) {
                                    answer = "q" + checkedId;
                                    try {
                                        question_id = json.getString("qID");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            question_container.addView(answer_items);

                            JSONArray answers = json.getJSONArray("answers");
                            for (int j = 0; j < answers.length(); j++) {
                                if (!answers.getString(j).toString().equals("")) {
                                    RadioButton answer_ = new RadioButton(QuestionActivity.this);
                                    answer_.setText(answers.getString(j).toString());
                                    answer_.setLayoutParams(demo_button.getLayoutParams());
                                    answer_.setId(j + 1);

                                    answer_items.addView(answer_);
                                }
                            }
                            demo_title.setText("Select your answer");
                        } else {
                            demo_title.setText(json.getString("msg"));
                            btn_try_again.setVisibility(View.VISIBLE);
                            btn_next_question.setVisibility(View.INVISIBLE);
                            btn_answer.setVisibility(View.INVISIBLE);
                        }

                        demo_button.setVisibility(View.INVISIBLE);
                        demo_group.setVisibility(View.INVISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(QuestionActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuestionActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // to get the survey from the server
    private class sendAnswerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getResponse(urls[0], answer_request_param);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress_pan.setVisibility(View.INVISIBLE);
            if (result != null) {
                if (!result.equals("failed")) {
                    try {
                        btn_next_question.setVisibility(View.VISIBLE);
                        btn_try_again.setVisibility(View.INVISIBLE);
                        btn_answer.setVisibility(View.INVISIBLE);
                        final JSONObject json = new JSONObject(result);
                        demo_title.setText(json.getString("msg"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(QuestionActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuestionActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // logout from the server
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
                    Toast.makeText(QuestionActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QuestionActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
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