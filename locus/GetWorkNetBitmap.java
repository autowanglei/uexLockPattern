package org.zywx.wbpalmstar.plugin.uexlockpattern.locus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.zywx.wbpalmstar.plugin.uexlockpattern.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class GetWorkNetBitmap {

    public static Bitmap getBitmap(String url) {
        HttpClient httpClient = Utils.getNewHttpClient();
        @SuppressWarnings("deprecation")
        String url1 = URLEncoder.encode(url);
        String url2 = url1.replaceAll("%3A", ":").replaceAll("%2F", "/");
        HttpGet httpGet = new HttpGet(url2);
        InputStream in = null;
        try {
            Bitmap bitmap = null;
            HttpResponse resp = httpClient.execute(httpGet);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                in = resp.getEntity().getContent();
                bitmap = BitmapFactory.decodeStream(in);
            }
            return toRoundBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap getHttpBitmap(String data) {
        Bitmap bitmap = null;
        try {
            @SuppressWarnings("deprecation")
            String url1 = URLEncoder.encode(data);
            String url2 = url1.replaceAll("%3A", ":").replaceAll("%2F", "/");
            Log.i("getHttpBitmap", "url2=" + url2);
            URL url = new URL(url2);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            // connection.setConnectTimeout(5 * 1000);

            connection.setReadTimeout(5 * 60 * 1000 /* milliseconds */);
            connection.setConnectTimeout(5 * 60 * 1000 /* milliseconds */);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            connection.connect();
            InputStream in = connection.getInputStream();

            int lenght = in.available();
            BitmapFactory.Options opts = null;
            int count = 0;
            count = lenght / 10000 + 1;
            opts = new BitmapFactory.Options();
            if (lenght > 100000) {
                opts.inSampleSize = count;
            } else {
                opts.inSampleSize = 1;
            }
            bitmap = BitmapFactory.decodeStream(new PatchInputStream(in), null,
                    opts);

            in.close();
            // 关闭连接
            connection.disconnect();
            if (bitmap != null) {
                Bitmap rouBitamp = toRoundBitmap(bitmap);
                if (bitmap != null && !bitmap.isRecycled()) { // 回收bitmap
                    bitmap.recycle();// bitmap.recycle()方法用于回收该bitmap所占用的内存，接着将bitmap置空，最后，别忘了用System.gc()调用一下系统的垃圾回收器。
                    bitmap = null;
                }
                System.gc();
                return rouBitamp;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right,
                dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);

        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
        } else {// 如果仅仅是用来判断网络连接
            // 则可以使用 cm.getActiveNetworkInfo().isAvailable();
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
