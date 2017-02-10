package org.zywx.wbpalmstar.plugin.uexlockpattern;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexlockpattern.locus.LocusPassWordView;
import org.zywx.wbpalmstar.plugin.uexlockpattern.locus.LoginActivity;
import org.zywx.wbpalmstar.plugin.uexlockpattern.locus.SetPasswordActivity;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ConstUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ShaPrefUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.StringUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.Utils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.vo.SetColorsVO;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

@SuppressWarnings({"deprecation", "unused"})
public class EUExLockPattern extends EUExBase {
    // 服务器端已定义
    static final String func_on_success = "uexLockPattern.cbLoginSuccess";
    static final String func_on_SET = "";
    static final String func_on_GET = "uexLockPattern.cbGet";
    static final String func_on_fail = "uexLockPattern.cbLoginFail";
    // 失败次数回调
    static final String func_on_failcount = "uexLockPattern.cbFailCount";
    // 提示语回调
    static final String func_on_toast_string = "uexLockPattern.cbToast";

    static final String func_on_forget = "uexLockPattern.cbForgetPassword";
    static final String func_on_other = "uexLockPattern.cbUseOther";

    static final String TAG = "uexLockPattern";
    private LoginActivity mActivityLockPattern;
    private static View mLockPatternDecorView;
    private String number;
    private boolean isOpen = false;
    public static ConstUtils con;
    private boolean isLoginTrueOrFalse = false;

    public String isInit = "";

    private int errorCount;

    private int countK = 0;

    private String longinStr;

    private static final String KEY_APP = "AppCanZYWX";

    private WWidgetData widgetData;

    public static LocalActivityManager mgr;

    /**
     * heighth：距离屏幕顶部的高度 width：屏幕较小的宽度
     */
    private int heighth;

    public EUExLockPattern(Context context, EBrowserView view) {
        super(context, view);
        widgetData = view.getCurrentWidget();
    }

    @SuppressWarnings("serial")
    public void initCon() {
        con = new ConstUtils() {

            @Override
            public void getResult(String result) {
                if (result != null) {
                    jsCallback(func_on_success, 0, EUExCallback.F_C_TEXT,
                            result);
                }
            }

            @Override
            public void getError(String error) {
                String error1 = ShaPrefUtils.getString(mContext, ERRORNAME,
                        ERRORNAME);
                if (error1 != null)
                    errorCount = Integer.parseInt(error1);

                if (error != null)
                    jsCallback(func_on_fail, 0, EUExCallback.F_C_TEXT, error);
            }

            @Override
            public void getForget() {
                jsCallback(func_on_forget, 0, EUExCallback.F_C_TEXT, "");
            }

            @Override
            public void getother() {
                jsCallback(func_on_other, 0, EUExCallback.F_C_TEXT, "");
            }

            @Override
            public void getErrorCount(String errorCount) {
                jsCallback(func_on_failcount, 0, EUExCallback.F_C_TEXT,
                        errorCount);

            }

            @Override
            public void getToast(String toast) {
                jsCallback(func_on_toast_string, 0, EUExCallback.F_C_TEXT,
                        toast);
            }

        };
    }

    /**
     * @param parms：hight、lengh、errorMaxCount、 password(可不传)、lockPassConfig
     */
    public void lock(String[] parms) {
        // long aTime = new Date().getTime();
        Log.i("lockaaaa", "lock");
        if (parms.length < 1) {
            return;
        }

        heighth = Integer.parseInt(parms[0]);
        if (parms.length >= 3) {
            try {
                number = parms[1];
                ShaPrefUtils.putString(mContext, number,
                        ConstUtils.SP_FILE_LOCK_VIEW_CONFIG,
                        ConstUtils.SP_KEY_LOCK_VIEW_LINE);
                ShaPrefUtils.putString(mContext, parms[2], ConstUtils.ERRORNAME,
                        ConstUtils.ERRORNAME);
                String lockPatternConfig = "";
                String password = "";
                switch (parms.length) {
                    /** 密码和配置json都可不传，所以当有4个参数时，第4个参数可能是密码，也肯能是配置json */
                    case 4:
                        if (!StringUtils.isJson(parms[3])) {
                            password = parms[3];
                        } else {
                            lockPatternConfig = parms[3];
                        }
                        break;
                    case 5:
                        password = parms[3];
                        lockPatternConfig = parms[4];
                        break;
                    default:
                        break;
                }
                Utils.saveLockPatternConfig(mContext, lockPatternConfig);
                if (!TextUtils.isEmpty(password)) {
                    ShaPrefUtils.putString(mContext, password,
                            ConstUtils.FILLOCKNAME, ConstUtils.KEYELOCKNAME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            String lock = ShaPrefUtils.getString(mContext,
                    ConstUtils.FILLOCKNAME, ConstUtils.KEYELOCKNAME);
            if (lock != null) {
                try {
                    isOpen = true;
                    isLoginTrueOrFalse = true;
                    if (con != null) {
                        con = null;
                    }
                    initCon();
                    longinStr = ConstUtils.LOCK;
                    openLoclPatter();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // Toast.makeText(mContext, "inittime" + (new Date().getTime() - aTime),
        // Toast.LENGTH_SHORT).show();
    }

    // Get获取手势键盘内部保存的字符串数据
    public void get(String[] parms) {
        if (parms.length != 0) {
            return;
        }

        String getPasswordData = ShaPrefUtils.getString(mContext,
                this.getClass().getName(), ConstUtils.SETORGETNAME);
        if (con != null) {
            con = null;
        }
        initCon();

        String getEnStr = AESDecode.decode(KEY_APP, getPasswordData);
        Log.i(TAG, "com.way.locus.setPasswordData:" + getEnStr + "=="
                + getPasswordData);
        Log.i(TAG,
                "com.way.locus.setPasswordData" + ShaPrefUtils.getString(
                        mContext, this.getClass().getName(),
                        ConstUtils.SETORGETNAME));
        jsCallback(func_on_GET, 0, EUExCallback.F_C_TEXT, getEnStr);
    }

    // Set存取手势键盘内部保存的字符串数据
    public void set(String[] parms) {
        Log.i("xindabao", "set=" + "set1");
        if (parms.length < 1) {
            return;
        }
        String setStr = parms[0];
        // aes加密
        String enSetStr = AESDecode.encode(KEY_APP, setStr);

        ShaPrefUtils.putString(mContext, enSetStr, this.getClass().getName(),
                ConstUtils.SETORGETNAME);

        initCon();
    }

    public void init(String[] parms) {
        // long aTime = new Date().getTime();
        if (parms.length >= 3) {
            heighth = Integer.parseInt(parms[0]);
            number = parms[1];
            String loginErrorCount = parms[2];
            String lockPatternConfig = "";
            if (parms.length == 4) {
                lockPatternConfig = parms[3];
            }
            Utils.saveLockPatternConfig(mContext, lockPatternConfig);

            getInit();
            ShaPrefUtils.putString(mContext, loginErrorCount,
                    ConstUtils.ERRORNAME, ConstUtils.ERRORNAME);
            try {
                // clearView();
                isOpen = true;
                String strData = ShaPrefUtils.getString(mContext,
                        ConstUtils.FILECONTNAME, ConstUtils.KEYECONTNAME);
                if (strData == null) {
                    ShaPrefUtils.putString(mContext, "" + 0,
                            ConstUtils.FILECONTNAME, ConstUtils.KEYECONTNAME);
                }
                countK = Integer.parseInt(ShaPrefUtils.getString(mContext,
                        ConstUtils.FILECONTNAME, ConstUtils.KEYECONTNAME));
                countK++;
                ShaPrefUtils.putString(mContext, "" + countK,
                        ConstUtils.FILECONTNAME, ConstUtils.KEYECONTNAME);
                ShaPrefUtils.putString(mContext, number,
                        ConstUtils.SP_FILE_LOCK_VIEW_CONFIG,
                        ConstUtils.SP_KEY_LOCK_VIEW_LINE);
                Utils.saveLockPatternConfig(mContext, lockPatternConfig);
                longinStr = ConstUtils.INIT;
                if (con != null) {
                    con = null;
                }
                initCon();
                openLoclPatter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @param parms
     * @deprecated
     */
    public void setImg(String[] parms) {
        if (parms.length != 1) {
            return;
        }

        try {
            Thread.sleep(3 * 500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        try {
            if (mActivityLockPattern != null) {
                String url = parms[0];
                mActivityLockPattern.changeViewToDrawPassword(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(String[] parms) {
        clearView();
    }

    public void openLoclPatter() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearView();
                mLockPatternDecorView = null;
                if (mLockPatternDecorView == null) {
                    Intent intent = null;

                    if (mgr == null) {
                        mgr = new LocalActivityManager((Activity) mContext,
                                false);
                        mgr.dispatchCreate(null);
                    }
                    // LocalActivityManager mgr = ((ActivityGroup)
                    // mContext).getLocalActivityManager();
                    if (longinStr.equals(ConstUtils.LOCK)) {
                        intent = new Intent(mContext, LoginActivity.class);
                        intent.putExtra(LoginActivity.CHECK_PASSWORK,
                                widgetData.m_appId);

                        if (isOpen) {
                            isOpen = false;
                            if (isLoginTrueOrFalse) {
                                isLoginTrueOrFalse = false;
                                intent.putExtra(
                                        LoginActivity.INTENT_KEY_AFRESH_LOGIN,
                                        true);
                            }
                            intent.putExtra(
                                    LoginActivity.INTENT_KEY_AFRESH_OR_OPEN,
                                    "open");
                        }
                    } else if (longinStr.equals(ConstUtils.INIT)) {
                        intent = new Intent(mContext,
                                SetPasswordActivity.class);
                        intent.putExtra(SetPasswordActivity.SETPASSWORD,
                                widgetData.m_appId);
                    }
                    Window window = mgr.startActivity(TAG, intent);
                    if (longinStr.equals(ConstUtils.LOCK)) {
                        mActivityLockPattern = (LoginActivity) window
                                .getContext();
                    }
                    mLockPatternDecorView = window.getDecorView();
                    LengthVO lengthVO = getDeviceWidth();
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                            lengthVO.width, lengthVO.width);
                    lp.leftMargin = lengthVO.leftMargin;
                    lp.topMargin = heighth;
                    addViewToCurrentWindow(mLockPatternDecorView, lp);
                    // addView2CurrentWindow(mLockPatternDecorView, lp);
                }
            }
        });
    }

    // /**
    // * @param child
    // * @param parms
    // */
    // private void addView2CurrentWindow(View child,
    // RelativeLayout.LayoutParams parms) {
    // int l = (int) (parms.leftMargin);
    // int t = (int) (parms.topMargin);
    // int w = parms.width;
    // int h = parms.height;
    // FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
    // lp.gravity = Gravity.NO_GRAVITY;
    // lp.leftMargin = l;
    // lp.topMargin = t;
    // // adptLayoutParams(parms, lp);
    // // Log.i(TAG, "addView2CurrentWindow");
    // mBrwView.addViewToCurrentWindow(child, lp);
    // }

    public void clearView() {

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mLockPatternDecorView) {
                    if (mgr == null) {
                        mgr = new LocalActivityManager((Activity) mContext,
                                false);
                        mgr.dispatchCreate(null);
                    }
                    // LocalActivityManager mgr = ((ActivityGroup)
                    // mContext).getLocalActivityManager();
                    removeViewFromCurrentWindow(mLockPatternDecorView);
                    // mActivityLockPattern = null;
                    // mLockPatternDecorView = null;
                    mgr.destroyActivity(TAG, true);
                }
            }
        });
    }

    public void getInit() {
        mContext.getSharedPreferences("com.way.locus.LocusPassWordView.temp", 0)
                .edit().clear().commit();
        mContext.getSharedPreferences(ConstUtils.SP_FILE_LOCK_VIEW_CONFIG, 0)
                .edit().clear().commit();
        mContext.getSharedPreferences("com.way.locus.preferences1", 0).edit()
                .clear().commit();
        mContext.getSharedPreferences("com.way.locus.init", 0).edit().clear()
                .commit();
        mContext.getSharedPreferences(ConstUtils.ERRORNAME, 0).edit().clear()
                .commit();
    }

    @Override
    protected boolean clean() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mActivityLockPattern) {
                    clearView();
                    mActivityLockPattern = null;
                }
            }
        });
        return true;
    }

    // 设置手势密码
    public void setLockPassWord(String[] parms) {
        Log.i("xindabao", "set=" + "set1");
        if (parms.length < 1) {
            return;
        }
        String LockPassWord = parms[0];
        ShaPrefUtils.putString(mContext, LockPassWord, ConstUtils.FILLOCKNAME,
                ConstUtils.KEYELOCKNAME);

        // 设置密码

        // aes加密
        // String enSetStr = AESDecode.encode(KEY_APP, setStr);
        //
        // ShaPrefUtils.setCheckPassword(mContext, enSetStr, this.getClass()
        // .getName(), Contains.SETORGETNAME);
        //
        // initCon();

        // jsCallback(func_on_setPW, 0, EUExCallback.F_C_TEXT, LockPassWord);
    }

    // 获取手势密码
    public void getLockPassWord(String[] parms) {
        if (parms.length != 0) {
            return;
        }

        String getPasswordData = ShaPrefUtils.getString(mContext,
                this.getClass().getName(), ConstUtils.SETORGETNAME);
        if (con != null) {
            con = null;
        }
        initCon();

        String getEnStr = AESDecode.decode(KEY_APP, getPasswordData);
        Log.i(TAG, "com.way.locus.setPasswordData:" + getEnStr + "=="
                + getPasswordData);
        Log.i(TAG,
                "com.way.locus.setPasswordData" + ShaPrefUtils.getString(
                        mContext, this.getClass().getName(),
                        ConstUtils.SETORGETNAME));
        jsCallback(func_on_GET, 0, EUExCallback.F_C_TEXT, getEnStr);
    }

    class LengthVO {
        int width;
        int leftMargin;
    }

    // 获取屏幕宽度
    private LengthVO getDeviceWidth() {
        LengthVO lengthVO = new LengthVO();
        float scale = Utils.getWebScale(mBrwView);
        int[] params = BUtility.getDeviceResolution((Activity) mContext);
        // 屏幕较小的宽度.0,width 1,height
        int width = (int) ((params[0] > params[1] ? params[1] : params[0])
                / scale);
        lengthVO.width = (int) (width * 0.9);
        lengthVO.leftMargin = (width - lengthVO.width) / 2;
        return lengthVO;
    }

    /**
     * 获取据顶部的高度，2/3(height-width)
     *
     * @return
     */
    public int getLockToTop() {
        int[] params = BUtility.getDeviceResolution((Activity) mContext);
        int topMagin = (int) (Math.abs((params[0] - params[1]) / 3.f * 2));
        return topMagin;
    }

    public void setColors(String[] parms) {
        if (parms.length < 1) {
            return;
        }
        Log.i("parms[0]", parms[0] + "");

        SetColorsVO setColorsVO = DataHelper.gson.fromJson(parms[0],
                SetColorsVO.class);
        String OUT_CYCLE_NORMAL = setColorsVO.getOUT_CYCLE_NORMAL();
        String CYCLE_ONTOUCH = setColorsVO.getCYCLE_ONTOUCH();
        String LINE_COLOR = setColorsVO.getLINE_COLOR();
        String ERROR_COLOR = setColorsVO.getERROR_COLOR();
        Log.i("OUT_CYCLE_NORMAL", OUT_CYCLE_NORMAL + "");
        Log.i("CYCLE_ONTOUCH", CYCLE_ONTOUCH + "");
        Log.i("LINE_COLOR", LINE_COLOR + "");
        Log.i("ERROR_COLOR", ERROR_COLOR + "");
        if (null != OUT_CYCLE_NORMAL) {
            LocusPassWordView
                    .setOUT_CYCLE_NORMAL(Color.parseColor(OUT_CYCLE_NORMAL));
        }
        if (null != CYCLE_ONTOUCH) {
            LocusPassWordView
                    .setINNER_CYCLE_ONTOUCH(Color.parseColor(CYCLE_ONTOUCH));
            LocusPassWordView
                    .setOUT_CYCLE_ONTOUCH(Color.parseColor(CYCLE_ONTOUCH));
        }
        if (null != LINE_COLOR) {
            LocusPassWordView.setLINE_COLOR(Color.parseColor(LINE_COLOR));
        }
        if (null != ERROR_COLOR) {
            LocusPassWordView.setERROR_COLOR(Color.parseColor(ERROR_COLOR));
        }
    }
}
