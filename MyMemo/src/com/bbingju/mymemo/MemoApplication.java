package com.bbingju.mymemo;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MemoApplication extends Application {

    public static final String MEMO_GROUP_NAME = "ALL_MEMOS";

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Memo.class);

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Add your initialization code here
        Parse.initialize(this,
                "W8byMB6pZqbWaiC0WlyVcIPOWBSeDTG48grCzJMo",
                "NMa4pYUmY1R33W82RKFmXtHnWGQTyIpUOT9cUbyu");

        ParseFacebookUtils.initialize(this);

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
