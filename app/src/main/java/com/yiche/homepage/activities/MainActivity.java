package com.yiche.homepage.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yiche.homepage.R;
import com.yiche.ycbaselib.service.ServiceHost;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_activity_main);
        TextView text = (TextView) findViewById(R.id.text);
        text.setText("已加载组件: "+ServiceHost.getMountModules());
    }
}
