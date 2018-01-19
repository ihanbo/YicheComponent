package com.yiche.ycpublishlib.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yiche.ycbaselib.service.ICirclesService;
import com.yiche.ycbaselib.service.IUserService;
import com.yiche.ycbaselib.service.ServiceHost;
import com.yiche.ycpublishlib.BuildConfig;
import com.yiche.ycpublishlib.PublishApplication;
import com.yiche.ycpublishlib.R;

public class PublishActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_activity_publish);
        TextView textView = (TextView) findViewById(R.id.publish_textview);
        textView.setText("发布界面 \n 已加载组件："+ServiceHost.getMountModules());
        findViewById(R.id.b1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICirclesService s = ServiceHost.getService(ICirclesService.class);
                if(s!=null){
                    Toast.makeText(PublishActivity.this, "name::"+s.getCirclesName()+" BuildConfig:"+ BuildConfig.DEBUG, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PublishActivity.this, "no service found"+" BuildConfig:"+ BuildConfig.DEBUG, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.b2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IUserService s = ServiceHost.getService(IUserService.class);
                PublishApplication.getInstance().exitApp();
                Toast.makeText(PublishActivity.this, s==null? "没有注册":"用户姓名："+s.getUserName(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
