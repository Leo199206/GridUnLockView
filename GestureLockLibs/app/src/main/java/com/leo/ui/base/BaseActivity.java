package com.leo.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.leo.util.SlidrUtil;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrListener;

/**
 * Created by leo on 16/4/6.
 * 基类
 */
public abstract class BaseActivity extends AppCompatActivity implements SlidrListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeInitView();
        initView();
        initListener();
        initData();
        Slidr.attach(this, SlidrUtil.getConfig(this, this));
    }

    public abstract void beforeInitView();

    public abstract void initView();

    public abstract void initListener();

    public abstract void initData();


    /**
     * 以下4个方法为手势返回上一页监听回调，子类如需要用到，重写即可
     */
    @Override
    public void onSlideStateChanged(int state) {

    }

    @Override
    public void onSlideChange(float percent) {

    }

    @Override
    public void onSlideOpened() {

    }

    @Override
    public void onSlideClosed() {

    }
}
