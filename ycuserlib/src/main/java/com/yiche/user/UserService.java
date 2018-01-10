package com.yiche.user;

import com.yiche.ycbaselib.service.IUserService;

/**
 * Created by hanbo on 2018/1/10.
 */

public class UserService implements IUserService {
    private static UserService mInstance = new UserService();

    public static UserService getInstance() {
        return mInstance;
    }


    @Override
    public String getUserName() {
        return "我的名字是易车";
    }
}
