package lin.xposed.Initialize;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Handler;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.Initialize.ProxyTool.IActivityManagerHandler;
import lin.xposed.Initialize.ProxyTool.ProxyHandlerCallback;
import lin.xposed.Initialize.ProxyTool.ProxyInstrumentation;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将模块自己的Activity注入到宿主
 * 围绕 获取到当前活动线程 替换当前活动线程中的启动属性
 */
public class ProxyActivityManager {

    //标记
    public static final String FLAG = "ProxyActivity_Flag";
    //要替换的活动类
    public static final String PROXY_CLASS_NAME = "com.tencent.mobileqq.activity.photo.CameraPreviewActivity";
    private static Class<?> clazz_ActivityThread;
    private static Object currentActivityThread;

    public static AtomicBoolean isInit = new AtomicBoolean();
    public static void injectModuleResources(Context context) {
        //自己写的注入资源到上下文
        CommonTool.InjectResourcesToContext(context);
    }


    @SuppressLint("PrivateApi")
    public static void initForStubActivity() {
        if (isInit.getAndSet(true)) return;
        try {
            //反射活动线程
            clazz_ActivityThread = Class.forName("android.app.ActivityThread");
            //当前的活动线程方法
            Method currentActivityThreadMethod =
                    clazz_ActivityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            //获取当前活动线程对象
            currentActivityThread = currentActivityThreadMethod.invoke(null);

            initInstrumentation();
            initHandler();
            initIActivityManager();
            initActivityTaskManager();
        } catch (Exception e) {
            LogUtils.addError(e);
        }
    }

    /*
     * Instrumentation负责调用Activity和Application生命周期
     */
    private static void initInstrumentation() throws Exception {
        //获取当前Instrumentation
        Field mInstrumentation = clazz_ActivityThread.getDeclaredField("mInstrumentation");
        mInstrumentation.setAccessible(true);
        //获取原Instrumentation
        Instrumentation instrumentation = (Instrumentation) mInstrumentation.get(currentActivityThread);
        //替换代理的到原本的mInstrumentation
        mInstrumentation.set(currentActivityThread, new ProxyInstrumentation(instrumentation));
    }

    /*
     * 初始化替换处理回调
     */
    @SuppressLint({"DiscouragedPrivateApi","PrivateApi"})
    private static void initHandler() throws Exception {
        Handler mH = FieIdUtils.getField(
                currentActivityThread,clazz_ActivityThread, "mH",
                Class.forName("android.app.ActivityThread$H"));
        Handler.Callback handler_callback = FieIdUtils.getField(
                mH,
                "mCallback",
                Handler.Callback.class);
        if (handler_callback == null
                || !handler_callback.getClass().getName().equals(ProxyHandlerCallback.class.getName())) {
            //换成自己的处理回调
            FieIdUtils.setField(mH, "mCallback", Handler.Callback.class, new ProxyHandlerCallback(handler_callback));
        }
    }

    @SuppressLint("PrivateApi")
    private static void initIActivityManager() throws Exception {
        Object gDefault;
        try {
            gDefault = FieIdUtils.getStaticFieId(
                    Class.forName("android.app.ActivityManagerNative"), "gDefault");
        } catch (Exception err1) {
            gDefault = FieIdUtils.getStaticFieId(ActivityManager.class,"IActivityManagerSingleton");
        }
        Object mInstance = FieIdUtils.getField(
                gDefault,
                Class.forName("android.util.Singleton"),
                "mInstance",
                Class.forName("android.app.IActivityManager$Stub$Proxy")
        );
        //代理活动管理器
        Object amProxy = Proxy.newProxyInstance(
                ClassUtils.getModuleLoader(),
                new Class[]{Class.forName("android.app.IActivityManager")},
                new IActivityManagerHandler(mInstance));
        FieIdUtils.setField(
                /*RunTime Object*/gDefault,
                Class.forName("android.util.Singleton"), "mInstance",
                Class.forName("android.app.IActivityManager$Stub$Proxy"), amProxy
        );
    }

    /*
     * 初始化替换ActivityTaskManager (活动任务管理器)
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    private static void initActivityTaskManager()  {
        try {
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Class<?> activityTaskManagerClass = Class.forName("android.app.ActivityTaskManager");
            Field fIActivityTaskManagerSingleton = activityTaskManagerClass.getDeclaredField("IActivityTaskManagerSingleton");
            fIActivityTaskManagerSingleton.setAccessible(true);
            Object singleton = fIActivityTaskManagerSingleton.get(null);
            singletonClass.getMethod("get").invoke(singleton);
            Object mDefaultTaskMgr = mInstanceField.get(singleton);
            Object proxy2 = Proxy.newProxyInstance(
                    ClassUtils.getModuleLoader(),
                    new Class[]{Class.forName("android.app.IActivityTaskManager")},
                    new IActivityManagerHandler(mDefaultTaskMgr));
            mInstanceField.set(singleton, proxy2);
        } catch (Exception e) {
        }
    }

}
