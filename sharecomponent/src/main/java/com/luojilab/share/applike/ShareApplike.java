package com.luojilab.share.applike;


import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.luojilab.componentservice.IApplicationLike;

/**
 * Created by mrzhang on 2017/6/15.
 */

public class ShareApplike implements IApplicationLike {

    private static ShareApplike mApp;
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


    public static ShareApplike getInstance() {
        return mApp;
    }

    public static void onCreate(Context context) {
        mApp = new ShareApplike();
        mApp.mApplication = (Application) context;

    }
}
