package com.lock.ui.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lock.Constants;
import com.lock.R;
import com.lock.gridview.enums.LockMode;
import com.lock.ui.base.BaseActivity;
import com.lock.util.PasswordUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by leo on 16/4/5.
 * 主activity
 */
public class MainActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.btn_setting)
    Button btnSetting;
    @BindView(R.id.btn_edit)
    Button btnEdit;
    @BindView(R.id.btn_verify)
    Button btnVerify;
    @BindView(R.id.btn_clear)
    Button btnClear;
    @BindView(R.id.radio_circle)
    RadioGroup rdCircle;
    @BindView(R.id.radio_arrow)
    RadioGroup rdArrow;
    private boolean showArrow = true;
    private boolean circleTop = true;

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
        rdArrow.setOnCheckedChangeListener(this);
        rdCircle.setOnCheckedChangeListener(this);
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
        intent.putExtra(Constants.INTENT_SECOND_ACTIVITY_KEY, mode);
        intent.putExtra(Constants.INTENT_CIRCLE_TOP, circleTop);
        intent.putExtra(Constants.INTENT_SHOW_ARROW, showArrow);
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

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.arrow_gone) {
            showArrow = false;
        } else if (checkedId == R.id.arrow_visible) {
            showArrow = true;
        } else if (checkedId == R.id.circle_bottom) {
            circleTop = false;
        } else {
            circleTop = true;
        }
    }
}
