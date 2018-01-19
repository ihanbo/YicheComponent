package com.yiche.ycbaselib.service;

import java.util.HashMap;
import java.util.Iterator;
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


    public static String getMountModules(){
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Class, Object>> iterator = mServices.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Class, Object> next = iterator.next();
            sb.append(next.getValue().getClass().getSimpleName());
            sb.append(" : ");
        }
        return sb.toString();
    }
}
