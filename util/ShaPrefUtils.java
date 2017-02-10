package org.zywx.wbpalmstar.plugin.uexlockpattern.util;

import android.content.Context;
import android.content.SharedPreferences;

public class ShaPrefUtils {

    public static String getString(Context mContext, String fileName,
            String keyName, String defValue) {
        SharedPreferences preferences = mContext.getSharedPreferences(fileName,
                0);
        return preferences.getString(keyName, defValue);
    }

    public static String getString(Context mContext, String fileName,
            String keyName) {
        SharedPreferences preferences = mContext.getSharedPreferences(fileName,
                0);
        return preferences.getString(keyName, null);
    }

    public static void putString(Context mContext, String value,
            String fileName, String keyName) {
        SharedPreferences.Editor token = mContext
                .getSharedPreferences(fileName, 0).edit();
        token.putString(keyName, value);
        token.commit();
    }

}
