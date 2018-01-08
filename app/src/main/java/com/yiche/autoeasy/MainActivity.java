package com.yiche.autoeasy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Fragment fragment;
    FragmentTransaction ft;

    Button installReadBookBtn;
    Button uninstallReadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        installReadBookBtn = (Button) findViewById(R.id.install_share);
        uninstallReadBtn = (Button) findViewById(R.id.uninstall_share);
        installReadBookBtn.setOnClickListener(this);
        uninstallReadBtn.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Toast.makeText(this, data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
        }
    }
}
