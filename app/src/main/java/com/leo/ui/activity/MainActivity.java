package com.leo.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.leo.Contants;
import com.leo.R;
import com.leo.gesturelibrary.enums.LockMode;
import com.leo.ui.base.BaseActivity;
import com.leo.util.PasswordUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by leo on 16/4/5.
 * 主activity
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_edit)
    Button btnEdit;
    @BindView(R.id.btn_verify)
    Button btnVerify;
    @BindView(R.id.btn_clear)
    Button btnClear;

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    public void initView() {
    }

    @Override
    public void initListener() {
        btnClear.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
        btnVerify.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }


    /**
     * 跳转到密码处理界面
     */
    private void actionSecondActivity(LockMode mode) {
        if (mode != LockMode.SETTING_PASSWORD) {
            if (TextUtils.isEmpty(PasswordUtil.getPin())) {
                Toast.makeText(getBaseContext(), "请先设置密码", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(Contants.INTENT_SECONDACTIVITY_KEY, mode);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_clear:
                actionSecondActivity(LockMode.CLEAR_PASSWORD);
                break;
            case R.id.btn_edit:
                actionSecondActivity(LockMode.EDIT_PASSWORD);
                break;
            case R.id.btn_setting:
                actionSecondActivity(LockMode.SETTING_PASSWORD);
                break;
            case R.id.btn_verify:
                actionSecondActivity(LockMode.VERIFY_PASSWORD);
                break;
        }
    }
}
