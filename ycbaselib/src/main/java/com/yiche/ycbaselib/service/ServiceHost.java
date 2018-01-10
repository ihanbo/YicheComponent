package com.yiche.ycbaselib.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanbo on 2018/1/10.
 */

public class ServiceHost {
    private static Map<Class,Object> mServices = new HashMap<>(10);


    public static<T>  T getService(Class<T> clazz){
        return (T)mServices.get(clazz);
    }

    public static<T>  void addService(Class<T> clazz, T instance) {
        mServices.put(clazz,instance);
    }
}
