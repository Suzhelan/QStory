package lin.xposed.HookUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.kyuubiran.ezxhelper.EzXHelper;
import lin.xposed.HostEnv;
import lin.xposed.R;
import lin.xposed.ReflectUtils.PostMain;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lin.xposed.HostEnv.context;

public class CommonTool {
    private static final String TAG = "";

    /**
     * 获取该activity所有view
     */

    public static List<View> getAllChildViews(Activity activity) {
        View view = activity.getWindow().getDecorView();
        return getAllChildViews(view);

    }

    private static List<View> getAllChildViews(View view) {
        List<View> allChildren = new ArrayList<>();
        if (view instanceof ViewGroup vp) {
            for (int i = 0; i < vp.getChildCount(); i++) {
                View views = vp.getChildAt(i);
                allChildren.add(views);
                //递归调用
                allChildren.addAll(getAllChildViews(views));
            }
        }
        return allChildren;

    }

    //复制文字到剪切板
    public static boolean CopyToClipboard(Context context, String text) {
        try {
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", text);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static int getColors(Context activity, int id) {
        return activity.getResources().getColor(id);
    }


    public static void Toast(Object obj) {
        String str = String.valueOf(obj);
        if (context != null)
            PostMain.postMain(() -> Toast.makeText(context, str, Toast.LENGTH_LONG).show());
    }

    public static void Toasts(Object str) {
        try {
            Context activity = getActivity();
            if (activity == null) {
                PostMain.postMain(() -> Toast.makeText(context, str.toString(), Toast.LENGTH_LONG).show());
                return;
            }
            PostMain.postMain(() -> Toast.makeText(activity, str.toString(), Toast.LENGTH_LONG).show());
        } catch (Exception e) {
            LogUtils.addRunLog(TAG, e);
        }
    }

    public static void InjectResourcesToContext(Context ctx) {
        try {
            if (ctx == null) return;
            try {
                ctx.getResources().getString(R.string.module_name);//如果该活动已被注入资源是可以获取到模块本身的资源的
            } catch (Exception e) {
                EzXHelper.addModuleAssetPath(ctx);//获取异常后把模块本身的res注入到hook应用的上下文中
            }
        } catch (Exception ignored) {

        }
    }

    public static void killAppProcess() {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) HostEnv.context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    public static void killAppProcess(Context context) {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList) {
            if (runningAppProcessInfo.pid != android.os.Process.myPid()) {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }


    public static void exitAPP() {
        ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
    }


    /**
     * 获取当前正在运行的Activity
     */
    @SuppressLint("PrivateApi")
    public static Activity getActivity() {
        Class<?> activityThreadClass;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取当前活动线程
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").
                    invoke(null);
            @SuppressLint("DiscouragedPrivateApi")
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            //获取线程Map
            Map<?, ?> activities = (Map<?, ?>) activitiesField.get(activityThread);
            if (activities == null) return null;
            for (Object activityRecord : activities.values()) {
                Class<?> activityRecordClass = activityRecord.getClass();
                //获取暂停状态
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                //不是暂停状态的话那就是当前正在运行的Activity
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    InjectResourcesToContext(activity);
                    return activity;
                }
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
        return null;
    }


}
