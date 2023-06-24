package lin.xposed.Initialize.ProxyTool;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.*;
import lin.xposed.Initialize.ProxyActivityManager;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ProxyHandlerCallback implements Handler.Callback {

    private final Handler.Callback mNextCallbackHook;

    public ProxyHandlerCallback(Handler.Callback next) {
        mNextCallbackHook = next;
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == 100) {
            // 启动活动
            onHandleLaunchActivity(msg);
        } else if (msg.what == 159) {
            // 执行事务
            onHandleExecuteTransaction(msg);
        }
        // call next hook
        if (mNextCallbackHook != null) {
            return mNextCallbackHook.handleMessage(msg);
        }
        return false;
    }

    /*
     * 处理活动
     *
     * 目的 还原Intent
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    @SuppressWarnings("JavaReflectionMemberAccess")
    private void onHandleLaunchActivity(Message msg) {
        try {
            Object activityClientRecord = msg.obj;
            Field field_intent = activityClientRecord.getClass().getDeclaredField("intent");
            field_intent.setAccessible(true);
            //获取启动活动时的Intent
            Intent intent = (Intent) field_intent.get(activityClientRecord);
            assert intent != null;
            Bundle bundle = null;
            try {
                //获取Bundle onCreate里的(Bundle savedInstanceState)那个
                Field fExtras = Intent.class.getDeclaredField("mExtras");
                fExtras.setAccessible(true);
                bundle = (Bundle) fExtras.get(intent);
            } catch (Exception e) {
                LogUtils.addError(e);
            }
            if (bundle != null) {
                //设置回宿主原本的类加载器
                bundle.setClassLoader(ClassUtils.getHostLoader());
                //有自己的Flag 说明是自己封装的那一份
                if (intent.hasExtra(ProxyActivityManager.FLAG)) {
                    //还原回原本的Intent
                    Intent realIntent = intent.getParcelableExtra(ProxyActivityManager.FLAG);
                    field_intent.set(activityClientRecord, realIntent);
                }
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
    }

    /*
     * 监听事务列表
     * 如果有活动事务那么交给 processLaunchActivityItem方法处理 来判断是否还原Intent
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    private void onHandleExecuteTransaction(Message msg) {
        Object clientTransaction = msg.obj;
        try {
            if (clientTransaction != null) {
                Method getCallbacks =
                        Class.forName("android.app.servertransaction.ClientTransaction")
                                .getDeclaredMethod("getCallbacks");
                getCallbacks.setAccessible(true);
                //事务列表
                List<?> clientTransactionItems =
                        (List<?>) getCallbacks.invoke(clientTransaction);
                //事务列表不为空
                if (clientTransactionItems != null && !clientTransactionItems.isEmpty()) {
                    for (Object item : clientTransactionItems) {
                        Class<?> c = item.getClass();
                        //启动活动的事务
                        if (c.getName().contains("LaunchActivityItem")) {
                            processLaunchActivityItem(item, clientTransaction);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
    }

    /*
     * 进程活动处理
     *
     * 目的 还原Intent
     */
    @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
    @SuppressWarnings("JavaReflectionMemberAccess")
    private void processLaunchActivityItem(Object item, Object clientTransaction)
            throws ReflectiveOperationException {
        Class<?> c = item.getClass();
        Field fmIntent = c.getDeclaredField("mIntent");
        fmIntent.setAccessible(true);
        //获取封装的Intent
        Intent wrapper = (Intent) fmIntent.get(item);
        assert wrapper != null;
        Bundle bundle = null;
        try {
            //获取bundle
            Field fExtras = Intent.class.getDeclaredField("mExtras");
            fExtras.setAccessible(true);
            bundle = (Bundle) fExtras.get(wrapper);
        } catch (Exception e) {
            LogUtils.addError(e);
        }
        if (bundle != null) {
            //设置回宿主的类加载器
            bundle.setClassLoader(ClassUtils.getHostLoader());
            //有自己的Flag 说明是自己封装的那一份
            if (wrapper.hasExtra(ProxyActivityManager.FLAG)) {

                //获取原本的Intent
                Intent realIntent = wrapper.getParcelableExtra(ProxyActivityManager.FLAG);
                //替换回原本的Intent
                fmIntent.set(item, realIntent);

                //Android 12
                if (Build.VERSION.SDK_INT >= 31) {
                    IBinder token = (IBinder) clientTransaction.getClass()
                            .getMethod("getActivityToken").invoke(clientTransaction);
                    Class<?> clazz_ActivityThread =
                            Class.forName("android.app.ActivityThread");
                    Method currentActivityThread =
                            clazz_ActivityThread.getDeclaredMethod("currentActivityThread");
                    currentActivityThread.setAccessible(true);
                    Object activityThread = currentActivityThread.invoke(null);
                    assert activityThread != null;
                    // Accessing hidden method Landroid/app/ClientTransactionHandler;->getLaunchingActivity(Landroid/os/IBinder;)Landroid/app/ActivityThread$ActivityClientRecord; (blocked, reflection, denied)
                    // Accessing hidden method Landroid/app/ActivityThread;->getLaunchingActivity(Landroid/os/IBinder;)Landroid/app/ActivityThread$ActivityClientRecord; (blocked, reflection, denied)
                    try {
                        Object acr = activityThread.getClass()
                                .getMethod("getLaunchingActivity", IBinder.class)
                                .invoke(activityThread, token);
                        if (acr != null) {
                            Field fAcrIntent = acr.getClass().getDeclaredField("intent");
                            fAcrIntent.setAccessible(true);
                            fAcrIntent.set(acr, realIntent);
                        }
                    } catch (NoSuchMethodException e) {

                    }
                }
            }
        }
    }
}
