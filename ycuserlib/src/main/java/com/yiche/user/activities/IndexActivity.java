package com.yiche.user.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.yiche.user.R;
import com.yiche.ycbaselib.component.BaseActivity;
import com.yiche.ycbaselib.service.ServiceHost;

public class IndexActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circles_activity_index);
        TextView text = (TextView) findViewById(R.id.text);
        text.setText("已加载组件: "+ ServiceHost.getMountModules());
    }
}
