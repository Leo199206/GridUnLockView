package com.leo.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.leo.Contants;
import com.leo.R;
import com.leo.gesturelibrary.enums.LockMode;
import com.leo.gesturelibrary.util.StringUtils;
import com.leo.ui.base.BaseActivity;
import com.leo.util.PasswordUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by leo on 16/4/5.
 * 主activity
 */
public class MainActivity extends BaseActivity implements RippleView.OnRippleCompleteListener {
    @Bind(R.id.rv_setting)
    RippleView rvSetting;
    @Bind(R.id.rv_edit)
    RippleView rvEdit;
    @Bind(R.id.rv_verify)
    RippleView rvVerify;
    @Bind(R.id.rv_clear)
    RippleView rvClear;

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    public void initView() {
        rvClear = (RippleView) findViewById(R.id.rv_clear);
        rvEdit = (RippleView) findViewById(R.id.rv_edit);
        rvSetting = (RippleView) findViewById(R.id.rv_setting);
        rvVerify = (RippleView) findViewById(R.id.rv_verify);
    }

    @Override
    public void initListener() {
        rvClear.setOnRippleCompleteListener(this);
        rvEdit.setOnRippleCompleteListener(this);
        rvSetting.setOnRippleCompleteListener(this);
        rvVerify.setOnRippleCompleteListener(this);
    }

    @Override
    public void initData() {

    }


    @Override
    public void onComplete(RippleView rippleView) {
        switch (rippleView.getId()) {
            case R.id.rv_clear:
                actionSecondActivity(LockMode.CLEAR_PASSWORD);
                break;
            case R.id.rv_edit:
                actionSecondActivity(LockMode.EDIT_PASSWORD);
                break;
            case R.id.rv_setting:
                actionSecondActivity(LockMode.SETTING_PASSWORD);
                break;
            case R.id.rv_verify:
                actionSecondActivity(LockMode.VERIFY_PASSWORD);
                break;
        }
    }

    /**
     * 跳转到密码处理界面
     */
    private void actionSecondActivity(LockMode mode) {
        if (mode != LockMode.SETTING_PASSWORD) {
            if (StringUtils.isEmpty(PasswordUtil.getPin(this))) {
                Toast.makeText(getBaseContext(), "请先设置密码", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, mode);
        startActivity(intent);
    }

}
