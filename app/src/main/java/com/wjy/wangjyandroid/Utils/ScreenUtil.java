package com.wjy.wangjyandroid.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by wjy on 2018/2/6.
 */

public class ScreenUtil {

    public static Rect getAppRect(Activity activity) {
        Rect appRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(appRect);
        return appRect;
    }

    public static DisplayMetrics getScreenMetrics(Context context) {
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics metrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(metrics);
//        return metrics;
        return context.getResources().getDisplayMetrics();
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    public static int dp2px(float dpVal, Context ctx) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, ctx.getResources().getDisplayMetrics());
    }

    public static float px2dp(float pxVal, Context ctx) {
        return (pxVal / ctx.getResources().getDisplayMetrics().density);
    }

}
