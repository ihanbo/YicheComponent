package com.yiche.circles;

import com.yiche.ycbaselib.service.ICirclesService;

/**
 * Created by hanbo on 2018/1/10.
 */

public class CirclesService implements ICirclesService {
    private static CirclesService mInstance = new CirclesService();

    public static CirclesService getInstance() {
        return mInstance;
    }

    @Override
    public String getCirclesName() {
        return "易车摇滚车圈";
    }
}
