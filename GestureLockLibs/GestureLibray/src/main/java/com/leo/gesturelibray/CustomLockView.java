package com.leo.gesturelibray;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.leo.gesturelibray.crypto.Base64;
import com.leo.gesturelibray.entity.Point;
import com.leo.gesturelibray.util.LockUtil;
import com.leo.gesturelibray.util.MathUtil;
import com.leo.gesturelibray.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;


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
    //是否可操作
    private boolean isTouch = true;
    //密码最小长度
    private int passwordMinLength = 3;
    //判断是否触摸屏幕
    private boolean checking = false;
    //刷新
    private TimerTask task = null;
    //计时器
    private Timer timer = new Timer();
    //监听
    private OnCompleteListener mCompleteListener;
    //清除痕迹的时间
    private long CLEAR_TIME = 0;
    //错误限制 默认为4次
    private int errorTimes = 4;
    //记录上一次滑动的密码
    private String oldPassword = null;
    //记录当前第几次触发 默认为0次
    private int showTimes = 0;
    //当前密码是否正确 默认为正确
    private boolean isCorrect = true;
    //是否显示滑动方向 默认为显示
    private boolean isShow = true;
    //验证或者设置 0:设置 1:验证
    private int status = 0;
    //用于执行清除界面
    private Handler handler = new Handler();
    //间距
    float roundW;
    //普通状态下圈的颜色
    private int mColorOrdinaryRing = 0xFF378FC9;
    //按下时圈的颜色
    private int mColorOnRing = 0xFF378FC9;
    //松开手时的颜色
    private int mColorErrorRing = 0xFF378FC9;

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
    private float mInnerBackgroudRadius;
    //内圆背景大小（半透明内圆）
    private float mInnerBackgroudWidth;
    //三角形边长
    private float mArrowLength;
    //未按下时圆圈的边宽
    private int mNoFingerStrokeWidth = 2;
    //按下时圆圈的边宽
    private int mOnStrokeWidth = 4;

    //用于定时执行清除界面
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(run);
            reset();
            postInvalidate();
        }
    };
    private String errorStr = "";

    public CustomLockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomLockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.GestureLock_styleable, defStyleAttr, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.GestureLock_styleable_color_on_ring) {
                mColorOnRing = a.getColor(attr, mColorOnRing);
            } else if (attr == R.styleable.GestureLock_styleable_color_ordinary_ring) {
                mColorOrdinaryRing = a.getColor(attr, mColorOrdinaryRing);
            } else if (attr == R.styleable.GestureLock_styleable_color_error_ring) {
                mColorErrorRing = a.getColor(attr, mColorErrorRing);
            } else if (attr == R.styleable.GestureLock_styleable_inner_ring_width) {
                mInnerRingWidth = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.GestureLock_styleable_outer_ring_spacing_width) {
                mCircleSpacing = a.getDimensionPixelSize(attr, 0);
            } else if (attr == R.styleable.GestureLock_styleable_inner_background_width) {
                mInnerBackgroudWidth = a.getDimensionPixelSize(attr, 0);
            }
        }
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
        // 计算圆圈图片的大小
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
            // 计算每个GestureLockView的宽度
            mOuterRingWidth = width / 6;
            //计算每个GestureLockView的间距
            mCircleSpacing = (width - mOuterRingWidth * 3) / 4;
        } else {
            float mSpacing = mCircleSpacing * (3 + 1);
            mOuterRingWidth = (width - mSpacing) / 3;
        }
        if (mInnerRingWidth == 0) {
            mInnerRingWidth = mOuterRingWidth / 3;
        }
        if (mInnerBackgroudWidth == 0) {
            mInnerBackgroudWidth = mInnerRingWidth * 1.3f;
        }
        mInnerBackgroudRadius = mInnerBackgroudWidth / 2;
        mRadius = mOuterRingWidth / 2;
        mInnerRingRadius = mInnerRingWidth / 2;
        mArrowLength = mRadius * 0.25f;//三角形的边长
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
        // 画连线
        if (sPoints.size() > 0) {
            Point tp = sPoints.get(0);
            for (int i = 1; i < sPoints.size(); i++) {
                //根据移动的方向绘制线
                Point p = sPoints.get(i);
                if (isCorrect) {
                    drawLine(canvas, tp, p);
                } else {
                    drawErrorLine(canvas, tp, p);
                }
                tp = p;
            }
            if (this.movingNoPoint) {
                //到达下一个点停止移动绘制固定的方向
                drawLine(canvas, tp, new Point((int) moveingX + 20, (int) moveingY));
            }
        }
        // 画所有点
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (p != null) {
                    if (p.state == Point.STATE_CHECK) {
                        onDrawOn(canvas, p);
                    } else if (p.state == Point.STATE_CHECK_ERROR) {
                        onDrawError(canvas, p);
                    } else {
                        onDrawNoFinger(canvas, p);
                    }
                }
            }
        }
        if (isShow) {
            // 绘制方向图标
            if (sPoints.size() > 0) {
                Point tp = sPoints.get(0);
                for (int i = 1; i < sPoints.size(); i++) {
                    //根据移动的方向绘制方向图标
                    Point p = sPoints.get(i);
                    if (isCorrect) {
                        drawDirectionArrow(canvas, tp, p, mColorOnRing);
                    } else {
                        drawDirectionArrow(canvas, tp, p, mColorErrorRing);
                    }
                    tp = p;
                }
            }
        }
    }


    /**
     * 绘制按下时状态
     *
     * @param canvas
     */
    private void onDrawOn(Canvas canvas, Point p) {
        // 绘制背景
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制外圆
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColorOnRing);
        mPaint.setStrokeWidth(mOnStrokeWidth);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, mColorOnRing);
        // 绘制内圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColorOnRing);
        canvas.drawCircle(p.x, p.y, mInnerRingRadius, mPaint);
    }


    /**
     * 绘制松开手时状态
     *
     * @param canvas
     */
    private void onDrawError(Canvas canvas, Point p) {
        // 绘制背景
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制圆圈
        mPaint.setColor(mColorOnRing);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mOnStrokeWidth);
        canvas.drawCircle(p.x, p.y, mRadius, mPaint);
        // 绘制内圆背景
        onDrawInnerCircleBackground(canvas, p, mColorOnRing);
        // 绘制内圆
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColorOnRing);
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
        mPaint.setColor(mColorOrdinaryRing);
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
        mPaint.setAlpha(100);
        canvas.drawCircle(p.x, p.y, mInnerBackgroudRadius, mPaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可操作
//        if (!isTouch) {
//            return false;
//        }
        if (errorTimes <= 0) {
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
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                // 删除之前的点
                reset();
                p = checkSelectPoint(ex, ey);
                if (p != null) {
                    checking = true;
                }
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                if (checking) {
                    p = checkSelectPoint(ex, ey);
                    if (p == null) {
                        movingNoPoint = true;
                        moveingX = ex;
                        moveingY = ey;
                    }
                }
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
            handler.postDelayed(run, 500);
            if (this.sPoints.size() == 1) {
                this.reset();
            } else if (this.sPoints.size() < passwordMinLength
                    && this.sPoints.size() > 0) {
                clearPassword();
                if (mCompleteListener != null) {
                    //密码太短
                    mCompleteListener.onPasswordIsShort(passwordMinLength);
                }
            } else if (mCompleteListener != null) {
                if (this.sPoints.size() >= passwordMinLength) {
                    int[] indexs = new int[sPoints.size()];
                    for (int i = 0; i < sPoints.size(); i++) {
                        indexs[i] = sPoints.get(i).index;
                    }
                    if (status == 0) {
                        invalidatePass(Base64.encryptionString(indexs), indexs);
                    } else if (status == 1) {
                        invalidateOldPsw(Base64.encryptionString(indexs), indexs);
                    }
                }
            }
        }
        postInvalidate();
        return true;
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
    public void reset() {
        for (Point p : sPoints) {
            p.state = Point.STATE_NORMAL;
        }
        sPoints.clear();
    }

    /**
     * 清空当前信息
     */
    public void clearCurrent() {
        showTimes = 0;
        errorTimes = 4;
        isCorrect = true;
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
        int color = R.color.red_fa4;
        mPaint.setColor(getResources().getColor(color));
        mPaint.setStrokeWidth(3);
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
        mPaint.setColor(mColorErrorRing);
        mPaint.setStrokeWidth(3);
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
        float offset = mInnerBackgroudRadius + (mArrowLength + mRadius - mInnerBackgroudRadius) / 2;//偏移量,定位三角形位置
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
     * 清除密码
     */
    private void clearPassword() {
        clearPassword(CLEAR_TIME);
    }

    /**
     * 清除密码
     */
    private void clearPassword(final long time) {
        if (time > 1) {
            if (task != null) {
                task.cancel();
            }
            postInvalidate();
            task = new TimerTask() {
                public void run() {
                    reset();
                    postInvalidate();
                }
            };
            timer.schedule(task, time);
        } else {
            reset();
            postInvalidate();
        }
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
    private void invalidatePass(String password, int[] indexs) {
        if (showTimes == 0) {
            oldPassword = password;
            mCompleteListener.onComplete(password, indexs);
            showTimes++;
            reset();
        } else if (showTimes == 1) {
            if (oldPassword != null && oldPassword.length() == password.length()) {
                if (!StringUtils.isEquals(oldPassword, password)) {
                    isCorrect = false;
                }
            } else {
                isCorrect = false;
            }
            if (!isCorrect) {
                error();
                if (mCompleteListener != null) {
                    mCompleteListener.onError(errorTimes);
                }
                postInvalidate();
            } else {
                if (mCompleteListener != null) {
                    mCompleteListener.onComplete(password, indexs);
                }
            }
        }
    }

    /**
     * 验证本地密码与当前滑动密码是否相同
     *
     * @param indexs
     */
    private void invalidateOldPsw(String password, int[] indexs) {
        if (oldPassword != null && password.length() == oldPassword.length()) {
            if (!StringUtils.isEquals(oldPassword, password)) {
                isCorrect = false;
            }
        } else {
            isCorrect = false;
        }
        if (!isCorrect) {
            errorTimes--;
            error();
            if (mCompleteListener != null) {
                mCompleteListener.onError(errorTimes);
            }
            postInvalidate();
        } else {
            if (mCompleteListener != null) {
                mCompleteListener.onComplete(password, indexs);
            }
        }
    }

    /**
     * 设置监听
     *
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    /**
     * 轨迹球画完监听事件
     */
    public interface OnCompleteListener {
        /**
         * 画完了
         */
        public void onComplete(String password, int[] indexs);

        /**
         * 绘制错误
         */
        public void onError(int errorTimes);

        /**
         * 密码太短
         */
        public void onPasswordIsShort(int passwordMinLength);

    }

    public int getErrorTimes() {
        return errorTimes;
    }

    public void setErrorTimes(int errorTimes) {
        this.errorTimes = errorTimes;
    }


    //是否已经设置过密码
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    //是否显示连接方向
    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    //设置已经设置过的密码
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    //最少密码长度
    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        this.passwordMinLength = passwordMinLength;
    }
}
