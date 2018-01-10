package com.yiche.ycpublishlib;

import com.yiche.ycbaselib.service.IPublishService;

/**
 * Created by hanbo on 2018/1/10.
 */

public class PublishService implements IPublishService {

    private static PublishService mInstance = new PublishService();

    public static PublishService getInstance() {
        return mInstance;
    }
    @Override
    public int getPublishNumbers() {
        return 2008;
    }
}
