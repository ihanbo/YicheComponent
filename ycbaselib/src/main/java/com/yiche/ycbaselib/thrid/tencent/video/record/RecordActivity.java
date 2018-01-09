package com.yiche.ycbaselib.thrid.tencent.video.record;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.ugc.TXRecordCommon;
import com.yiche.ycbaselib.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Description: 腾讯小视频录制
 *
 * @author: Lyongwang
 * @date: 2018/1/9 上午10:14
 */
public class RecordActivity extends Activity {
    private static final String TAG = RecordActivity.class.getSimpleName();
    /**
     * 最大录制时长
     */
    public static final String RECORD_CONFIG_MAX_DURATION = "record_config_max_duration";
    /**
     * 最小录制时长
     */
    public static final String RECORD_CONFIG_MIN_DURATION = "record_config_min_duration";
    /**
     * 视频比例
     */
    public static final String RECORD_CONFIG_ASPECT_RATIO = "record_config_aspect_ratio";
    /**
     * 视频清晰度 标清/高清/超清
     */
    public static final String RECORD_CONFIG_RECOMMEND_QUALITY = "record_config_recommend_quality";
    /**
     * 横竖屏
     */
    public static final String RECORD_CONFIG_HOME_ORIENTATION = "record_config_home_orientation";
    /**
     * 自定义清晰度-录制分辨率
     */
    public static final String RECORD_CONFIG_RESOLUTION = "record_config_resolution";
    /**
     * 自定义清晰度-码率
     */
    public static final String RECORD_CONFIG_BITE_RATE = "record_config_bite_rate";
    /**
     * 自定义清晰度-帧率
     */
    public static final String RECORD_CONFIG_FPS = "record_config_fps";
    /**
     * 自定义清晰度-关键帧间隔
     */
    public static final String RECORD_CONFIG_GOP = "record_config_gop";
    /**
     * 录制完成是否直接编辑
     */
    public static final String RECORD_CONFIG_NEED_EDITER = "record_config_go_editer";
    private int mMinDuration;
    private int mMaxDuration;
    private int mAspectRatio;
    private int mRecommendQuality;
    private boolean mNeedEditer;

    @IntDef({TXRecordCommon.VIDEO_ASPECT_RATIO_1_1,
            TXRecordCommon.VIDEO_ASPECT_RATIO_3_4,
            TXRecordCommon.VIDEO_ASPECT_RATIO_9_16})
    @Retention(RetentionPolicy.SOURCE)
    private @interface RatioType {
    }

    @IntDef({TXRecordCommon.VIDEO_QUALITY_LOW,
            TXRecordCommon.VIDEO_QUALITY_MEDIUM,
            TXRecordCommon.VIDEO_QUALITY_HIGH})
    @Retention(RetentionPolicy.SOURCE)
    private @interface QualityType {
    }

    /**
     * @param context Context
     * @param maxDuration 最大录制时长
     * @param minDuration 最小录制时长
     * @param aspectRatio 录制屏幕比例
     * @param videoQuality 录制清晰度
     * @param needEditer 录制完是否进入编辑
     */
    public static void start(Context context, int maxDuration, int minDuration, @RatioType int aspectRatio, @QualityType int videoQuality, boolean needEditer) {
        Intent intent = new Intent(context, RecordActivity.class);
        intent.putExtra(RECORD_CONFIG_MIN_DURATION, minDuration);
        intent.putExtra(RECORD_CONFIG_MAX_DURATION, maxDuration);
        intent.putExtra(RECORD_CONFIG_ASPECT_RATIO, aspectRatio);
        intent.putExtra(RECORD_CONFIG_RECOMMEND_QUALITY, videoQuality);
        intent.putExtra(RECORD_CONFIG_NEED_EDITER, needEditer);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.base_activity_record);

        initViews();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        if (intent == null) {
            TXCLog.e(TAG, "intent is null");
            return;
        }
        mMinDuration = intent.getIntExtra(RECORD_CONFIG_MIN_DURATION, 5 * 1000);
        mMaxDuration = intent.getIntExtra(RECORD_CONFIG_MAX_DURATION, 60 * 1000);
        mAspectRatio = intent.getIntExtra(RECORD_CONFIG_ASPECT_RATIO, TXRecordCommon.VIDEO_ASPECT_RATIO_9_16);
        mRecommendQuality = intent.getIntExtra(RECORD_CONFIG_RECOMMEND_QUALITY, TXRecordCommon.VIDEO_QUALITY_MEDIUM);
        mNeedEditer = intent.getBooleanExtra(RECORD_CONFIG_NEED_EDITER, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (hasPermission()) {
            startCameraPreview();
        }
    }

    private void startCameraPreview() {

    }

    private boolean hasPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            List<String> permissions = new ArrayList<>();
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
//                permissions.add(Manifest.permission.CAMERA);
//            }
//            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
//                permissions.add(Manifest.permission.RECORD_AUDIO);
//            }
//            if (permissions.size() != 0) {
//                ActivityCompat.requestPermissions(this,
//                        permissions.toArray(new String[0]),
//                        100);
//                return false;
//            }
//        }

        return true;
    }

    private void initViews() {

    }

}
