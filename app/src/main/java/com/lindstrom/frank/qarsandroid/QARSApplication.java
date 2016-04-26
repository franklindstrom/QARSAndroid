package com.lindstrom.frank.qarsandroid;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by golden on 2/21/2016.
 */
public class QARSApplication extends Application {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "0yCLlrA5aLmfiZvHY4IEoeR7u";
    private static final String TWITTER_SECRET = "VyymJBDRueZwmOcP96N2jdbWPQkDYjOue8b2ZgvnK21QgycH3e";

    @Override
    public void onCreate() {
        MultiDex.install(this);
        super.onCreate();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }
}
