package lin.xposed.Initialize;

import HookItem.loadHook.HookItemMainInfo;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.github.kyuubiran.ezxhelper.EzXHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HookUtils.XPBridge;
import lin.xposed.HostEnv;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.Utils.LogUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class InitItemHook {
    public static AtomicBoolean isInit = new AtomicBoolean();

    public static void startHookQQ() {
        /*
         * hook同一个对象不同的方法
         * 避免发生 NullPointerException: Null reference used for synchronization (monitor-enter) 锁同步对象为空
         * 全局的Context可能未初始化导致调用方法锁对象为空 ConfigTool.GlobalConfig.getString() 抛出上面那个错误
         * */
        XposedHelpers.findAndHookMethod(ClassUtils.getClass("com.tencent.mobileqq.qfix.QFixApplication"), "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isInit.getAndSet(true))//防止多次挂钩命中被多次注入
                    return;
                try {
                    HostEnv.application = (Application) param.thisObject;//正在运行的应用对象
                    HostEnv.context = HostEnv.application.getApplicationContext();//qq全局的context 不能用来构造视图奥
                    ClassUtils.setHostClassLoader(
                            param.thisObject.getClass().getClassLoader()
                    );//使用qq自带的类加载器
                    hook();
                } catch (Exception e) {
                    XposedBridge.log(e);
                    LogUtils.addRunLog("[Main Error]" + Log.getStackTraceString(e));
                }
            }
        });
        XposedHelpers.findAndHookMethod("com.tencent.mobileqq.qfix.QFixApplication", ClassUtils.getHostLoader(), "attachBaseContext", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (isInit.getAndSet(true))//防止多次挂钩命中被多次注入
                    return;
                try {
                    HostEnv.application = (Application) param.thisObject;
                    HostEnv.context = (Context) param.args[0];//qq全局的context 不能用来构造视图奥
                    ClassUtils.setHostClassLoader(
                            param.thisObject.getClass().getClassLoader()
                    );//使用qq自带的类加载器

                    hook();
                } catch (Exception e) {
                    XposedBridge.log(e);
                    LogUtils.addRunLog("[Main 2 Error]" + Log.getStackTraceString(e));
                }
            }
        });
    }

    private static void hook() {
        try {
            if (HostEnv.application == null) {
                XposedBridge.log("[QStory] application null");
            }
            if (HostEnv.context == null) {
                XposedBridge.log("[QStory] context null");
            }
            if (ClassUtils.getHostLoader() == null) {
                XposedBridge.log("[QStory] ClassLoader null");
            }
            PathInit.isFileComplete();//初始化路径信息
            //初始化全局Context
            EzXHelper.initAppContext(HostEnv.context, false);
            CommonTool.InjectResourcesToContext(HostEnv.context);

            HostEnv.init(HostEnv.context);//得到版本信息

            //延时hook
            hookStart();

            ProxyActivityManager.initForStubActivity();
            //注入设置
//            if (HostEnv.QQVersion >= QQVersion.QQ_8_9_68) InjectSettingsLayout.inject();
            InjectSettingsLayout.startHookSetUpLayout();

            //加载hook
            if (PathInit.IsInitPath.get() && PathInit.verifyStoragePermissions(HostEnv.context)) {
                HookItemMainInfo.onBefore();

            }
        } catch (Exception e) {
            LogUtils.addRunLog(e);
        }
    }


    private static void hookStart() {
        //一些环境初始化后才会执行的代码 并且不会重复执行 只执行一次
        try {
            XPBridge.HookBeforeOnce(XposedHelpers.findMethodBestMatch(ClassUtils.getClass("com.tencent.mobileqq.startup.step.LoadData"), "doStep"),
                    param -> {
                        try {
                            if (!PathInit.verifyStoragePermissions(HostEnv.context)) {
                                PathInit.dialog();
                                return;
                            }
                            HookItemMainInfo.onAfter();
                        } catch (Exception e) {
                            XposedBridge.log(e);
                        }
                        HttpInit.init();
                    }
            );
        } catch (Exception e) {

        }
    }
}
