package com.lindstrom.frank.qarsandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by golden on 2/12/2016.
 */
public class SpeakerLoginActivity extends Activity {
    public RelativeLayout progress_pan;

    public ConnectivityManager connectivityManager;
    public NetworkInfo networkInfo;

    public Connection connection;

    public String email_address, password;
    public String server_url = "https://q-ars.com/PowerpointApi/login.php?";
    public String request_params;
    public EditText txt_speaker_email, txt_speaker_password;

    public static ArrayList<String> sessionAry;

    public int uid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_speaker_login);

        sessionAry = new ArrayList<String>();
        sessionAry.clear();

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();

        connection = new Connection();

        txt_speaker_email = (EditText) findViewById(R.id.txt_speaker_email);
        txt_speaker_password = (EditText) findViewById(R.id.txt_speaker_password);
        progress_pan = (RelativeLayout) findViewById(R.id.connection_loading_layout);
        progress_pan.setVisibility(View.INVISIBLE);
    }

    public void onSpeakerSignIn(View v) {
        if (checkTextBoxes()) {
            if (networkInfo != null && networkInfo.isAvailable()) {
                progress_pan.setVisibility(View.VISIBLE);
                email_address = txt_speaker_email.getText().toString();
                password = txt_speaker_password.getText().toString();
                request_params = "userName=" + email_address + "&pwd=" + password;
                new HttpAsyncTask().execute(server_url + request_params);
            } else {
                Toast.makeText(SpeakerLoginActivity.this, "Please Check Your Internet Connection of You Device!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onSpeakerSignUp(View v) {
        if (networkInfo != null && networkInfo.isAvailable()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://q-ars.com/signup"));
            startActivity(intent);
        } else {
            Toast.makeText(SpeakerLoginActivity.this, "Please Check Your Internet Connection of You Device!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkTextBoxes() {
        if (txt_speaker_email.getText().toString().equals("")) {
            Toast.makeText(SpeakerLoginActivity.this, "Please Enter Your Email!", Toast.LENGTH_SHORT).show();
            return false;
        } else if (txt_speaker_password.getText().toString().equals("")) {
            Toast.makeText(SpeakerLoginActivity.this, "Please Enter Your Password!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return connection.getResponse(urls[0], request_params);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            progress_pan.setVisibility(View.INVISIBLE);
            if (result != null) {
                if (!result.equals("failed")) {
                    try {
                        sessionAry.clear();
                        JSONObject json = new JSONObject(result);
                        JSONArray jsonAry = json.getJSONArray("Accounts");
                        for (int i = 0; i < jsonAry.length(); i++) {
                            JSONObject jsonObj = jsonAry.getJSONObject(i);
                            String session = jsonObj.getString("ClientID") + "," + jsonObj.getString("uID") + "," + jsonObj.getString("uName") + "," + jsonObj.getString("sName");
                            sessionAry.add(session);
                            uid = Integer.parseInt(jsonObj.getString("uID"));
                        }
                        int status = json.getInt("status");
                        if (status == 1) {
                            Intent intent = new Intent(SpeakerLoginActivity.this, SelectSessionActivity.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                            finish();
                        } else {
                            AlertDialog alert = new AlertDialog.Builder(SpeakerLoginActivity.this)
                                    .setTitle("Q-ARS Says ...")
                                    .setMessage("Invalid mail address or password !")
                                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                            alert.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(SpeakerLoginActivity.this, "Connection Failed!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SpeakerLoginActivity.this, "Unable connect to server!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
