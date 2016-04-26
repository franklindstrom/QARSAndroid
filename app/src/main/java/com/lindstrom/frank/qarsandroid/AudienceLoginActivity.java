package com.lindstrom.frank.qarsandroid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.enumeration.ProfileField;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.regex.Pattern;

public class AudienceLoginActivity extends FragmentActivity implements OnConnectionFailedListener {
    public static RelativeLayout progress_pan;
    public static String deviceID, deviceInfo;
    public int param = 0;
    public final int REQUEST_CODE_RESOLVE_ERR = 9000;

    // for get connectivity information
    public ConnectivityManager cm;
    public NetworkInfo info;

    // for connect to server
    public Connection connection;
    public String server_url = "https://q-ars.com/api/account.php?";
    public String request_params;

    // fir facebook sign in
    public AccessToken facebookAccessToken;
    public LoginButton facebook_loginButton;
    public CallbackManager callbackManager;
    public String facebookFirstName, facebookLastName, facebookEmail, facebookBirthday, facebookGender, facebookCountry, facebookFriendsList;
    public int facebookFriendsCount;

    // for google plus sign in
    public GoogleApiClient mGoogleApiClient;
    public SignInButton google_signinButton;
    public final int RC_SIGN_IN = 9001;
    public String googlePlusFirstName, googlePlusLastName, googlePlusEmail, googlePlusGender, googlePlusBirthday;

    // for twitter login
    public Twitter twitter;
    public TwitterLoginButton twitter_loginButton;
    public String twitterName, twitterScreenName, twitterEmail, twitterLocation, twitterFriendsCount, twitterFollowersCount;

    // for linked in login
    public LinkedInOAuthService oAuthService;
    public LinkedInApiClientFactory factory;
    public LinkedInRequestToken liToken;
    public LinkedInApiClient client;
    public final String CONSUMER_KEY = "77gio39d6ggfoa";
    public final String CONSUMER_SECRET = "dKbuJDaCY2iv9wPI";
    public final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
    public final String OAUTH_CALLBACK_HOST = "litestcalback";
    public final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
    public String linkedinFirstName, linkedinLastName, linkedinEmail, linkedinLocation, linkedinConnectionNum;

    // for login with email
    public String emailId;

    // QR code scanned result
    public String scanned_result = "http://www.q-ars.com/polling.php?sesID=";

    // user id that is received when I logged in.
    public String user_id;

    public View login_facebook, login_google, login_linked, login_twitter, login_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_audience_login);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        info = cm.getActiveNetworkInfo();

        connection = new Connection();

        login_facebook = findViewById(R.id.login_facebook);
        login_google = findViewById(R.id.login_google);
        login_linked = findViewById(R.id.login_linked);
        login_twitter = findViewById(R.id.login_twitter);
        login_email = findViewById(R.id.login_email);
        progress_pan = (RelativeLayout) findViewById(R.id.connection_loading_layout);
        progress_pan.setVisibility(View.INVISIBLE);

        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        deviceInfo = android.os.Build.MODEL;
        deviceInfo = deviceInfo.replaceAll(" ", "");

        /**
         * for facebook sign in
         **/
        callbackManager = CallbackManager.Factory.create();
        facebook_loginButton = (LoginButton) findViewById(R.id.facebook_login_button);

        // set readable permissions to allow from facebook accounts
        facebook_loginButton.setReadPermissions(Arrays.asList("public_profile", "user_friends", "user_birthday", "user_about_me", "email", "user_location"));

        // login callback from facebook
        facebook_loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                progress_pan.setVisibility(View.VISIBLE);
                facebookAccessToken = loginResult.getAccessToken();
                // get user information from facebook login callback
                GraphRequest request = GraphRequest.newMeRequest(facebookAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        getFacebookData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,birthday,gender,location,friends");
                request.setParameters(parameters);
                request.executeAsync();

                LoginManager.getInstance().logOut();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        facebook_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param = 1;
            }
        });

        /**
         * for google plus sign in
         *
         * Configure sign-in to request the user's ID, email address, and basic profile. ID and
         * basic profile are included in DEFAULT_SIGN_IN.
         * */

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(new Scope(Scopes.PLUS_LOGIN)).requestEmail().build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Plus.API)
                .build();

        google_signinButton = (SignInButton) findViewById(R.id.google_login_button);
        google_signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param = 2;
                progress_pan.setVisibility(View.VISIBLE);
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        /**
         * for twitter login
         * */
        twitter_loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitter_loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                //TODO: Remove toast and use the TwitterSession's userID
                // The TwitterSession is also available through:
                TwitterSession session = result.data;
                // with your app's user model
                Twitter.getApiClient(session).getAccountService().verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> userResult) {
                        User user = userResult.data;
                        twitterName = user.name;
                        twitterScreenName = user.screenName;
                        twitterEmail = getDeviceAccount();
                        twitterLocation = user.location;
                        twitterFriendsCount = String.valueOf(user.friendsCount);
                        twitterFollowersCount = String.valueOf(user.followersCount);
                        new SendLoginDataToServer().execute();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
            }
        });

        twitter_loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param = 3;
            }
        });

        /**
         * for linked in login
         * */
        login_linked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param = 4;
                progress_pan.setVisibility(View.VISIBLE);
                oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(CONSUMER_KEY, CONSUMER_SECRET);
                factory = LinkedInApiClientFactory.newInstance(CONSUMER_KEY, CONSUMER_SECRET);
                liToken = oAuthService.getOAuthRequestToken(OAUTH_CALLBACK_URL);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(liToken.getAuthorizationUrl()));
                startActivity(i);
            }
        });

        /**
         * for login with email
         * */
        login_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                param = 5;
                EditText txt_mail_address = (EditText) findViewById(R.id.txt_user_email);
                emailId = txt_mail_address.getText().toString();
                if (emailId.length() > 0) {
                    progress_pan.setVisibility(View.VISIBLE);
                    new SendLoginDataToServer().execute();
                } else {
                    Toast.makeText(getApplicationContext(), "Please input mail address.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (info == null || !info.isAvailable()) {
            login_facebook.setEnabled(false);
            login_google.setEnabled(false);
            login_linked.setEnabled(false);
            login_twitter.setEnabled(false);
            login_email.setEnabled(false);
            facebook_loginButton.setEnabled(false);
            google_signinButton.setEnabled(false);
            twitter_loginButton.setEnabled(false);

            login_facebook.setAlpha(0.5f);
            login_google.setAlpha(0.5f);
            login_linked.setAlpha(0.5f);
            login_twitter.setAlpha(0.5f);
            login_email.setAlpha(0.5f);

            Toast.makeText(AudienceLoginActivity.this, "Please Check Internet Connection of Your Device!", Toast.LENGTH_SHORT).show();
        }
    }

    // get facebook account profile data
    public void getFacebookData(JSONObject object) {
        try {
            if (object.has("first_name"))
                facebookFirstName = object.getString("first_name");
            if (object.has("last_name"))
                facebookLastName = object.getString("last_name");
            if (object.has("email"))
                facebookEmail = object.getString("email");
            if (object.has("birthday"))
                facebookBirthday = object.getString("birthday");
            if (object.has("gender"))
                facebookGender = object.getString("gender");
            if (object.has("location"))
                facebookCountry = object.getJSONObject("location").getString("name");
            if (object.has("friends")) {
                JSONArray friends = object.getJSONObject("friends").getJSONArray("data");
                if (friends.length() > 0) {
                    for (int i = 0; i < friends.length(); i++) {
                        JSONObject obj = new JSONObject(friends.get(i).toString());
                        if (i == 0) facebookFriendsList = obj.getString("name");
                        else facebookFriendsList += "," + obj.getString("name");
                    }
                }

            }

            new SendLoginDataToServer().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // get data from google+ account
    public class getGooglePlusData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String sAccessToken = GoogleAuthUtil.getToken(AudienceLoginActivity.this, googlePlusEmail, "oauth2:" + Scopes.PLUS_LOGIN + " https://www.googleapis.com/auth/plus.profile.emails.read");
                URL url = new URL("https://www.googleapis.com/plus/v1/people/me");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer " + sAccessToken);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    InputStream inputStream = urlConnection.getInputStream();
                    return connection.convertInputStreamToString(inputStream);
                } else {
                    return "failed";
                }
            } catch (UserRecoverableAuthException userAuthEx) {
                // Start the user recoverable action using the intent returned by
                // getIntent()
                startActivityForResult(userAuthEx.getIntent(), RC_SIGN_IN);
            } catch (Exception e) {
                System.out.println("error is " + e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            System.out.println("result is " + result);
            if (result.equals("failed")) {
                Toast.makeText(AudienceLoginActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONObject json = new JSONObject(result);
                    googlePlusGender = json.getString("gender");
                    googlePlusBirthday = json.getString("birthday");
                    new SendLoginDataToServer().execute();
                } catch (JSONException e) {
                    System.out.println("error is " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    // get mail address from device
    public String getDeviceAccount() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(AudienceLoginActivity.this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }

        return null;
    }

    // send the login data to server
    public class SendLoginDataToServer extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            if (param == 1) {
                request_params = "socialType=fb&firstName=" + eatNull(facebookFirstName) + "&lastName=" + eatNull(facebookLastName) + "&gender=" + eatNull(facebookGender) + "&email=" + eatNull(facebookEmail) + "&dob=" + eatNull(facebookBirthday) + "&deviceID=" + eatNull(deviceID) + "&country=" + eatNull(facebookCountry).replaceAll(" ", "") + "&education=&relationship=&friends_count=" + eatNull(String.valueOf(facebookFriendsCount)) + "&followers_count=&profession=&device_info=" + eatNull(deviceInfo) + "&OS=Android&social_friend=" + eatNull(facebookFriendsList).replaceAll(" ", "");
            } else if (param == 2) {
                request_params = "socialType=gPlus&firstName=" + eatNull(googlePlusFirstName) + "&lastName=" + eatNull(googlePlusLastName) + "&gender=" + eatNull(googlePlusGender) + "&email=" + eatNull(googlePlusEmail) + "&dob=" + eatNull(googlePlusBirthday) + "&deviceID=" + eatNull(deviceID) + "&country=&education=&relationship=&friends_count=&followers_count=&profession=&device_info=" + eatNull(deviceInfo) + "&OS=Android&social_friend=";
            } else if (param == 3) {
                request_params = "socialType=twitter&firstName=" + eatNull(twitterName) + "&lastName=" + eatNull(twitterScreenName) + "&gender=&email=" + eatNull(twitterEmail) + "&dob=&deviceID=" + eatNull(deviceID) + "&country=" + eatNull(twitterLocation).replaceAll(" ", "") + "&education=&relationship=&friends_count=" + eatNull(twitterFriendsCount) + "&followers_count=" + eatNull(twitterFollowersCount) + "&profession=&device_info=" + eatNull(deviceInfo) + "&OS=Android&social_friend=";
            } else if (param == 4) {
                request_params = "socialType=linkedIn&firstName=" + eatNull(linkedinFirstName) + "&lastName=" + eatNull(linkedinLastName) + "&gender=&email=" + eatNull(linkedinEmail) + "&dob=&deviceID=" + eatNull(deviceID) + "&country=" + eatNull(linkedinLocation).replaceAll(" ", "") + "&education=&relationship=&friends_count=" + eatNull(linkedinConnectionNum) + "&followers_count=&profession=&device_info=" + eatNull(deviceInfo) + "&OS=Android&social_friend=";
            } else if (param == 5) {
                request_params = "socialType=email&firstName=&lastName=&gender=&email=" + eatNull(emailId) + "&dob=&deviceID=" + eatNull(deviceID) + "&country=&education=&relationship=&friends_count=&followers_count=&profession=&device_info=" + eatNull(deviceInfo) + "&OS=Android&social_friend=";
            }

            String response_ = connection.getResponse(server_url + request_params, request_params);
            try {
                JSONObject obj = new JSONObject(response_);
                return obj.getString("uid");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            user_id = result;
            progress_pan.setVisibility(View.INVISIBLE);
            param = 100;
            IntentIntegrator integrator = new IntentIntegrator(AudienceLoginActivity.this);
            integrator.initiateScan();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            String verifier = intent.getData().getQueryParameter("oauth_verifier");

            LinkedInAccessToken accessToken = oAuthService.getOAuthAccessToken(liToken, verifier);
            client = factory.createLinkedInApiClient(accessToken);

            Person profile = client.getProfileForCurrentUser(EnumSet.of(ProfileField.ID, ProfileField.FIRST_NAME, ProfileField.LAST_NAME, ProfileField.EMAIL_ADDRESS, ProfileField.DATE_OF_BIRTH, ProfileField.LOCATION_NAME, ProfileField.LOCATION_COUNTRY, ProfileField.CONNECTIONS, ProfileField.NUM_CONNECTIONS));
            linkedinFirstName = profile.getFirstName();
            linkedinLastName = profile.getLastName();
            linkedinEmail = profile.getEmailAddress();
            linkedinLocation = profile.getLocation().getName();
            linkedinConnectionNum = String.valueOf(profile.getNumConnections());
            new SendLoginDataToServer().execute();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();
                    // Get account information
                    String[] ary = account.getDisplayName().split(" ");
                    googlePlusFirstName = ary[0];
                    googlePlusLastName = ary[1];
                    googlePlusEmail = account.getEmail();
                    new getGooglePlusData().execute("https://www.googleapis.com/oauth2/v1/userinfo?access_token=");
                }
            } else {
                if (param == 1) {
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                } else if (param == 3) {
                    // Pass the activity result to the login button.
                    progress_pan.setVisibility(View.VISIBLE);
                    twitter_loginButton.onActivityResult(requestCode, resultCode, data);
                } else if (param == 100) {
                    IntentResult scan = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

                    if (scan != null) {
                        String contents = data.getStringExtra("SCAN_RESULT");
                        if (contents.contains(scanned_result)) {
                            Intent intent = new Intent(AudienceLoginActivity.this, RegisterActivity.class);
                            intent.putExtra("session_id", contents.split("=")[1]);
                            intent.putExtra("user_id", user_id);
                            startActivity(intent);
                            finish();
                        } else {
                            AlertDialog alert = new AlertDialog.Builder(AudienceLoginActivity.this)
                                    .setTitle("Q-ARS Questions says")
                                    .setMessage("No Questions are available")
                                    .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).create();
                            alert.show();
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        Toast.makeText(AudienceLoginActivity.this, "No result", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    // make null value to blank text
    public String eatNull(String value) {
        return value == null ? "" : value;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // TODO Auto-generated method stub
        progress_pan.setVisibility(View.INVISIBLE);
        System.out.println("connection failed");
        if (connectionResult.hasResolution()) {
            /**
             * The user clicked the sign-in button already. Start to resolve
             * connection errors. Wait until onConnected() to dismiss the
             * connection dialog.
             * */
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.disconnect();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}