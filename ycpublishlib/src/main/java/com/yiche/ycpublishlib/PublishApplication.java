package com.yiche.ycpublishlib;

import android.app.Application;

/**
 * Created by hanbo on 2018/1/10.
 */

public class PublishApplication extends Application {

    private static PublishApplication mApplication;

    public static PublishApplication getInstance() {
        return mApplication;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    public void onTrimMemory2(int level) {
    }

    public void exitApp(){

    }
}
