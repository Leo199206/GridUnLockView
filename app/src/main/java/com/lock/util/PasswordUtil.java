package com.lock.util;

import com.lock.Constants;

/**
 * Created by leo on 16/4/5.
 */
public class PasswordUtil {

    /**
     * 获取设置过的密码
     */
    public static String getPin() {
        String password = ConfigUtil.getString(Constants.PASS_KEY);
        return password;
    }
}
