#### 使用说明文档 

九宫格手势解锁控件，用于锁屏或密码验证需求。

#### 效果预览
<img src="https://raw.githubusercontent.com/Leo199206/GridUnLockView/master/device-2021-04-10-203741.gif" width="300" heght="500" align=center />


#### 依赖
+ 添加maven仓库配置到项目根目录gradle文件下

```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

+ 添加以下maven依赖配置到app模块，gradle文件下

```
implementation  'com.github.Leo199206:GridUnLockView:1.1.6'
```

#### 添加到布局

```
  <com.lock.gridview.GridLockView
        android:id="@+id/lv_lock"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        app:color_error_ring="#e91515"
        app:color_on_ring="#64a460"
        app:color_up_ring="#3ce915"
        app:connecting_line_color="#93d98f"
        app:connecting_line_error_color="#f47e7e"
        app:connecting_line_width="5dp"
        app:inner_ring_background_color="#93d98f"
        app:inner_ring_background_error_color="#f47e7e"
        app:inner_ring_background_width="20dp"
        app:inner_ring_color="#3ce915"
        app:inner_ring_error_color="#e91515"
        app:inner_ring_width="14dp"
        app:outer_ring_finger_line_width="2dp"
        app:outer_ring_spacing_width="50dp"
        app:outer_ring_un_finger_line_width="1dp" />

```

+ 代码配置

```
  lockView.setMode(LockMode.SETTING_PASSWORD);
  lockView.setOnGridUnLockViewListener( new GridLockView.OnGridUnLockViewListener() {

        @Override
        public void onComplete(LockMode mode, String password, int[] index) {
           
        }

        @Override
        public void clearPassword(LockMode mode, String password, int[] index) {
    
        }

        @Override
        public void savePassword(LockMode mode, String password, int[] index) {
          
        }

        @Override
        public void onError(LockMode mode, String errorCount) {
         
        }

        @Override
        public void onPasswordIsShort(LockMode mode, int passwordMinLength) {
          
        }

        @Override
        public void onAgainInputPassword(LockMode mode, String password, int[] index) {
          
        }

        @Override
        public void onInputNewPassword(LockMode mode) {
          
        }

        @Override
        public void onEnteredPasswordsDiffer(LockMode mode) {
       
        }


        @Override
        public void onErrorNumberMany() {
        
        }

    });

```


#### 已定义样式属性

| 属性  | 说明 |
| --- | --- |
| color_up_ring | 默认状态，宫格圆圈颜色|
| color_on_ring | 按下状态，宫格圆圈颜色 |
| color_error_ring | 解锁密码或者绘制错误时，宫格圆圈颜色 |
| connecting_line_color | 默认连线颜色 |
| connecting_line_error_color | 错误连线颜色 | 
| inner_ring_background_color | 按下时，解锁内圆背景颜色 | 
| inner_ring_error_color | 绘制错误时，内圆颜色 |
| inner_ring_background_error_color | 绘制错误时，内圆背景颜色 | 
| outer_ring_spacing_width | 内圆与外圈间距 | 
| inner_ring_width | 内圆尺寸大小 | 
| inner_ring_background_width | 内圆背景大小 |
| connecting_line_width | 解锁连线宽度 |
| is_line_in_on_circle_top | 连接线绘制层级控制|
| is_show_direction_arrow | 是否显示连线箭头|


#### 支持
+ 使用过程中，如有问题或者建议，欢迎提出issue  

#### LICENSE
GridUnLockView is under the Apache License Version 2.0. See the [LICENSE](https://raw.githubusercontent.com/Leo199206/GridUnLockView/main/LICENSE) file for details.
LICENSE
