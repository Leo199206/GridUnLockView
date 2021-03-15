package com.leo.gesturelibrary.view;

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

import com.leo.gesturelibrary.R;
import com.leo.gesturelibrary.entity.Point;
import com.leo.gesturelibrary.enums.LockMode;
import com.leo.gesturelibrary.util.LockUtil;
import com.leo.gesturelibrary.util.MathUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;


/**
 * 手势解锁
 */
public class CustomLockView extends View {
    //控件宽度
    private float width = 0;
    //控件高度
    private float height = 0;
    //是否已缓存
    private boolean isCache = false;
    //画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //九宫格的圆
    private Point[][] mPoints = new Point[3][3];
    //选中圆的集合
    private List<Point> sPoints = new ArrayList<Point>();
    //判断是否正在绘制并且未到达下一个点
    private boolean movingNoPoint = false;
    //正在移动的x,y坐标
    float moveingX, moveingY;
    //密码最小长度
    private int passwordMinLength = 3;
    //判断是否触摸屏幕
    private boolean checking = false;
    //刷新
    private TimerTask task = null;
    //监听
    private OnLockViewListener mCompleteListener;
    //错误限制 默认为4次
    private int errorNumber = 4;
    //记录上一次滑动的密码
    private String oldPassword = null;
    //记录当前第几次触发 默认为0次
    private int showTimes = 0;
    //当前密码是否正确 默认为正确
    private boolean isCorrect = true;
    //是否显示滑动方向 默认为显示
    private boolean isShowArrow = true;
    //验证或者设置 0:设置 1:验证
    private LockMode mode = LockMode.SETTING_PASSWORD;
    //用于执行清除界面
    private Handler handler = new Handler();
    //间距
    float roundW;
    //普通状态下圈的颜色
    private int mColorUpRing = 0xFF378FC9;
    //按下时圈的颜色
    private int mColorOnRing = 0xFF378FC9;
    //松开手时的颜色
    private int mColorErrorRing = 0xFF378FC9;
    //正常连接线颜色
    private int mConnectingLineColor = mColorOnRing;

    //发生错误时连接线颜色
    private int mConnectingLineErrorColor;
    //按下时内圆颜色
    private int mInnerRingColor;
    private int mInnerRingBackgroundColor;
    private int mInnerRingErrorColor;
    private int mInnerRingBackgroundErrorColor;


    //外圈大小
    private float mOuterRingWidth = 120;
    //内圆大小
    private float mInnerRingWidth = mOuterRingWidth / 3;
    //内圆间距
    private float mCircleSpacing;
    //圆圈半径
    private float mRadius;
    //小圆半径
    private float mInnerRingRadius;
    //小圆半透明背景半径
    private float mInnerBackgroundRadius;
    //内圆背景大小（半透明内圆）
    private float mInnerBackgroundWidth;
    //三角形边长
    private float mArrowLength;
    //未按下时圆圈的边宽
    private int mNoFingerStrokeWidth = 2;
    //连接线宽度
    private int mConnectingLineWidth = 2;
    //按下时圆圈的边宽
    private int mOnStrokeWidth = 4;
    //编辑密码前是否验证
    private boolean isEditVerify = false;
    //是否将连接线绘制在圆的上层
    private boolean isLineInTheRound = false;

    //用于定时执行清除界面
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(run);
            reset();
            postInvalidate();
        }
    };

    public CustomLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GestureLock_styleable, defStyleAttr, 0);
        mColorOnRing = a.getColor(R.styleable.GestureLock_styleable_color_on_ring, mColorOnRing);
        mColorUpRing = a.getColor(R.styleable.GestureLock_styleable_color_up_ring, mColorUpRing);
        mColorErrorRing = a.getColor(R.styleable.GestureLock_styleable_color_error_ring, mColorErrorRing);
        mInnerRingWidth = a.getDimensionPixelOffset(R.styleable.GestureLock_styleable_inner_ring_width, 0);
        mCircleSpacing = a.getDimensionPixelOffset(R.styleable.GestureLock_styleable_outer_ring_spacing_width, 0);
        mInnerBackgroundWidth = a.getDimensionPixelOffset(R.styleable.GestureLock_styleable_inner_ring_background_width, 0);
        isLineInTheRound = a.getBoolean(R.styleable.GestureLock_styleable_is_line_in_the_round, false);
        mConnectingLineWidth = a.getDimensionPixelOffset(R.styleable.GestureLock_styleable_connecting_line_width, 2);
        mConnectingLineColor = a.getColor(R.styleable.GestureLock_styleable_connecting_line_color, mColorOnRing);
        mConnectingLineErrorColor = a.getColor(R.styleable.GestureLock_styleable_connecting_line_error_color, mColorErrorRing);
        mInnerRingColor = a.getColor(R.styleable.GestureLock_styleable_inner_ring_color, mColorOnRing);
        mInnerRingBackgroundColor = a.getColor(R.styleable.GestureLock_styleable_inner_ring_background_color, mColorOnRing);
        mInnerRingErrorColor = a.getColor(R.styleable.GestureLock_styleable_inner_ring_error_color, mColorErrorRing);
        mInnerRingBackgroundErrorColor = a.getColor(R.styleable.GestureLock_styleable_inner_ring_background_error_color, mColorErrorRing);
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
        roundW = width - (mOuterRingWidth * 3);
        roundW = roundW / 4 + mOuterRingWidth / 2;
        mPoints[0][0] = new Point(getX(0), y + roundW);
        mPoints[0][1] = new Point(getX(1), y + roundW);
        mPoints[0][2] = new Point(getX(2), y + roundW);
        mPoints[1][0] = new Point(getX(0), y + height / 2);
        mPoints[1][1] = new Point(getX(1), y + height / 2);
        mPoints[1][2] = new Point(getX(2), y + height / 2);
        mPoints[2][0] = new Point(getX(0), y + height - roundW);
        mPoints[2][1] = new Point(getX(1), y + height - roundW);
        mPoints[2][2] = new Point(getX(2), y + height - roundW);
        int k = 0;
        for (Point[] ps : mPoints) {
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
        if (mCircleSpacing == 0) {
            initCircleSpacing();
        } else {
            float mSpacing = mCircleSpacing * (3 + 1);
            mOuterRingWidth = (width - mSpacing) / 3;
        }
        if (mOuterRingWidth <= 0) {//防止手动设置圆圆之间间距过大问题
            initCircleSpacing();
        }
        if (mInnerRingWidth == 0 || mInnerRingWidth >= mOuterRingWidth) {
            mInnerRingWidth = mOuterRingWidth / 3;
        }
        if (mInnerBackgroundWidth == 0 || mInnerBackgroundWidth >= mOuterRingWidth) {
            mInnerBackgroundWidth = mInnerRingWidth * 1.3f;
        }
        mInnerBackgroundRadius = mInnerBackgroundWidth / 2;
        mRadius = mOuterRingWidth / 2;
        mInnerRingRadius = mInnerRingWidth / 2;
        mArrowLength = mRadius * 0.25f;//三角形的边长
    }


    /**
     * 当外圈间距没有设置时，初始化外圆之间的间距
     */
    private void initCircleSpacing() {
        // 计算每个GestureLockView的宽度
        mOuterRingWidth = width / 6;
        //计算每个GestureLockView的间距
        mCircleSpacing = (width - mOuterRingWidth * 3) / 4;
    }


    /**
     * 获取x点得坐标
     */
    public float getX(int i) {
        if (i == 0) {
            return mCircleSpacing + mRadius;
        } else if (i == 1) {
            return width / 2;
        }
        return mCircleSpacing * 3 + mOuterRingWidth * 2 + mRadius;

    }


    /**
     * 图像绘制
     *
     * @param canvas
     */
    private void drawToCanvas(Canvas canvas) {
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        if (isLineInTheRound) {
            // 画外圈
            drawAllOuterRing(canvas);
            // 画连线
            drawAllLine(canvas);
            // 画内圈
            drawAllInnerRing(canvas);
        } else {
            // 画连线
            drawAllLine(canvas);
            // 画外圈
            drawAllOuterRing(canvas);
            // 画所有点
            drawAllInnerRing(canvas);
        }
        // 是否绘制方向图标
        if (isShowArrow) {
            drawDirectionArrow(canvas);
        }
    }

    /**
     * 绘制解锁连接线
     *
     * @param canvas
     */
    private void drawAllLine(Canvas canvas) {
        if (sPoints.size() > 0) {
            Point tp = sPoints.get(0);
            for (int i = 1; i < sPoints.size(); i++) {
                //根据移动的方向绘制线
                Point p = sPoints.get(i);
                if (p.state == Point.STATE_CHECK_ERROR) {
                    drawErrorLine(canvas, tp, p);
                } else {
                    drawLine(canvas, tp, p);
                }
                tp = p;
            }
            if (this.movingNoPoint) {
                //到达下一个点停止移动绘制固定的方向
                drawLine(canvas, tp, new Point((int) moveingX + 20, (int) moveingY));
            }
        }
    }


    /**
     * 绘制解锁图案所有的点
     *
     * @param canvas
     */
    private void drawAllInnerRing(Canvas canvas) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
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
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
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
        if (sPoints.size() <= 0) {
            return;
        }
        Point tp = sPoints.get(0);
        for (int i = 1; i < sPoints.size(); i++) {
            //根据移动的方向绘制方向图标
            Point p = sPoints.get(i);
            if (p.state == Point.STATE_CHECK_ERROR) {
                drawDirectionArrow(canvas, tp, p, mColorErrorRing);
            } else {
                drawDirectionArrow(canvas, tp, p, mColorOnRing);
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
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制外圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColorOnRing);
        mPaint.setStrokeWidth(mOnStrokeWidth);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
    }

    /**
     * 绘制错误状态外圈
     *
     * @PARAM CANVAS
     * @PARAM P
     */
    private void onDrawErrorOuterRing(Canvas canvas, Point p) {
        // 绘制背景
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制圆圈
        mPaint.setColor(mColorErrorRing);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mOnStrokeWidth);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
    }


    /**
     * 绘制按下时内圈
     *
     * @param canvas
     */
    private void onDrawInnerRing(Canvas canvas, Point p) {
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, mInnerRingBackgroundColor);
        // 绘制内圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mInnerRingColor);
        canvas.drawCircle(p.x, p.y, mInnerRingRadius, mPaint);
    }

    /**
     * 绘制错误状态内圈
     *
     * @PARAM CANVAS
     * @PARAM P
     */
    private void onDrawErrorInnerRing(Canvas canvas, Point p) {
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, mInnerRingBackgroundErrorColor);
        // 绘制内圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mInnerRingErrorColor);
        canvas.drawCircle(p.x, p.y, mInnerRingRadius, mPaint);
    }


    /**
     * 绘制普通状态
     *
     * @param canvas
     */
    private void onDrawNoFinger(Canvas canvas, Point p) {
        // 绘制外圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColorUpRing);
        mPaint.setStrokeWidth(mNoFingerStrokeWidth);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
    }

    /**
     * 绘制内圆透明背景
     *
     * @param canvas
     */
    private void onDrawInnerCircleBackground(Canvas canvas, Point p, int color) {
        // 绘制内圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        canvas.drawCircle(p.x, p.y, mInnerBackgroundRadius, mPaint);
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
                moveingX = ex;
                moveingY = ey;
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
        if (this.sPoints.size() == 1) {
            this.reset();
            return;
        }
        if (this.sPoints.size() < passwordMinLength
                && this.sPoints.size() > 0) {
            error();
            if (mCompleteListener != null) {
                mCompleteListener.onPasswordIsShort(mode, passwordMinLength);  //密码太短
            }
            return;
        }
        if (this.sPoints.size() >= passwordMinLength) {
            int[] indexs = new int[sPoints.size()];
            for (int i = 0; i < sPoints.size(); i++) {
                indexs[i] = sPoints.get(i).index;
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
                moveingX = ex;
                moveingY = ey;
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
        this.sPoints.add(point);
    }

    /**
     * 检查点是否被选择
     *
     * @param x
     * @param y
     * @return
     */
    private Point checkSelectPoint(float x, float y) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (LockUtil.checkInRound(p.x, p.y, mRadius, (int) x, (int) y)) {
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
        if (sPoints.contains(p)) {
            if (sPoints.size() > 2) {
                // 与非最后一点重叠
                if (sPoints.get(sPoints.size() - 1).index != p.index) {
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
        for (Point p : sPoints) {
            p.state = Point.STATE_NORMAL;
        }
        sPoints.clear();
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
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mConnectingLineColor);
        mPaint.setStrokeWidth(mConnectingLineWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, mPaint);
    }

    /**
     * 错误线
     *
     * @param canvas
     * @param a
     * @param b
     */
    private void drawErrorLine(Canvas canvas, Point a, Point b) {
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mConnectingLineErrorColor);
        mPaint.setStrokeWidth(mConnectingLineWidth);
        canvas.drawLine(a.x, a.y, b.x, b.y, mPaint);
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
        Path mArrowPath = new Path();
        float offset = mInnerBackgroundRadius + (mArrowLength + mRadius - mInnerBackgroundRadius) / 2;//偏移量,定位三角形位置
        mArrowPath.moveTo(a.x, a.y - offset);
        mArrowPath.lineTo(a.x - mArrowLength, a.y - offset
                + mArrowLength);
        mArrowPath.lineTo(a.x + mArrowLength, a.y - offset
                + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(color);
        canvas.drawPath(mArrowPath, mPaint);
    }


    /**
     * 设置已经选中的为错误
     */
    private void error() {
        for (Point p : sPoints) {
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
            if (mCompleteListener != null) {
                mCompleteListener.onAginInputPassword(mode, password, indexs);
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
        if (mCompleteListener == null) {
            return;
        }
        if (mode == LockMode.SETTING_PASSWORD) {
            mCompleteListener.onEnteredPasswordsDiffer(mode);
        } else if (mode == LockMode.EDIT_PASSWORD && isEditVerify) {
            mCompleteListener.onEnteredPasswordsDiffer(mode);
        } else {
            errorNumber--;
            if (errorNumber <= 0) {
                mCompleteListener.onErrorNumberMany();
            } else {
                mCompleteListener.onError(mode, errorNumber + "");
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
        if (mCompleteListener == null) {
            return;
        }
        if (mode == LockMode.EDIT_PASSWORD && !isEditVerify) {//修改密码，旧密码正确，进行新密码设置
            mCompleteListener.onInputNewPassword(mode);
            isEditVerify = true;
            showTimes = 0;
            return;
        }
        if (mode == LockMode.EDIT_PASSWORD && isEditVerify) {
            savePassWord(password, indexs);
        } else if (mode == LockMode.CLEAR_PASSWORD) {//清除密码
            if (mCompleteListener != null) {
                mCompleteListener.clearPassword(mode, password, indexs);
            }
        } else if (mode == LockMode.SETTING_PASSWORD) {//完成密码设置，存储到本地
            savePassWord(password, indexs);
        } else {
            isEditVerify = false;
        }
        mCompleteListener.onComplete(mode, password, indexs);
    }

    /**
     * 存储密码到本地
     *
     * @param password
     */
    private void savePassWord(String password, int[] indexs) {
        if (mCompleteListener != null) {
            mCompleteListener.savePassword(mode, password, indexs);
        }
    }


    /**
     * 设置监听
     *
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnLockViewListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
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
    public interface OnLockViewListener {
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
        void onAginInputPassword(LockMode mode, String password, int[] indexs);


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
    public void setShowArrow(boolean isShowArrow) {
        this.isShowArrow = isShowArrow;
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
     * @param lineInTheRound
     */
    public CustomLockView setLineInTheRound(boolean lineInTheRound) {
        isLineInTheRound = lineInTheRound;
        return this;
    }

}
