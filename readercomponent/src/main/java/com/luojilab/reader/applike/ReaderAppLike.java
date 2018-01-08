package com.luojilab.reader.applike;

import android.app.Application;
import android.content.Context;

import com.luojilab.componentservice.IApplicationLike;

/**
 * Created by mrzhang on 2017/6/15.
 */

public class ReaderAppLike implements IApplicationLike {

    private static ReaderAppLike mApp;
    private Application mApplication;

    @Override
    public void onCreate() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public Application getApplication() {
        return mApplication;
    }

    public static ReaderAppLike getInstance(){
        return mApp;
    }


    public static void onCreate(Context context) {
        mApp = new ReaderAppLike();
        mApp.mApplication = (Application) context;
        mApp.onCreate();
    }
}
