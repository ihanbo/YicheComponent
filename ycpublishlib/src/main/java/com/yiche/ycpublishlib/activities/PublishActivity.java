package com.yiche.ycpublishlib.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yiche.ycbaselib.service.ICirclesService;
import com.yiche.ycbaselib.service.ServiceHost;
import com.yiche.ycpublishlib.R;

public class PublishActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.publish_activity_publish);
        findViewById(R.id.b1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICirclesService s = ServiceHost.getService(ICirclesService.class);
                if(s!=null){
                    Toast.makeText(PublishActivity.this, "name::"+s.getCirclesName(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(PublishActivity.this, "no service found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.b2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICirclesService s = ServiceHost.getService(ICirclesService.class);
                Toast.makeText(PublishActivity.this, s==null? "没有注册":"注册了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
