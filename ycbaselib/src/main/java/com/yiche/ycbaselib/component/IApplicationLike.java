package com.yiche.ycbaselib.component;

import android.app.Application;

/**
 *
 * Created by hanbo on 2018/1/10.
 */

public interface IApplicationLike {

    void onCreate(Application application,boolean debug);
    void exitApp();
    void onTrimMemory(int level);
}
