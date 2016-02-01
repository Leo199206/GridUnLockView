package com.leo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.leo.gesturelibray.CustomLockView;
import com.leo.gesturelibray.util.StringUtils;
import com.leo.util.ConfigUtil;

public class MainActivity extends AppCompatActivity {
    private CustomLockView lockView;
    private TextView tv_text;
    private static String PASS_KEY = "PASS_KEY_MAP";
    private boolean isUnLock = false;
    private String mPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lockView = (CustomLockView) findViewById(R.id.lv_lock);
        tv_text = (TextView) findViewById(R.id.tv_text);
        lockView.setShow(true);//不显示绘制方向
        lockView.setErrorTimes(3);//允许最大输入次数
        lockView.setPasswordMinLength(4);//密码最少位数
        setLockMode();
        lockView.setOnCompleteListener(onCompleteListener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }


    /**
     * 密码输入模式
     */
    private void setLockMode() {
        String password = ConfigUtil.getInstance(this).getString(PASS_KEY);
        if (StringUtils.isNotEmpty(password)) {
            lockView.setStatus(1);//解锁
            isUnLock = true;
            lockView.setOldPassword(password);
            tv_text.setText("请输入密码");
        } else {
            isUnLock = false;
            lockView.setStatus(0);//设置密码
            tv_text.setText("请设置密码");
        }
    }


    /**
     * 密码输入监听
     */
    CustomLockView.OnCompleteListener onCompleteListener = new CustomLockView.OnCompleteListener() {
        @Override
        public void onComplete(String password, int[] indexs) {
            if (isUnLock) {
                tv_text.setText("密码正确");
                Toast.makeText(MainActivity.this, "密码正确", Toast.LENGTH_SHORT).show();
            } else {
                if (StringUtils.isEmpty(mPassword)) {
                    mPassword = password;
                    tv_text.setText("请再次输入密码");
                    Toast.makeText(MainActivity.this, "请再次输入密码", Toast.LENGTH_SHORT).show();
                }else if (StringUtils.isEquals(mPassword,password)){
                    tv_text.setText("密码设置成功");
                    Toast.makeText(MainActivity.this, "密码设置成功", Toast.LENGTH_SHORT).show();
                    ConfigUtil.getInstance(MainActivity.this).putString(PASS_KEY,password);
                    setLockMode();
                }else {
                    tv_text.setText("两次输入的密码不一致");
                    Toast.makeText(MainActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onError(int errorTimes) {
            Toast.makeText(MainActivity.this, "密码错误，还可以输入" + errorTimes + "次", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPasswordIsShort(int passwordMinLength) {
            Toast.makeText(MainActivity.this, "密码不能少于" + passwordMinLength + "个点", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
