/*
 * "THE BEER-WARE LICENSE"
 *
 * <pjhwang@gmail.com> wrote this file.  As long as you retain this
 * notice you can do whatever you want with this stuff. If we meet
 * some day, and you think this stuff is worth it, you can buy me a
 * beer in return.
 *
 * - Byung Ju Hwang.
 */

package com.bbingju.mymemo;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Parse 서비스 등록 수행
 */
public class MemoApplication extends Application {

    public static final String MEMO_GROUP_NAME = "ALL_MEMOS";

    @Override
    public void onCreate() {
        super.onCreate();

        // Register the data model to ParseObject.
        ParseObject.registerSubclass(Memo.class);

        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        Parse.initialize(this);
        ParseFacebookUtils.initialize(this);

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access.
        // defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
