package org.zywx.wbpalmstar.plugin.uexlockpattern.locus;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexlockpattern.EUExLockPattern;
import org.zywx.wbpalmstar.plugin.uexlockpattern.locus.LocusPassWordView.OnCompleteListener;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ConstUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.PassWordErrorCallBack;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.ShaPrefUtils;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.UtilModel;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.Utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

    private LocusPassWordView lpwv;
    private Toast toast;
    private String result = "";
    public static final String INTENT_KEY_AFRESH_OR_OPEN = "intentKeyOpen";
    public static final String INTENT_KEY_TEXTVIEW = "titleTab";
    public static final String INTENT_KEY_AFRESH_LOGIN = "loginIsTrueOrFalse";

    /**
     * data:image/gif;base64,base64编码的gif图片数据
     * data:image/png;base64,base64编码的png图片数据
     * data:image/jpg;base64,base64编码的jpeg图片数据
     * data:image/x-icon;base64,base64编码的icon图片数据
     */
    public static final String IMAGE_GIF = "data:image/gif;base64,";
    public static final String IMAGE_PNG = "data:image/png;base64,";
    public static final String IMAGE_JPEG = "data:image/jpg;base64,";
    public static final String IMAGE_X_ICON = "data:image/x-icon;base64,";

    private boolean isSuccess = true;
    private final static String TAG = "LoginActivity";
    private final static int forgetId = 1234567890, otherId = 1234567891;
    private UtilModel utilModel;
    private String app_Id;
    public static final String CHECK_PASSWORK = "check_passwork";
    private int loginCount = 0;

    @SuppressWarnings("unused")
    private void showToast(CharSequence message) {
        if (null == toast) {
            toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
    private PassWordErrorCallBack callError = new PassWordErrorCallBack() {

        @Override
        public void passWordError(int count) {
            EUExLockPattern.con.getError(count + "");
            EUExLockPattern.con.getErrorCount((loginCount - count) + "");
        }

        @Override
        public void passWordTooShort(String errMsg) {
            EUExLockPattern.con.getToast(errMsg);
        }
    };

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(EUExUtil
                .getResLayoutID("plugin_uexlockpattern_login_activity"));
        app_Id = getIntent().getStringExtra(CHECK_PASSWORK);
        String check = Utils.getCheckPasswork(this);
        if (check == null) {
            String cipher = ShaPrefUtils.getString(LoginActivity.this,
                    ConstUtils.FILLOCKNAME, ConstUtils.KEYELOCKNAME);
            String md5Str = MD5.getMD5Code(cipher + app_Id);
            Utils.setString(this, md5Str);
            Utils.setCheckPasswork(this, app_Id);
        }

        loginCount = Integer.parseInt(ShaPrefUtils.getString(LoginActivity.this,
                ConstUtils.ERRORNAME, ConstUtils.ERRORNAME));
        // 获取到开标志位。
        lpwv = (LocusPassWordView) this
                .findViewById(EUExUtil.getResIdID("mLocusPassWordView"));

        utilModel = new UtilModel();
        if (lpwv != null) {
            lpwv.setUtilModel(utilModel);
            lpwv.setCallError(callError);
        }
        lpwv.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(String mPassword) {
                String cipher = ShaPrefUtils.getString(LoginActivity.this,
                        ConstUtils.FILLOCKNAME, ConstUtils.KEYELOCKNAME);
                String md5_passwork = MD5.getMD5Code(mPassword + app_Id);
                if (cipher.equals(md5_passwork)) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("status", "0");
                        json.put("password", md5_passwork);
                    } catch (JSONException e) {
                    }
                    result = json.toString();
                    isSuccess = true;
                } else {
                    lpwv.setError(true);
                    Log.i("lpwv", "lpwv.setError(true)=" + lpwv.isError());
                    int k = utilModel.getCountError();
                    k++;
                    utilModel.setCountError(k);
                    isSuccess = false;
                    lpwv.markError();
                    callError.passWordError(utilModel.getCountError());
                }
                if (isSuccess) {
                    EUExLockPattern.con.getResult(result);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void changeViewToDrawPassword(String logString) {
        if (logString != null && logString.startsWith("http://")) {
            if (GetWorkNetBitmap.isNetworkAvailable(this)) {
                visitNetWork(logString);
            }
        }
    }

    public String getBase64(String strBase) {
        if (strBase.startsWith(IMAGE_GIF)) {
            return strBase.replace(IMAGE_GIF, "");
        } else if (strBase.startsWith(IMAGE_PNG)) {
            return strBase.replace(IMAGE_PNG, "");
        } else if (strBase.startsWith(IMAGE_JPEG)) {
            return strBase.replace(IMAGE_JPEG, "");
        } else if (strBase.startsWith(IMAGE_X_ICON)) {
            return strBase.replace(IMAGE_X_ICON, "");
        }
        return null;
    }

    // 用于访问网络获取数据。
    public void visitNetWork(String url) {
        Log.i(TAG, "url=" + url);
        MyAsyncTask task = null;
        try {
            Log.i(TAG, "TextUtils=" + TextUtils.isEmpty(url));
            // 判断url路径
            task = new MyAsyncTask();
            task.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyAsyncTask extends AsyncTask<String, String, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            Log.i(TAG, "params[0]=" + params[0]);
            try {
                return GetWorkNetBitmap.getHttpBitmap(params[0]);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }
    }

    // data:image/jpg;base64,xxxxxxxxxx
    @SuppressLint("NewApi")
    public Bitmap stringtoBitmap(String string) {
        // 将字符串转换成Bitmap类型
        Bitmap bitmap;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
                    bitmapArray.length);
            return GetWorkNetBitmap.toRoundBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == forgetId) {
            Log.i("forgetId", "forgetId===forgetId");
            EUExLockPattern.con.getForget();
        } else if (v.getId() == otherId) {
            EUExLockPattern.con.getother();
        }
    }
}
