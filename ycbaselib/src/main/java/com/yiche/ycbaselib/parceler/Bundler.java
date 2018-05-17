package com.yiche.ycbaselib.parceler;

import android.os.Bundle;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class Bundler {
    static Gson gson = new Gson();

    public static Bundle putAsString(Bundle bundle,String key,Object obj){
        if(obj==null){
            return bundle;
        }

        String value = gson.toJson(obj);
        bundle.putString(key,value);
        return bundle;
    }

    public static<T>  T getByType(Bundle bundle,String key,Type type){
        if(bundle==null||type==null){
            return null;
        }
        String ss = bundle.getString(key);
        if(ss==null||ss.length()==0){
            return null;
        }
        return  (T)gson.fromJson(ss, type);
    }
}
