package org.zywx.wbpalmstar.plugin.uexlockpattern.locus;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.EUExLockPattern;
import org.zywx.wbpalmstar.plugin.uexlockpattern.locus.LocusPassWordView.OnCompleteListener;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ConstUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.PassWordErrorCallBack;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ShaPrefUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.Utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SetPasswordActivity extends Activity {
    private String password;
    @SuppressWarnings("unused")
    private boolean needverify = true;
    private Toast toast;
    private String app_Id;
    public static final String SETPASSWORD = "password_MD5";
    private Context mContext;

    private PassWordErrorCallBack callError = new PassWordErrorCallBack() {

        @Override
        public void passWordError(int count) {
        }

        @Override
        public void passWordTooShort(String errMsg) {
            EUExLockPattern.con.getToast(errMsg);
        }

    };

    @SuppressWarnings("unused")
    private void showToast(CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mContext = this;
            app_Id = getIntent().getStringExtra(SETPASSWORD);
            Utils.setCheckPasswork(this, app_Id);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(EUExUtil.getResLayoutID(
                    "plugin_uexlockpattern_setpassword_activity"));
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 第一次输入，手势密码。
    public void initView() {
        // 设置手势密码
        try {
            final LocusPassWordView lpwv;
            final LinearLayout layout = (LinearLayout) this
                    .findViewById(EUExUtil.getResIdID("mLocusPassWordView"));
            lpwv = new LocusPassWordView(SetPasswordActivity.this);
            if (lpwv != null) {
                lpwv.height = 90;
            }
            final LinearLayout.LayoutParams layoutParm = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            layout.addView(lpwv, layoutParm);
            lpwv.setCallError(callError);
            lpwv.setOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(String mPassword) {
                    password = mPassword;
                    Log.i("mPassword", "mPassword=" + mPassword);
                    if (!TextUtils.isEmpty(password)) {
                        // setString(mPassword);
                        layout.removeAllViews();
                        try {
                            lpwv.setTempPassWord(
                                    MD5.getMD5Code(mPassword + app_Id));
                            Thread.sleep(500);
                            harvestView();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            if (lpwv.isPasswordEmpty()) {
                this.needverify = false;
                EUExLockPattern.con.getToast("绘制解锁密码");
            } else {
                EUExLockPattern.con.getToast("绘制解锁密码");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 第二次输入密码。
    public void harvestView() {
        EUExLockPattern.con.getToast("再次绘制解锁密码");
        final LocusPassWordView lpwv2 = new LocusPassWordView(
                SetPasswordActivity.this);
        final LinearLayout layout2 = (LinearLayout) this
                .findViewById(EUExUtil.getResIdID("mLocusPassWordView"));
        LinearLayout.LayoutParams layoutParm2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layout2.addView(lpwv2, layoutParm2);
        if (lpwv2 != null) {
            lpwv2.height = 90;
        }
        lpwv2.setCallError(callError);
        lpwv2.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(String mPassword) {
                password = mPassword;
                Log.i("mPassword", "mPassword=" + mPassword);
                if (!TextUtils.isEmpty(password)) {
                    String md5_passwork = MD5.getMD5Code(mPassword + app_Id);
                    boolean isVery = lpwv2.verifyPassword(md5_passwork);
                    Log.i("mPassword", "very----------=" + isVery);
                    if (isVery) {
                        Utils.setString(mContext, md5_passwork);

                        String count1 = ShaPrefUtils.getString(
                                SetPasswordActivity.this,
                                ConstUtils.FILECONTNAME,
                                ConstUtils.KEYECONTNAME);
                        int count = Integer.parseInt(count1);
                        if (count > 1) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("status", "1");
                                json.put("password", md5_passwork);
                            } catch (JSONException e) {
                            }
                            EUExLockPattern.con.getResult(json.toString());
                        } else {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("status", "2");
                                json.put("password", md5_passwork);
                            } catch (JSONException e) {
                            }
                            EUExLockPattern.con.getResult(json.toString());
                        }
                    } else {
                        lpwv2.setError(true);
                        lpwv2.markError();
                        EUExLockPattern.con.getToast("与上一次绘制不一致，请重新绘制");
                    }
                }
            }
        });
    }
}
