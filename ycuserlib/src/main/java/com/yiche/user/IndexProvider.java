package com.yiche.user;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.yiche.ycbaselib.service.IUserService;
import com.yiche.ycbaselib.service.ServiceHost;

/**
 * 无侵入初始化
 * Created by hanbo on 2018/1/10.
 */

public class IndexProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        Log.i("hh","用户组件已注册，Application is :"+getContext());
        ServiceHost.addService(IUserService.class, UserService.getInstance());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
