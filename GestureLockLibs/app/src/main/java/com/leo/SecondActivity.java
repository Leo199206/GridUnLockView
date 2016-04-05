package com.leo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.gesturelibray.CustomLockView;
import com.leo.gesturelibray.enums.LockMode;
import com.leo.util.PasswordUtil;

import static com.leo.gesturelibray.enums.LockMode.CLEAR_PASSWORD;
import static com.leo.gesturelibray.enums.LockMode.SETTING_PASSWORD;

public class SecondActivity extends Activity {
    private CustomLockView lockView;
    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();
        initData();

    }

    /**
     * 初始化View
     */
    private void initView() {
        lockView = (CustomLockView) findViewById(R.id.lv_lock);
        tv_text = (TextView) findViewById(R.id.tv_text);
        lockView.setShow(true);//不显示绘制方向
        lockView.setErrorTimes(3);//允许最大输入次数
        lockView.setPasswordMinLength(4);//密码最少位数
        lockView.setOnCompleteListener(onCompleteListener);
        lockView.setSaveLockKey(Contants.PASS_KEY);
    }


    /**
     * 密码输入模式
     */
    private void setLockMode(LockMode mode, String password, String msg) {
        lockView.setMode(mode);
        lockView.setErrorTimes(3);
        if (mode != SETTING_PASSWORD) {
            lockView.setOldPassword(password);
        }
        tv_text.setText(msg);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        LockMode lockMode = (LockMode) getIntent().getSerializableExtra(Contants.INTENT_SECONDACTIVITY_KEY);
        setLockMode(lockMode);
    }

    public void onBack(View view) {
        onBackPressed();
    }

    /**
     * 密码输入监听
     */
    CustomLockView.OnCompleteListener onCompleteListener = new CustomLockView.OnCompleteListener() {
        @Override
        public void onComplete(String password, int[] indexs) {
            Toast.makeText(SecondActivity.this, getPassWordHint(), Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onError(String errorTimes) {
            Toast.makeText(SecondActivity.this, "密码错误，还可以输入" + errorTimes + "次", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPasswordIsShort(int passwordMinLength) {
            Toast.makeText(SecondActivity.this, "密码不能少于" + passwordMinLength + "个点", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAginInputPassword(LockMode mode) {
            Toast.makeText(SecondActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInputNewPassword() {
            Toast.makeText(SecondActivity.this, "请输入新密码", Toast.LENGTH_SHORT).show();
        }

    };


    /**
     * 密码相关操作完成回调提示
     */
    private String getPassWordHint() {
        String str = null;
        switch (lockView.getMode()) {
            case SETTING_PASSWORD:
                str = "密码设置成功";
                break;
            case EDIT_PASSWORD:
                str = "密码修改成功";
                break;
            case VERIFY_PASSWORD:
                str = "密码正确";
                break;
            case CLEAR_PASSWORD:
                str = "密码已经关闭";
                break;
        }
        return str;
    }

    /**
     * 设置解锁模式
     */
    private void setLockMode(LockMode mode) {
        String str = "";
        switch (mode) {
            case CLEAR_PASSWORD:
                str = "清除密码";
                setLockMode(CLEAR_PASSWORD, PasswordUtil.getPin(this), str);
                break;
            case EDIT_PASSWORD:
                str = "修改密码";
                setLockMode(LockMode.EDIT_PASSWORD, PasswordUtil.getPin(this), str);
                break;
            case SETTING_PASSWORD:
                str = "设置密码";
                setLockMode(SETTING_PASSWORD, null, str);
                break;
            case VERIFY_PASSWORD:
                str = "验证密码";
                setLockMode(LockMode.VERIFY_PASSWORD, PasswordUtil.getPin(this), str);
                break;
        }
        tv_text.setText(str);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
