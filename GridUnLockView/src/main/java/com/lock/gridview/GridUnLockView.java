package com.lock.gridview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.lock.gridview.entity.Point;
import com.lock.gridview.enums.LockMode;
import com.lock.gridview.util.LockUtil;
import com.lock.gridview.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;


/**
 * 九宫格手势解锁控件
 */
public class GridUnLockView extends View {
    //控件宽度
    private float width = 0;
    //控件高度
    private float height = 0;
    //是否已缓存
    private boolean isCache = false;
    //画笔
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //九宫格的圆
    private Point[][] allPoints = new Point[3][3];
    //选中圆的集合
    private List<Point> selectPoints = new ArrayList<Point>();
    //判断是否正在绘制并且未到达下一个点
    private boolean movingNoPoint = false;
    //正在移动的x,y坐标
    float moveX, moveY;
    //密码最小长度
    private int passwordMinLength = 3;
    //判断是否触摸屏幕
    private boolean checking = false;
    //刷新
    private TimerTask task = null;
    //监听
    private OnGridUnLockViewListener lockViewListener;
    //错误限制 默认为4次
    private int errorNumber = 4;
    //记录上一次滑动的密码
    private String oldPassword = null;
    //记录当前第几次触发 默认为0次
    private int showTimes = 0;
    //当前密码是否正确 默认为正确
    private boolean isCorrect = true;
    //是否显示滑动方向 默认为显示
    private boolean isShowDirectionArrow = true;
    //验证或者设置 0:设置 1:验证
    private LockMode mode = LockMode.SETTING_PASSWORD;
    //用于执行清除界面
    private Handler handler = new Handler();
    //间距
    float roundW;
    //普通状态下圈的颜色
    private int colorUpRing = 0xFF378FC9;
    //按下时圈的颜色
    private int colorOnRing = 0xFF378FC9;
    //松开手时的颜色
    private int colorErrorRing = 0xFF378FC9;
    //正常连接线颜色
    private int connectingLineColor;

    //发生错误时连接线颜色
    private int connectingLineErrorColor;
    //按下时内圆颜色
    private int innerRingColor;
    private int innerRingBackgroundColor;
    private int innerRingErrorColor;
    private int innerRingBackgroundErrorColor;


    //外圈大小
    private float outerRingWidth = 120;
    //内圆大小
    private float innerRingWidth;
    //内圆间距
    private float circleSpacing;
    //圆圈半径
    private float radius;
    //小圆半径
    private float innerRingRadius;
    //小圆半透明背景半径
    private float innerBackgroundRadius;
    //内圆背景大小（半透明内圆）
    private float innerBackgroundWidth;
    //三角形边长
    private float arrowLength;
    //未按下时圆圈的边宽
    private int unFingerStrokeWidth = 2;
    //连接线宽度
    private int connectingLineWidth = 2;
    //按下时圆圈的边宽
    private int fingerStrokeWidth = 4;
    //编辑密码前是否验证
    private boolean isEditVerify = false;
    //是否将连接线绘制在圆的上层
    private boolean isLineOnCircle;

    //用于定时执行清除界面
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(run);
            reset();
            postInvalidate();
        }
    };

    public GridUnLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridUnLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GridLockView, defStyleAttr, 0);
        colorOnRing = a.getColor(R.styleable.GridLockView_color_on_ring, colorOnRing);
        colorUpRing = a.getColor(R.styleable.GridLockView_color_up_ring, colorUpRing);
        colorErrorRing = a.getColor(R.styleable.GridLockView_color_error_ring, colorErrorRing);
        innerRingWidth = a.getDimensionPixelOffset(R.styleable.GridLockView_inner_ring_width, 0);
        circleSpacing = a.getDimensionPixelOffset(R.styleable.GridLockView_outer_ring_spacing_width, 0);
        innerBackgroundWidth = a.getDimensionPixelOffset(R.styleable.GridLockView_inner_ring_background_width, 0);
        isLineOnCircle = a.getBoolean(R.styleable.GridLockView_is_line_in_on_circle_top, false);
        connectingLineWidth = a.getDimensionPixelOffset(R.styleable.GridLockView_connecting_line_width, 2);
        connectingLineColor = a.getColor(R.styleable.GridLockView_connecting_line_color, colorOnRing);
        connectingLineErrorColor = a.getColor(R.styleable.GridLockView_connecting_line_error_color, colorErrorRing);
        innerRingColor = a.getColor(R.styleable.GridLockView_inner_ring_color, colorOnRing);
        innerRingBackgroundColor = a.getColor(R.styleable.GridLockView_inner_ring_background_color, colorOnRing);
        innerRingErrorColor = a.getColor(R.styleable.GridLockView_inner_ring_error_color, colorErrorRing);
        innerRingBackgroundErrorColor = a.getColor(R.styleable.GridLockView_inner_ring_background_error_color, colorErrorRing);
        isShowDirectionArrow = a.getBoolean(R.styleable.GridLockView_is_show_direction_arrow, false);
        fingerStrokeWidth = a.getDimensionPixelOffset(R.styleable.GridLockView_outer_ring_finger_line_width, 4);
        unFingerStrokeWidth = a.getDimensionPixelOffset(R.styleable.GridLockView_outer_ring_un_finger_line_width, 1);
        a.recycle();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MathUtil.measure(widthMeasureSpec);
        height = MathUtil.measure(heightMeasureSpec);
        width = Math.min(width, height);
        height = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isCache) {
            initCache();
        }
        //绘制圆以及显示当前状态
        drawToCanvas(canvas);
    }

    /**
     * 初始化Cache信息
     */
    private void initCache() {
        float y = 0;
        initGestureLockViewWidth();
        // 计算圆圈的大小及位置
        roundW = width - (outerRingWidth * 3);
        roundW = roundW / 4 + outerRingWidth / 2;
        allPoints[0][0] = new Point(getX(0), y + roundW);
        allPoints[0][1] = new Point(getX(1), y + roundW);
        allPoints[0][2] = new Point(getX(2), y + roundW);
        allPoints[1][0] = new Point(getX(0), y + height / 2);
        allPoints[1][1] = new Point(getX(1), y + height / 2);
        allPoints[1][2] = new Point(getX(2), y + height / 2);
        allPoints[2][0] = new Point(getX(0), y + height - roundW);
        allPoints[2][1] = new Point(getX(1), y + height - roundW);
        allPoints[2][2] = new Point(getX(2), y + height - roundW);
        int k = 0;
        for (Point[] ps : allPoints) {
            for (Point p : ps) {
                p.index = k;
                k++;
            }
        }
        isCache = true;
    }


    /**
     * 计算圆以及连接线的尺寸
     */
    private void initGestureLockViewWidth() {
        if (circleSpacing == 0) {
            initCircleSpacing();
        } else {
            float mSpacing = circleSpacing * (3 + 1);
            outerRingWidth = (width - mSpacing) / 3;
        }
        if (outerRingWidth <= 0) {//防止手动设置圆圆之间间距过大问题
            initCircleSpacing();
        }
        if (innerRingWidth == 0 || innerRingWidth >= outerRingWidth) {
            innerRingWidth = outerRingWidth / 3;
        }
        if (innerBackgroundWidth == 0 || innerBackgroundWidth >= outerRingWidth) {
            innerBackgroundWidth = innerRingWidth * 1.3f;
        }
        innerBackgroundRadius = innerBackgroundWidth / 2;
        radius = outerRingWidth / 2;
        innerRingRadius = innerRingWidth / 2;
        arrowLength = radius * 0.25f;//三角形的边长
    }


    /**
     * 当外圈间距没有设置时，初始化外圆之间的间距
     */
    private void initCircleSpacing() {
        // 计算每个GestureLockView的宽度
        outerRingWidth = width / 6;
        //计算每个GestureLockView的间距
        circleSpacing = (width - outerRingWidth * 3) / 4;
    }


    /**
     * 获取x点得坐标
     */
    public float getX(int i) {
        if (i == 0) {
            return circleSpacing + radius;
        } else if (i == 1) {
            return width / 2;
        }
        return circleSpacing * 3 + outerRingWidth * 2 + radius;

    }


    /**
     * 图像绘制
     *
     * @param canvas
     */
    private void drawToCanvas(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        if (isLineOnCircle) {
            // 画外圈
            drawAllOuterRing(canvas);
            // 画连线
            drawAllLine(canvas);
            // 画内圈
        } else {
            // 画连线
            drawAllLine(canvas);
            // 画外圈
            drawAllOuterRing(canvas);
            // 画所有点
        }
        drawAllInnerRing(canvas);
        // 是否绘制方向图标
        if (isShowDirectionArrow) {
            drawDirectionArrow(canvas);
        }
    }

    /**
     * 绘制解锁连接线
     *
     * @param canvas
     */
    private void drawAllLine(Canvas canvas) {
        if (selectPoints.size() > 0) {
            Point tp = selectPoints.get(0);
            for (int i = 1; i < selectPoints.size(); i++) {
                //根据移动的方向绘制线
                Point p = selectPoints.get(i);
                if (p.state == Point.STATE_CHECK_ERROR) {
                    drawErrorLine(canvas, tp, p);
                } else {
                    drawLine(canvas, tp, p);
                }
                tp = p;
            }
            if (this.movingNoPoint) {
                //到达下一个点停止移动绘制固定的方向
                drawLine(canvas, tp, new Point((int) moveX + 20, (int) moveY));
            }
        }
    }


    /**
     * 绘制解锁图案所有的点
     *
     * @param canvas
     */
    private void drawAllInnerRing(Canvas canvas) {
        for (int i = 0; i < allPoints.length; i++) {
            for (int j = 0; j < allPoints[i].length; j++) {
                Point p = allPoints[i][j];
                if (p != null) {
                    if (p.state == Point.STATE_CHECK) {
                        onDrawInnerRing(canvas, p);
                    } else if (p.state == Point.STATE_CHECK_ERROR) {
                        onDrawErrorInnerRing(canvas, p);
                    } else {
                        onDrawNoFinger(canvas, p);
                    }
                }
            }
        }
    }


    /**
     * 绘制解锁图案所有的点
     *
     * @param canvas
     */
    private void drawAllOuterRing(Canvas canvas) {
        for (int i = 0; i < allPoints.length; i++) {
            for (int j = 0; j < allPoints[i].length; j++) {
                Point p = allPoints[i][j];
                if (p != null) {
                    if (p.state == Point.STATE_CHECK) {
                        onDrawOuterRing(canvas, p);
                    } else if (p.state == Point.STATE_CHECK_ERROR) {
                        onDrawErrorOuterRing(canvas, p);
                    } else {
                        onDrawNoFinger(canvas, p);
                    }
                }
            }
        }
    }


    /**
     * 绘制解锁图案连接的方向
     *
     * @param canvas
     */
    private void drawDirectionArrow(Canvas canvas) {
        // 绘制方向图标
        if (selectPoints.size() <= 0) {
            return;
        }
        Point tp = selectPoints.get(0);
        for (int i = 1; i < selectPoints.size(); i++) {
            //根据移动的方向绘制方向图标
            Point p = selectPoints.get(i);
            if (p.state == Point.STATE_CHECK_ERROR) {
                drawDirectionArrow(canvas, tp, p, colorErrorRing);
            } else {
                drawDirectionArrow(canvas, tp, p, colorOnRing);
            }
            tp = p;
        }
    }


    /**
     * 绘制正常状态外圈
     *
     * @PARAM CANVAS
     * @PARAM P
     */
    private void onDrawOuterRing(Canvas canvas, Point p) {
        // 绘制背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, radius, paint);
        // 绘制外圆
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colorOnRing);
        paint.setStrokeWidth(fingerStrokeWidth);
        canvas.drawCircle(p.x, p.y, radius, paint);
    }

    /**
     * 绘制错误状态外圈
     *
     * @PARAM CANVAS
     * @PARAM P
     */
    private void onDrawErrorOuterRing(Canvas canvas, Point p) {
        // 绘制背景
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, radius, paint);
        // 绘制圆圈
        paint.setColor(colorErrorRing);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(fingerStrokeWidth);
        canvas.drawCircle(p.x, p.y, radius, paint);
    }


    /**
     * 绘制按下时内圈
     *
     * @param canvas
     */
    private void onDrawInnerRing(Canvas canvas, Point p) {
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, innerRingBackgroundColor);
        // 绘制内圆
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(innerRingColor);
        canvas.drawCircle(p.x, p.y, innerRingRadius, paint);
    }

    /**
     * 绘制错误状态内圈
     *
     * @PARAM CANVAS
     * @PARAM P
     */
    private void onDrawErrorInnerRing(Canvas canvas, Point p) {
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, innerRingBackgroundErrorColor);
        // 绘制内圆
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(innerRingErrorColor);
        canvas.drawCircle(p.x, p.y, innerRingRadius, paint);
    }


    /**
     * 绘制普通状态
     *
     * @param canvas
     */
    private void onDrawNoFinger(Canvas canvas, Point p) {
        // 绘制外圆
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(colorUpRing);
        paint.setStrokeWidth(unFingerStrokeWidth);
        canvas.drawCircle(p.x, p.y, radius, paint);
    }

    /**
     * 绘制内圆透明背景
     *
     * @param canvas
     */
    private void onDrawInnerCircleBackground(Canvas canvas, Point p, int color) {
        // 绘制内圆
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(p.x, p.y, innerBackgroundRadius, paint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可操作
        if (errorNumber <= 0) {
            return false;
        }
        isCorrect = true;
        handler.removeCallbacks(run);
        movingNoPoint = false;
        float ex = event.getX();
        float ey = event.getY();
        boolean isFinish = false;
        Point p = null;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // 点下
                // 如果正在清除密码,则取消
                p = actionDown(ex, ey);
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                p = actionMove(ex, ey);
                break;
            case MotionEvent.ACTION_UP: // 提起
                p = checkSelectPoint(ex, ey);
                checking = false;
                isFinish = true;
                break;
            default:
                movingNoPoint = true;
                break;
        }
        if (!isFinish && checking && p != null) {
            int rk = crossPoint(p);
            if (rk == 2) {
                //与非最后一重叠
                movingNoPoint = true;
                moveX = ex;
                moveY = ey;
            } else if (rk == 0) {
                //一个新点
                p.state = Point.STATE_CHECK;
                addPoint(p);
            }
        }
        if (isFinish) {
            actionFinish();
        }
        postInvalidate();
        return true;
    }

    /**
     * 解锁图案绘制完成
     */
    private void actionFinish() {
        handler.postDelayed(run, 500);
        if (this.selectPoints.size() == 1) {
            this.reset();
            return;
        }
        if (this.selectPoints.size() < passwordMinLength
                && this.selectPoints.size() > 0) {
            error();
            if (lockViewListener != null) {
                lockViewListener.onPasswordIsShort(mode, passwordMinLength);  //密码太短
            }
            return;
        }
        if (this.selectPoints.size() >= passwordMinLength) {
            int[] indexs = new int[selectPoints.size()];
            for (int i = 0; i < selectPoints.size(); i++) {
                indexs[i] = selectPoints.get(i).index;
            }
            if (mode == LockMode.SETTING_PASSWORD || isEditVerify) {
                invalidSettingPass(arrayToString(indexs), indexs);
            } else {
                onVerifyPassword(arrayToString(indexs), indexs);
            }
        }
    }

    /**
     * 按下
     *
     * @param ex
     * @param ey
     */
    private Point actionDown(float ex, float ey) {
        // 如果正在清除密码,则取消
        if (task != null) {
            task.cancel();
            task = null;
        }
        // 删除之前的点
        reset();
        Point p = checkSelectPoint(ex, ey);
        if (p != null) {
            checking = true;
        }
        return p;
    }

    /**
     * 移动
     *
     * @param ex
     * @param ey
     */
    private Point actionMove(float ex, float ey) {
        Point p = null;
        if (checking) {
            p = checkSelectPoint(ex, ey);
            if (p == null) {
                movingNoPoint = true;
                moveX = ex;
                moveY = ey;
            }
        }
        return p;
    }


    /**
     * 向选中点集合中添加一个点
     *
     * @param point
     */
    private void addPoint(Point point) {
        this.selectPoints.add(point);
    }

    /**
     * 检查点是否被选择
     *
     * @param x
     * @param y
     * @return
     */
    private Point checkSelectPoint(float x, float y) {
        for (int i = 0; i < allPoints.length; i++) {
            for (int j = 0; j < allPoints[i].length; j++) {
                Point p = allPoints[i][j];
                if (LockUtil.checkInRound(p.x, p.y, radius, (int) x, (int) y)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * 判断点是否有交叉 返回 0,新点 ,1 与上一点重叠 2,与非最后一点重叠
     *
     * @param p
     * @return
     */
    private int crossPoint(Point p) {
        // 重叠的不最后一个则 reset
        if (selectPoints.contains(p)) {
            if (selectPoints.size() > 2) {
                // 与非最后一点重叠
                if (selectPoints.get(selectPoints.size() - 1).index != p.index) {
                    return 2;
                }
            }
            return 1; // 与最后一点重叠
        } else {
            return 0; // 新点
        }
    }

    /**
     * 重置点状态
     */
    private void reset() {
        for (Point p : selectPoints) {
            p.state = Point.STATE_NORMAL;
        }
        selectPoints.clear();
    }

    /**
     * 清空当前信息
     */
    public void clearCurrentState() {
        showTimes = 0;
        isCorrect = true;
        oldPassword = "";
        isEditVerify = false;
        reset();
        postInvalidate();
    }

    /**
     * 画两点的连接
     *
     * @param canvas
     * @param a
     * @param b
     */
    private void drawLine(Canvas canvas, Point a, Point b) {
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(connectingLineColor);
        paint.setStrokeWidth(connectingLineWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
    }

    /**
     * 错误线
     *
     * @param canvas
     * @param a
     * @param b
     */
    private void drawErrorLine(Canvas canvas, Point a, Point b) {
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(connectingLineErrorColor);
        paint.setStrokeWidth(connectingLineWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, paint);
    }

    /**
     * 绘制方向图标,三角形指示标
     *
     * @param canvas
     * @param a
     * @param b
     * @param color
     */
    private void drawDirectionArrow(Canvas canvas, Point a, Point b, int color) {
        //获取角度
        float degrees = LockUtil.getDegrees(a, b) + 90;
        //根据两点方向旋转
        canvas.rotate(degrees, a.x, a.y);
        drawArrow(canvas, a, color);
        //旋转方向
        canvas.rotate(-degrees, a.x, a.y);
    }


    /**
     * 绘制三角形指示标
     *
     * @param canvas
     * @param a
     * @param color
     */
    private void drawArrow(Canvas canvas, Point a, int color) {
        // 绘制三角形，初始时是个默认箭头朝上的一个等腰三角形，用户绘制结束后，根据由两个GestureLockView决定需要旋转多少度
        Path arrowPath = new Path();
        float offset = innerBackgroundRadius + (arrowLength + radius - innerBackgroundRadius) / 2;//偏移量,定位三角形位置
        arrowPath.moveTo(a.x, a.y - offset);
        arrowPath.lineTo(a.x - arrowLength, a.y - offset
                + arrowLength);
        arrowPath.lineTo(a.x + arrowLength, a.y - offset
                + arrowLength);
        arrowPath.close();
        arrowPath.setFillType(Path.FillType.WINDING);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawPath(arrowPath, paint);
    }


    /**
     * 设置已经选中的为错误
     */
    private void error() {
        for (Point p : selectPoints) {
            p.state = Point.STATE_CHECK_ERROR;
        }
    }

    /**
     * 验证设置密码，滑动两次密码是否相同
     *
     * @param password
     */
    private void invalidSettingPass(String password, int[] indexs) {
        if (showTimes == 0) {
            oldPassword = password;
            if (lockViewListener != null) {
                lockViewListener.onAgainInputPassword(mode, password, indexs);
            }
            showTimes++;
            reset();
        } else if (showTimes == 1) {
            onVerifyPassword(password, indexs);
        }
    }

    /**
     * 验证本地密码与当前滑动密码是否相同
     *
     * @param index
     * @param password
     */
    private void onVerifyPassword(String password, int[] index) {
        if (TextUtils.equals(oldPassword, password)) {
            isCorrect = true;
        } else {
            isCorrect = false;
        }
        if (!isCorrect) {
            drawPassWordError();
        } else {
            drawPassWordRight(password, index);
        }
    }

    /**
     * 密码输入错误回调
     */
    private void drawPassWordError() {
        if (lockViewListener == null) {
            return;
        }
        if (mode == LockMode.SETTING_PASSWORD) {
            lockViewListener.onEnteredPasswordsDiffer(mode);
        } else if (mode == LockMode.EDIT_PASSWORD && isEditVerify) {
            lockViewListener.onEnteredPasswordsDiffer(mode);
        } else {
            errorNumber--;
            if (errorNumber <= 0) {
                lockViewListener.onErrorNumberMany();
            } else {
                lockViewListener.onError(mode, errorNumber + "");
            }
        }
        error();
        postInvalidate();
    }


    /**
     * 输入密码正确相关回调
     *
     * @param indexs
     * @param password
     */
    private void drawPassWordRight(String password, int[] indexs) {
        if (lockViewListener == null) {
            return;
        }
        if (mode == LockMode.EDIT_PASSWORD && !isEditVerify) {//修改密码，旧密码正确，进行新密码设置
            lockViewListener.onInputNewPassword(mode);
            isEditVerify = true;
            showTimes = 0;
            return;
        }
        if (mode == LockMode.EDIT_PASSWORD && isEditVerify) {
            savePassWord(password, indexs);
        } else if (mode == LockMode.CLEAR_PASSWORD) {//清除密码
            if (lockViewListener != null) {
                lockViewListener.clearPassword(mode, password, indexs);
            }
        } else if (mode == LockMode.SETTING_PASSWORD) {//完成密码设置，存储到本地
            savePassWord(password, indexs);
        } else {
            isEditVerify = false;
        }
        lockViewListener.onComplete(mode, password, indexs);
    }

    /**
     * 存储密码到本地
     *
     * @param password
     */
    private void savePassWord(String password, int[] indexs) {
        if (lockViewListener != null) {
            lockViewListener.savePassword(mode, password, indexs);
        }
    }


    /**
     * 设置监听
     *
     * @param mCompleteListener
     */
    public void setOnGridUnLockViewListener(OnGridUnLockViewListener mCompleteListener) {
        this.lockViewListener = mCompleteListener;
    }


    /**
     * array to String
     *
     * @param array
     */
    public String arrayToString(int[] array) {
        String a = "";
        for (int i : array) {
            a += i;
        }
        return a;
    }

    /**
     * 轨迹球画完监听事件
     */
    public interface OnGridUnLockViewListener {
        /**
         * 画完了
         */
        void onComplete(LockMode mode, String password, int[] indexs);

        /**
         * 验证正确，执行清除密码回调
         */
        void clearPassword(LockMode mode, String password, int[] indexs);

        /**
         * 验证完成，保存密码
         */
        void savePassword(LockMode mode, String password, int[] indexs);

        /**
         * 绘制错误
         */
        void onError(LockMode mode, String errorTimes);

        /**
         * 密码太短
         */
        void onPasswordIsShort(LockMode mode, int passwordMinLength);


        /**
         * 设置密码再次输入密码
         */
        void onAgainInputPassword(LockMode mode, String password, int[] indexs);


        /**
         * 修改密码，输入新密码
         */
        void onInputNewPassword(LockMode mode);

        /**
         * 两次输入密码不一致
         */
        void onEnteredPasswordsDiffer(LockMode mode);

        /**
         * 密码输入错误次数，已达到设置次数
         */
        void onErrorNumberMany();

    }


    /**
     * 设置允许最大输入错误次数
     *
     * @param errorCount
     */
    public void setInputPasswordMaxCount(int errorCount) {
        this.errorNumber = errorCount;
    }


    /**
     * 是否显示连接方向三角形
     *
     * @param isShowArrow
     */
    public void setShowDirectionArrow(boolean isShowArrow) {
        this.isShowDirectionArrow = isShowArrow;
    }

    /**
     * 获取已经设置过的密码
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * 设置旧密码
     *
     * @param oldPassword
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * 最小输入密码位数
     */
    public int getPasswordMinLength() {
        return passwordMinLength;
    }


    /**
     * 设置密码最少输入长度
     *
     * @param passwordMinLength
     */
    public void setInputPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }

    /**
     * 九宫格解锁模式
     */
    public LockMode getMode() {
        return mode;
    }


    /**
     * 设置解锁模式
     *
     * @param mode
     */
    public void setMode(LockMode mode) {
        this.mode = mode;
    }

    /**
     * 是否将连接线绘制在圆圈上面
     *
     * @param isTop
     */
    public GridUnLockView setLineOnCircleTop(boolean isTop) {
        isLineOnCircle = isTop;
        return this;
    }

}
