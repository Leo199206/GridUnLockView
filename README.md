# GestureLibrary
### 手势解锁库

#### 项目编译环境：Android studio

#### 属性说明

##### 颜色与尺寸
* color_up_ring 未按下时九宫格按钮颜色
* color_on_ring 按下状态下圈的颜色
* color_error_ring 错误时的颜色
* connecting_line_color 正常连接线颜色
* connecting_line_error_color 发生错误时连接线颜色
* inner_ring_color 内圆颜色
* inner_ring_background_color 内圆背景圆颜色
* inner_ring_error_color 内圆错误状态下颜色
* inner_ring_background_error_color 内圆背景圆错误状态下颜色
* outer_ring_spacing_width 外圈与外圈之间的间距
* inner_ring_width 内圆大小
* inner_ring_background_width 内圆背景大小
* connecting_line_width 连接线宽度
* is_line_in_the_round 解锁连接线，是否绘制在圆圈上层

##### 其他

* 设置是否显示绘制方向（三角形指示）: setShowArrow(true);/setShowArrow(false);
* 设置密码允许输错次数: setInputPasswordMaxCount(3);
* 设置密码最少长度:setInputPasswordMinLength(3);
* 设置密码后是否立即保存到本地:setSavePin(true);
* 设置模式:setMode(LockMode mode);
* 设置连接线是否绘制在圆的上部:setLineInTheRound()


##### LockMode

* 设置密码:LockMode.SETTING_PASSWORD
* 修改密码:LockMode.EDIT_PASSWORD
* 验证密码:LockMode.VERIFY_PASSWORD
* 清除密码:LockMode.CLEAR_PASSWORD

#### Gradle
Add this to your project's build.gradle file
```xml
dependencies {

   compile 'com.leo.gesturelibray:GestureLibray:1.1.4'

}
```
### Version：1.1.1

##### 更新说明：
###### Version：1.1.4
* 新增内圈颜色、连接线、连接线是否覆盖在圆上等属性配置
* fix bug
