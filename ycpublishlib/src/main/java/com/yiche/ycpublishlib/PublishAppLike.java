package com.yiche.ycpublishlib;

import android.app.Application;
import android.util.Log;

import com.yiche.ycbaselib.component.IApplicationLike;
import com.yiche.ycbaselib.service.IPublishService;
import com.yiche.ycbaselib.service.ServiceHost;

/**
 * Created by hanbo on 2018/1/10.
 */

public class PublishAppLike implements IApplicationLike {
    @Override
    public void onCreate(Application application) {
        Log.i("yiche","发布组件加载了，Application is :"+application.toString());
        ServiceHost.addService(IPublishService.class,PublishService.getInstance());

    }

    @Override
    public void onStop() {

    }

    @Override
    public void exitApp() {
        Log.i("yiche","发布组件：exitApp:");
    }

    @Override
    public void onTrimMemory(int level) {
        Log.i("yiche","发布组件：onTrimMemory，level is :"+level);
    }
}