package com.yiche.circles;

import android.app.Application;
import android.util.Log;

import com.yiche.ycbaselib.component.IApplicationLike;
import com.yiche.ycbaselib.service.ICirclesService;
import com.yiche.ycbaselib.service.ServiceHost;

/**
 * Created by hanbo on 2018/1/8.
 */

public class CirclesAppLike implements IApplicationLike {
    @Override
    public void onCreate(Application application,boolean debug) {
        Log.i("yiche","车圈组件加载了，Application is :"+application.toString()+"  debug:"+debug);
        ServiceHost.addService(ICirclesService.class,CirclesService.getInstance());

    }

    @Override
    public void exitApp() {
        Log.i("yiche","车圈组件：exitApp:");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.i("yiche","车圈组件：onTrimMemory，level is :"+level);
    }
}
