package com.leo;

import android.app.Application;

import com.leo.util.ConfigUtil;

/**
 * <pre>
 *     author : leo
 *     time   : 2019/01/04
 *     desc   :
 * </pre>
 */
public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ConfigUtil.init(this);
    }
}
