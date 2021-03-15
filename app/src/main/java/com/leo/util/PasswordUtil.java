package com.leo.util;

import android.content.Context;

import com.leo.Contants;

/**
 * Created by leo on 16/4/5.
 */
public class PasswordUtil {

    /**
     * 获取设置过的密码
     */
    public static String getPin() {
        String password = ConfigUtil.getString(Contants.PASS_KEY);
        return password;
    }
}
