package com.yiche.circles.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yiche.circles.R;
import com.yiche.ycbaselib.component.BaseActivity;
import com.yiche.ycbaselib.service.IPublishService;
import com.yiche.ycbaselib.service.ServiceHost;

public class IndexActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circles_activity_index);
        findViewById(R.id.b1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IPublishService s = ServiceHost.getService(IPublishService.class);
                if(s==null){
                    Toast.makeText(IndexActivity.this, "组件未注册", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(IndexActivity.this, "发布条数："+s.getPublishNumbers(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
