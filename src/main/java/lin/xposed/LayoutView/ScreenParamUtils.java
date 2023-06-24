package lin.xposed.LayoutView;

import android.content.Context;
import lin.xposed.HostEnv;

public class ScreenParamUtils {

    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    public static int dpToPx(Context context, float dpValue) {
        // 获取屏幕密度
        final float scale = context.getResources().getDisplayMetrics().density;
        // 结果+0.5是为了int取整时更接近
        return (int) (dpValue * scale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        if (dpValue > 0) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } else {
            float f = -dpValue;
            final float scale = context.getResources().getDisplayMetrics().density;
            return -(int) (f * scale + 0.5f);
        }

    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    public static int pxToDp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp转px
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转sp
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public static int getScreenWidth() {
        return HostEnv.context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return HostEnv.context.getResources().getDisplayMetrics().heightPixels;
    }
}
