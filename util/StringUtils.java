package org.zywx.wbpalmstar.plugin.uexlockpattern.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author wanglei
 * @explain 该类提供对json字符串的操作，包括添加、获取、删除key
 *
 */
public class StringUtils {

    /**
     * 在登login参数中添加 isFirstLogin字段
     * 
     * @param jsonStr
     * @return
     * 
     * @ps isFirstLogin 为login的必须字段，若网页不传改参数，则添加，默认为false。
     */
    public static String putString(String jsonStr, String key, String val) {
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            if (!json.has(key)) {
                json.put(key, val);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    /**
     * @param jsonStr
     * @param key
     * @return
     */
    public static String getString(String jsonStr, String key) {
        String value = "";
        try {
            JSONObject json = new JSONObject(jsonStr);
            value = json.optString(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String remove(String jsonStr, String key) {
        JSONObject Json = null;
        try {
            Json = new JSONObject(jsonStr);
            if (Json.has(key)) {
                Json.remove(key);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Json.toString();
    }

    /**
     * 判断是否是json结构
     */
    public static boolean isJson(String value) {
        try {
            new JSONObject(value);
        } catch (JSONException e) {
            return false;
        }
        return true;
    }
}