package com.luojilab.componentservice;

import android.app.Application;

/**
 * Created by hanbo on 2018/1/7.
 */

public interface IApplicationLike {
    void onCreate();
    void onStop();
    void onTrimMemory(int level);
    Application getApplication();
}
