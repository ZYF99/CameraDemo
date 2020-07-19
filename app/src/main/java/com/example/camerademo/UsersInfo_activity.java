package com.example.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * @BelongsProject: UsersInfo_activity
 * @BelongsPackage: com.example.administrator.myapplication
 * @Author: Administrator
 * @CreateTime: 2020-01-08 10:58
 */

public class UsersInfo_activity extends Activity {


    private TextView text_name, text_condition;
    private String name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userinfo_activity);
        init();
    }

    protected void init() {
        Intent intent = getIntent();
        name = intent.getStringExtra("Username");
        text_name = (TextView) findViewById(R.id.text_name);
        text_name.setText(name);
        text_condition = (TextView) findViewById(R.id.text_condition);
        text_condition.setText("在线");
    }

}
