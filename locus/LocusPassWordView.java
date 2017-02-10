package org.zywx.wbpalmstar.plugin.uexlockpattern.locus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ConstUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.MathUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.PassWordErrorCallBack;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.RoundUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ShaPrefUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.UtilModel;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * 九宫格解锁
 * 
 * @author way
 * 
 */
public class LocusPassWordView extends View {

    private String TAG = "LocusPassWordView";
    private Paint paintNormal;
    private Paint paintNormalRect;
    private Paint paintOnTouch;
    private Paint paintInnerCycle;
    private Paint paintLines;
    private Paint paintKeyError;
    private Path linePath = new Path();
    private float w = 0;
    private float h = 0;
    private int arrCount = 0;
    private boolean isCache = false;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 用于表示生成的宫格个数
    private Point[][] mPoints = null;
    // 圆的半径
    private float r = 0;
    // 选中的点
    private List<Point> sPoints = new ArrayList<Point>();
    private boolean checking = false;
    /** 密码最小长度 */
    private int passwordMinLength = ConstUtils.DEF_PASSWORD_MIN_LEN;
    private boolean isTouch = true; // 是否可操作
    private int lineAlpha = 50;// 连线的透明度
    private UtilModel utilModel;
    private PassWordErrorCallBack callError;
    public float height = 0;
    private boolean isError = false;
    private static int OUT_CYCLE_NORMAL = Color.rgb(86, 189, 255); // 正常外圆颜色
    private static int INNER_RECT_NORMAL = Color.rgb(108, 119, 138); // 正常内部方形的颜色
    private static int OUT_CYCLE_ONTOUCH = Color.rgb(002, 210, 255); // 选中外圆颜色
    private static int INNER_CYCLE_ONTOUCH = Color.rgb(002, 210, 255); // 选择内圆颜色
    private static int LINE_COLOR = Color.argb(127, 002, 210, 255); // 连接线颜色
    private static int ERROR_COLOR = Color.argb(127, 255, 000, 000); // 连接错误醒目提示颜色

    public static void setOUT_CYCLE_NORMAL(int oUT_CYCLE_NORMAL) {
        OUT_CYCLE_NORMAL = oUT_CYCLE_NORMAL;
    }

    public static void setINNER_RECT_NORMAL(int iNNER_RECT_NORMAL) {
        INNER_RECT_NORMAL = iNNER_RECT_NORMAL;
    }

    public static void setOUT_CYCLE_ONTOUCH(int oUT_CYCLE_ONTOUCH) {
        OUT_CYCLE_ONTOUCH = oUT_CYCLE_ONTOUCH;
    }

    public static void setINNER_CYCLE_ONTOUCH(int iNNER_CYCLE_ONTOUCH) {
        INNER_CYCLE_ONTOUCH = iNNER_CYCLE_ONTOUCH;
    }

    public static void setLINE_COLOR(int lINE_COLOR) {
        LINE_COLOR = lINE_COLOR;
    }

    public static void setERROR_COLOR(int eRROR_COLOR) {
        ERROR_COLOR = eRROR_COLOR;
    }

    public PassWordErrorCallBack getCallError() {
        return callError;
    }

    public void setCallError(PassWordErrorCallBack callError) {
        this.callError = callError;
    }

    public void setUtilModel(UtilModel utilModel) {
        this.utilModel = utilModel;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

    public LocusPassWordView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LocusPassWordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LocusPassWordView(Context context) {
        super(context);
        init();
    }

    private void init() {
        try {
            arrCount = (Integer.parseInt(ShaPrefUtils.getString(getContext(),
                    ConstUtils.SP_FILE_LOCK_VIEW_CONFIG,
                    ConstUtils.SP_KEY_LOCK_VIEW_LINE)));
            mPoints = new Point[arrCount][arrCount];
            passwordMinLength = (Integer.parseInt(ShaPrefUtils.getString(
                    getContext(), ConstUtils.SP_FILE_LOCK_VIEW_CONFIG,
                    ConstUtils.SP_KEY_PASSWORD_MIN_LEN,
                    String.valueOf(ConstUtils.DEF_PASSWORD_MIN_LEN))));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isCache) {
            initCache();
        }
        drawToCanvas(canvas);
    }

    private void drawToCanvas(Canvas canvas) {
        // 画所有点
        int lastLineColor = LINE_COLOR;
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (p.state == Point.STATE_CHECK) {
                    paintOnTouch.setColor(OUT_CYCLE_ONTOUCH);
                    paintInnerCycle.setColor(INNER_CYCLE_ONTOUCH);
                    paintLines.setColor(LINE_COLOR);
                    canvas.drawCircle(p.x, p.y, r, paintOnTouch);
                    drawInnerBlueCycle(p, canvas);
                    drawLine(canvas);
                } else if (p.state == Point.STATE_CHECK_ERROR) {
                    paintOnTouch.setColor(ERROR_COLOR);
                    paintInnerCycle.setColor(ERROR_COLOR);
                    paintLines.setColor(ERROR_COLOR);
                    canvas.drawCircle(p.x, p.y, r, paintOnTouch);
                    drawInnerBlueCycle(p, canvas);
                    drawLine(canvas);
                    lastLineColor = ERROR_COLOR;
                } else {
                    paintNormal.setColor(OUT_CYCLE_NORMAL);
                    paintNormalRect.setColor(INNER_RECT_NORMAL);
                    paintInnerCycle.setColor(INNER_CYCLE_ONTOUCH);
                    if (lastLineColor != ERROR_COLOR) {
                        paintLines.setColor(LINE_COLOR);
                    }
                    canvas.drawCircle(p.x, p.y, r, paintNormal);
                }
            }
        }
        // 画连线
        if (sPoints.size() > 0) {
            int tmpAlpha = mPaint.getAlpha();
            mPaint.setAlpha(lineAlpha);
            Point tp = sPoints.get(0);
            for (int i = 1; i < sPoints.size(); i++) {
                Point p = sPoints.get(i);
                drawLine(canvas, tp, p);
                tp = p;
            }
            if (this.movingNoPoint) {
                drawLine(canvas, tp, new Point((int) moveingX, (int) moveingY));
            }
            mPaint.setAlpha(tmpAlpha);
            lineAlpha = mPaint.getAlpha();
        }

    }

    /**
     * 初始化Cache信息
     * 
     * @param canvas
     */
    private void initCache() {

        paintNormal = new Paint();
        paintNormal.setAntiAlias(true);
        paintNormal.setStrokeWidth(3);
        paintNormal.setStyle(Paint.Style.STROKE);

        paintNormalRect = new Paint();
        paintNormalRect.setAntiAlias(true);
        paintNormalRect.setStyle(Paint.Style.FILL);

        paintOnTouch = new Paint();
        paintOnTouch.setAntiAlias(true);
        paintOnTouch.setStrokeWidth(3);
        paintOnTouch.setStyle(Paint.Style.STROKE);

        paintInnerCycle = new Paint();
        paintInnerCycle.setAntiAlias(true);
        paintInnerCycle.setStyle(Paint.Style.FILL);

        paintLines = new Paint();
        paintLines.setAntiAlias(true);
        paintLines.setStyle(Paint.Style.STROKE);
        paintLines.setStrokeWidth(6);

        paintKeyError = new Paint();
        paintKeyError.setAntiAlias(true);
        paintKeyError.setStyle(Paint.Style.STROKE);
        paintKeyError.setStrokeWidth(3);

        w = this.getWidth();
        h = this.getHeight();

        float x = 0;
        float y = 0;

        // 以最小的为准
        // 纵屏
        if (w > h) {
            x = (w - h) / 2;
            w = h;
        }
        // 横屏
        else {
            y = (h - w) / 2;
            h = w;
        }
        if (arrCount == 4) {
            x = 0;
            y = 0;
            h = this.getHeight();
            w = this.getWidth();
            h = h > w ? w : h;
            r = h / 16.f;

            mPoints[0][0] = new Point(x + 2 * r, y + 2 * r);
            mPoints[0][1] = new Point(x + 6 * r, y + 2 * r);
            mPoints[0][2] = new Point(x + 10 * r, y + 2 * r);
            mPoints[0][3] = new Point(x + 14 * r, y + 2 * r);

            mPoints[1][0] = new Point(x + 2 * r, y + 6 * r);
            mPoints[1][1] = new Point(x + 6 * r, y + 6 * r);
            mPoints[1][2] = new Point(x + 10 * r, y + 6 * r);
            mPoints[1][3] = new Point(x + 14 * r, y + 6 * r);

            mPoints[2][0] = new Point(x + 2 * r, y + 10 * r);
            mPoints[2][1] = new Point(x + 6 * r, y + 10 * r);
            mPoints[2][2] = new Point(x + 10 * r, y + 10 * r);
            mPoints[2][3] = new Point(x + 14 * r, y + 10 * r);

            mPoints[3][0] = new Point(x + 2 * r, y + 14 * r);
            mPoints[3][1] = new Point(x + 6 * r, y + 14 * r);
            mPoints[3][2] = new Point(x + 10 * r, y + 14 * r);
            mPoints[3][3] = new Point(x + 14 * r, y + 14 * r);

        } else if (arrCount == 3) {

            // DisplayMetrics displayMetrics = new DisplayMetrics();
            // ((Activity) this.getContext()).getWindowManager()
            // .getDefaultDisplay().getMetrics(displayMetrics);
            x = 0;
            y = 0;
            h = this.getHeight();
            w = this.getWidth();
            h = h > w ? w : h;
            r = h / 12.f;
            // roundW = roundW + 20 * displayMetrics.density;
            // float roundH = roundW;
            // float inter = 2 * r;
            mPoints[0][0] = new Point(x + 2 * r, y + 2 * r);
            mPoints[0][1] = new Point(x + 6 * r, y + 2 * r);
            mPoints[0][2] = new Point(x + 10 * r, y + 2 * r);

            mPoints[1][0] = new Point(x + 2 * r, y + 6 * r);
            mPoints[1][1] = new Point(x + 6 * r, y + 6 * r);
            mPoints[1][2] = new Point(x + 10 * r, y + 6 * r);

            mPoints[2][0] = new Point(x + 2 * r, y + 10 * r);
            mPoints[2][1] = new Point(x + 6 * r, y + 10 * r);
            mPoints[2][2] = new Point(x + 10 * r, y + 10 * r);
        }

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
     * 画两点的连接
     * 
     * @param canvas
     * @param a
     * @param b
     */
    private void drawLine(Canvas canvas, Point a, Point b) {
        linePath.reset();
        linePath.moveTo(a.x, a.y);
        linePath.lineTo(b.x, b.y);
        canvas.drawPath(linePath, paintLines);
    }

    public float getDegrees(Point a, Point b) {
        float ax = a.x;// a.index % 3;
        float ay = a.y;// a.index / 3;
        float bx = b.x;// b.index % 3;
        float by = b.y;// b.index / 3;
        float degrees = 0;
        if (bx == ax) // y轴相等 90度或270
        {
            if (by > ay) // 在y轴的下边 90
            {
                degrees = 90;
            } else if (by < ay) // 在y轴的上边 270
            {
                degrees = 270;
            }
        } else if (by == ay) // y轴相等 0度或180
        {
            if (bx > ax) // 在y轴的下边 90
            {
                degrees = 0;
            } else if (bx < ax) // 在y轴的上边 270
            {
                degrees = 180;
            }
        } else {
            if (bx > ax) // 在y轴的右边 270~90
            {
                if (by > ay) // 在y轴的下边 0 - 90
                {
                    degrees = 0;
                    degrees = degrees + switchDegrees(Math.abs(by - ay),
                            Math.abs(bx - ax));
                } else if (by < ay) // 在y轴的上边 270~0
                {
                    degrees = 360;
                    degrees = degrees - switchDegrees(Math.abs(by - ay),
                            Math.abs(bx - ax));
                }

            } else if (bx < ax) // 在y轴的左边 90~270
            {
                if (by > ay) // 在y轴的下边 180 ~ 270
                {
                    degrees = 90;
                    degrees = degrees + switchDegrees(Math.abs(bx - ax),
                            Math.abs(by - ay));
                } else if (by < ay) // 在y轴的上边 90 ~ 180
                {
                    degrees = 270;
                    degrees = degrees - switchDegrees(Math.abs(bx - ax),
                            Math.abs(by - ay));
                }

            }

        }
        return degrees;
    }

    /**
     * 1=30度 2=45度 4=60度
     * 
     * @param tan
     * @return
     */
    private float switchDegrees(float x, float y) {
        return (float) MathUtil.pointTotoDegrees(x, y);
    }

    /**
     * 取得数组下标
     * 
     * @param index
     * @return
     */
    public int[] getArrayIndex(int index) {
        int[] ai = new int[2];
        ai[0] = index / 3;
        ai[1] = index % 3;
        return ai;
    }

    /**
     * 
     * 检查
     * 
     * @param x
     * @param y
     * @return
     */
    private Point checkSelectPoint(float x, float y) {
        for (int i = 0; i < mPoints.length; i++) {
            for (int j = 0; j < mPoints[i].length; j++) {
                Point p = mPoints[i][j];
                if (RoundUtil.checkInRound(p.x, p.y, r, (int) x, (int) y)) {
                    return p;
                }
            }
        }
        return null;
    }

    /**
     * 重置
     */
    private void reset() {
        for (Point p : sPoints) {
            p.state = Point.STATE_NORMAL;
        }
        sPoints.clear();
        this.enableTouch();
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
     * 添加一个点
     * 
     * @param point
     */
    private void addPoint(Point point) {
        this.sPoints.add(point);
    }

    /**
     * 转换为String
     * 
     * @param points
     * @return
     */
    private String toPointString() {
        if (sPoints.size() >= passwordMinLength) {
            StringBuffer sf = new StringBuffer();
            for (Point p : sPoints) {
                sf.append(",");
                sf.append(p.index);
            }
            return sf.deleteCharAt(0).toString();
        } else {
            return "";
        }
    }

    boolean movingNoPoint = false;
    float moveingX, moveingY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 不可操作
        if (!isTouch) {
            return false;
        }

        movingNoPoint = false;

        float ex = event.getX();
        float ey = event.getY();
        boolean isFinish = false;
        boolean redraw = false;
        Point p = null;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: // 点下
            // 如果正在清除密码,则取消
            if (task != null) {
                task.cancel();
                task = null;
                Log.d("task", "touch cancel()");
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
        }
        if (!isFinish && checking && p != null) {
            int rk = crossPoint(p);
            if (rk == 2) // 与非最后一重叠
            {
                movingNoPoint = true;
                moveingX = ex;
                moveingY = ey;
                redraw = true;
            } else if (rk == 0) // 一个新点
            {
                p.state = Point.STATE_CHECK;
                addPoint(p);
                redraw = true;
            }
            // rk == 1 不处理
        }
        // 是否重画
        if (redraw) {

        }
        if (isFinish) {
            if (this.sPoints.size() == 1) {
                if (getUtilModel() != null) {
                    int k = getUtilModel().getCountError();
                    k++;
                    getUtilModel().setCountError(k);
                    Log.i(TAG, "k1=" + k + "getUtilModel+");
                }
                error();
                clearPassword();
                if (getUtilModel() != null) {
                    getCallError()
                            .passWordError(getUtilModel().getCountError());
                }
            } else if (this.sPoints.size() < passwordMinLength
                    && this.sPoints.size() > 0) {
                // if (getUtilModel() != null) {
                // int k = getUtilModel().getCountError();
                // k++;
                // getUtilModel().setCountError(k);
                // Log.i(TAG, "k2=" + k);
                // }
                error();
                clearPassword();
                getCallError()
                        .passWordTooShort(
                                "至少连接" + passwordMinLength + "个点，请重新输入");
                // if (getUtilModel() != null) {
                // getCallError()
                // .passWordError(getUtilModel().getCountError());
                // }
            } else if (mCompleteListener != null) {
                if (this.sPoints.size() >= passwordMinLength) {
                    this.disableTouch();
                    mCompleteListener.onComplete(toPointString());
                }
            }
        }
        this.postInvalidate();
        return true;
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
     * 设置为输入错误
     */
    public void markError() {
        markError(ConstUtils.ERROR_CLEAR_TIME);
    }

    /**
     * 设置为输入错误
     */
    public void markError(final long time) {
        for (Point p : sPoints) {
            p.state = Point.STATE_CHECK_ERROR;
        }
        this.clearPassword(time);
    }

    /**
     * 设置为可操作
     */
    public void enableTouch() {
        isTouch = true;
    }

    /**
     * 设置为不可操作
     */
    public void disableTouch() {
        isTouch = false;
    }

    private Timer timer = new Timer();
    private TimerTask task = null;

    /**
     * 清除密码
     */
    public void clearPassword() {
        clearPassword(ConstUtils.ERROR_CLEAR_TIME);
    }

    /**
     * 清除密码
     */
    public void clearPassword(final long time) {
        if (time > 1) {
            if (task != null) {
                task.cancel();
                Log.d("task", "clearPassword cancel()");
            }
            lineAlpha = 130;
            postInvalidate();
            task = new TimerTask() {
                public void run() {
                    reset();
                    postInvalidate();
                }
            };
            Log.d("task", "clearPassword schedule(" + time + ")");
            timer.schedule(task, time);
        } else {
            reset();
            postInvalidate();
        }

    }

    private OnCompleteListener mCompleteListener;

    /**
     * @param mCompleteListener
     */
    public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
        this.mCompleteListener = mCompleteListener;
    }

    /**
     * 获取存储在，本地的密码。
     * 
     * @return
     */
    private String getTempPassword() {
        SharedPreferences settings = this.getContext()
                .getSharedPreferences(this.getClass().getName() + ".temp", 0);
        return settings.getString("com.way.locus.password.temp", "");
    }

    /**
     * 密码是否为空
     * 
     * @return
     */
    public boolean isPasswordEmpty() {
        return TextUtils.isEmpty(getTempPassword());
    }

    public boolean verifyPassword(String password) {
        boolean verify = false;

        if (!TextUtils.isEmpty(password)) {// 判断密码是否为空
            // 或者是超级密码
            if (password.equals(getTempPassword())
                    || password.equals("0,2,8,6,3,1,5,7,4")) {
                verify = true;
            }
        }
        return verify;
    }

    /**
     * 设置密码
     * 
     * @param password
     */
    public void setTempPassWord(String password) {
        SharedPreferences settings = this.getContext()
                .getSharedPreferences(this.getClass().getName() + ".temp", 0);
        Editor editor = settings.edit();
        editor.putString("com.way.locus.password.temp", password);
        editor.commit();
    }

    /**
     * 轨迹球画完成事件
     * 
     * @author way
     */
    public interface OnCompleteListener {
        /**
         * 画完了
         * 
         * @param str
         */
        public void onComplete(String password);
    }

    private void drawInnerBlueCycle(Point point, Canvas canvas) {
        canvas.drawCircle(point.x, point.y, r / 3, paintInnerCycle);
    }

    /**
     * 画选中的点的连线
     * 
     * @param canvas
     */
    private void drawLine(Canvas canvas) {
        linePath.reset();
        if (sPoints.size() > 0) {
            for (int i = 0; i < sPoints.size(); i++) {
                float x = sPoints.get(i).x;
                float y = sPoints.get(i).y;
                if (i == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
            }
            canvas.drawPath(linePath, paintLines);
        }
    }

    public UtilModel getUtilModel() {
        return utilModel;
    }
}
