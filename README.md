# GestureLibray
##手势解锁库
#####注：因为项目需要用到手势解锁，在github上找到了某位网友提供的库，效果很好，但是由于该库是直接使用资源图片绘制状态图标，如果要实现其他的UI效果不是很灵活，于是自己对其进行了一些优化，提供了一部分自定义属性，以满足项目需求。在此感谢这位网友，PS:时间比较久了,具体我也不记得是哪位网友了，囧...。
###效果如下图：
![Alt Text](http://a1.qpic.cn/psb?/ce0faf5b-de8c-4e33-90c5-83a9f7b4be98/zaSllfWJWbMTBTMvXTg5hbIdi2eGw86R2fmB3tvPvlk!/b/dGUBAAAAAAAA&bo=UgFYAgAAAAACSGU!&rf=viewer_4)
###项目编译环境：Android studio
###密码说明:输入的密码添加了Base64加密
###属性说明
####颜色与尺寸
#####color_ordinary_ring 未按下时九宫格按钮颜色
#####color_on_ring 按下状态下圈的颜色
#####color_error_ring 密码错误时的颜色
#####outer_ring_spacing_width 宫格之间的间距（圆的大小会根据设置的间距动态适配）
#####inner_ring_width 中间最小的圆的大小
#####inner_background_width 中间透明的圆的大小
####其他
#####设置是否显示绘制方向: setShow(true);/setShow(false);
#####设置密码允许输错次数: setErrorTimes(3);
#####设置密码最少长度:setPasswordMinLength(3);

###引用库方式：
####gradle: compile 'com.leo.gesturelibray:GestureLibray:1.0.4'


