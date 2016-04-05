package com.leo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.leo.gesturelibray.enums.LockMode;
import com.leo.gesturelibray.util.StringUtils;
import com.leo.util.PasswordUtil;

/**
 * Created by leo on 16/4/5.
 */
public class MainActivity extends AppCompatActivity implements RippleView.OnRippleCompleteListener {
    private RippleView rv_clear, rv_setting, rv_edit, rv_verify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv_clear = (RippleView) findViewById(R.id.rv_clear);
        rv_edit = (RippleView) findViewById(R.id.rv_edit);
        rv_setting = (RippleView) findViewById(R.id.rv_setting);
        rv_verify = (RippleView) findViewById(R.id.rv_verify);
        rv_clear.setOnRippleCompleteListener(this);
        rv_edit.setOnRippleCompleteListener(this);
        rv_setting.setOnRippleCompleteListener(this);
        rv_verify.setOnRippleCompleteListener(this);
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
}
