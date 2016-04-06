package com.leo.ui.activity;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.Contants;
import com.leo.R;
import com.leo.gesturelibray.enums.LockMode;
import com.leo.gesturelibray.view.CustomLockView;
import com.leo.ui.base.BaseActivity;
import com.leo.util.PasswordUtil;
import com.leo.util.ToastUtil;

import static com.leo.gesturelibray.enums.LockMode.CLEAR_PASSWORD;
import static com.leo.gesturelibray.enums.LockMode.SETTING_PASSWORD;

public class SecondActivity extends BaseActivity {
    private CustomLockView lockView;
    private TextView tv_text;


    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_second);
    }

    /**
     * 初始化View
     */
    @Override
    public void initView() {
        lockView = (CustomLockView) findViewById(R.id.lv_lock);
        tv_text = (TextView) findViewById(R.id.tv_text);
    }

    /**
     * 设置监听回调
     */
    @Override
    public void initListener() {
        lockView.setOnCompleteListener(onCompleteListener);
    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {
        lockView.setShow(true);//不显示绘制方向
        lockView.setErrorTimes(3);//允许最大输入次数
        lockView.setPasswordMinLength(4);//密码最少位数
        lockView.setSaveLockKey(Contants.PASS_KEY);
        LockMode lockMode = (LockMode) getIntent().getSerializableExtra(Contants.INTENT_SECONDACTIVITY_KEY);
        setLockMode(lockMode);
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
     * onClick 返回
     */
    public void onBack(View view) {
        onBackPressed();
    }

    /**
     * 密码输入监听
     */
    CustomLockView.OnCompleteListener onCompleteListener = new CustomLockView.OnCompleteListener() {
        @Override
        public void onComplete(String password, int[] indexs) {
            ToastUtil.showMessage(SecondActivity.this, getPassWordHint());
            finish();
        }

        @Override
        public void onError(String errorTimes) {
            ToastUtil.showMessage(SecondActivity.this, "密码错误，还可以输入" + errorTimes + "次");
        }

        @Override
        public void onPasswordIsShort(int passwordMinLength) {
            ToastUtil.showMessage(SecondActivity.this, "密码不能少于" + passwordMinLength + "个点");
        }

        @Override
        public void onAginInputPassword(LockMode mode) {
            ToastUtil.showMessage(SecondActivity.this, "请再次输入密码");
        }

        @Override
        public void onInputNewPassword() {
            ToastUtil.showMessage(SecondActivity.this, "请输入新密码");
        }

        @Override
        public void onEnteredPasswordsDiffer() {
            ToastUtil.showMessage(SecondActivity.this, "两次输入的密码不一致");
            Toast.makeText(SecondActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
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
