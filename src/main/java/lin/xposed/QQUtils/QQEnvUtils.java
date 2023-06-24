package lin.xposed.QQUtils;

import android.util.Log;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;

import java.util.Objects;

public class QQEnvUtils {

    public static int getTargetID(String IDName) {

        try {
            return FieIdUtils.getField(null,
                    Objects.requireNonNull(
                            ClassUtils.getClass("com.tencent.mobileqq.R$id")),
                    IDName,
                    int.class);
        } catch (Exception e) {
            return 0;
        }

    }

    public static String getCurrentUin() {
        try {
            Object runTime = getAppRuntime();
            if (runTime == null) return null;
            return MethodUtils.callMethod(runTime, "getCurrentAccountUin", String.class, new Class[]{});
        } catch (Exception e) {
            LogUtils.addRunLog("getMUIN Error" + Log.getStackTraceString(e));
            return null;
        }
    }

    public static Object getAppRuntime() throws Exception {
        Object sApplication = MethodUtils.callStaticMethod(ClassUtils.getClass("com.tencent.common.app.BaseApplicationImpl"),
                "getApplication", ClassUtils.getClass("com.tencent.common.app.BaseApplicationImpl"), new Class[]{});
        return MethodUtils.callMethod(sApplication, "getRuntime", ClassUtils.getClass("mqq.app.AppRuntime"), new Class[]{});
    }
}
