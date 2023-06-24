package lin.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import lin.xposed.HookUtils.CommonTool;

public class HostEnv {
    public static final boolean isDebug = BuildConfig.DEBUG;
    public static String packageName = "com.tencent.mobileqq";
    public static String TimPackageName = "com.tencent.tim";
    public static int QQVersion;
    public static String QQVersionName;
    public static String DataPath;
    public static String apkPath;
    public static String externalPath;

    public static Object AppInterface;//应用界面
    public static Object SessionInfo;//当前会话

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static Application application;

    public static String moduleName;
    public static String modulePackageName;
    public static String moduleVersionName;
    public static String modulePath;
    public static int moduleVersion;


    public static void init(Context context) {
        if (context == null) {
            return;
        }
        CommonTool.InjectResourcesToContext(context);
        PackageManager packageManager = context.getPackageManager();
        try {
            //QQ版本
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            QQVersion = (int) packageInfo.getLongVersionCode();
            QQVersionName = packageInfo.versionName;

            //模块信息
            moduleVersion = BuildConfig.VERSION_CODE;
            moduleVersionName = BuildConfig.VERSION_NAME;

        } catch (PackageManager.NameNotFoundException e) {
                CommonTool.Toasts("获取QQ或模块信息错误 请尝试检查读取应用列表权限 \n" + Log.getStackTraceString(e));
        }
        //模块名和模块包名
        moduleName = context.getString(R.string.module_name);
        modulePackageName = BuildConfig.APPLICATION_ID;

    }
}
