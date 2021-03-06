package com.myxh.coolshopping.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.myxh.coolshopping.R;
import com.myxh.coolshopping.ui.base.BaseActivity;
import com.myxh.coolshopping.util.SharePreferenceUtil;

/**
 * Created by asus on 2016/8/28.
 */
public class SplashActivity extends BaseActivity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isFirst = SharePreferenceUtil.getBoolean(SplashActivity.this,"isFirst",true);
                Intent intent = new Intent();
                if (isFirst) {
                    SharePreferenceUtil.putBoolean(SplashActivity.this,"isFirst",false);
                    intent.setClass(SplashActivity.this,GuideActivity.class);
                } else {
                    intent.setClass(SplashActivity.this,MainActivity.class);
                }
                startActivity(intent);
                finish();
            }
        },2000);
    }

}
