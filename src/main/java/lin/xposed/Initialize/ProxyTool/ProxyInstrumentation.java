package lin.xposed.Initialize.ProxyTool;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.*;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.annotation.Nullable;
import lin.xposed.Initialize.ProxyActivityManager;
import lin.xposed.ReflectUtils.ClassUtils;

/*
* 重写全部方法
*/
@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class ProxyInstrumentation extends Instrumentation {

    private final Instrumentation rawInstrumentation;

    public ProxyInstrumentation(Instrumentation base) {
        this.rawInstrumentation = base;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return rawInstrumentation.newActivity(cl, className, intent);
        } catch (Exception e) {
            if (ProxyUtil.isModuleProxyActivity(className)) {
                //是模块的活动那么new出来返回
                return (Activity) ClassUtils.loadClass(ClassUtils.getModuleLoader(),className).newInstance();
            }
            throw e;
        }
    }

    @Override
    public void onCreate(Bundle arguments) {
        rawInstrumentation.onCreate(arguments);
    }

    @Override
    public void start() {
        rawInstrumentation.start();
    }

    @Override
    public void onStart() {
        rawInstrumentation.onStart();
    }

    @Override
    public boolean onException(Object obj, Throwable e) {
        return rawInstrumentation.onException(obj, e);
    }

    @Override
    public void sendStatus(int resultCode, Bundle results) {
        rawInstrumentation.sendStatus(resultCode, results);
    }

    @Override
    public void addResults(Bundle results) {
        rawInstrumentation.addResults(results);
    }

    @Override
    public void finish(int resultCode, Bundle results) {
        rawInstrumentation.finish(resultCode, results);
    }

    @Override
    public void setAutomaticPerformanceSnapshots() {
        rawInstrumentation.setAutomaticPerformanceSnapshots();
    }

    @Override
    public void startPerformanceSnapshot() {
        rawInstrumentation.startPerformanceSnapshot();
    }

    @Override
    public void endPerformanceSnapshot() {
        rawInstrumentation.endPerformanceSnapshot();
    }

    @Override
    public void onDestroy() {
        rawInstrumentation.onDestroy();
    }

    @Override
    public Context getContext() {
        return rawInstrumentation.getContext();
    }

    @Override
    public ComponentName getComponentName() {
        return rawInstrumentation.getComponentName();
    }

    @Override
    public Context getTargetContext() {
        return rawInstrumentation.getTargetContext();
    }


    @Override
    public String getProcessName() {
        return rawInstrumentation.getProcessName();
    }

    @Override
    public boolean isProfiling() {
        return rawInstrumentation.isProfiling();
    }

    @Override
    public void startProfiling() {
        rawInstrumentation.startProfiling();
    }

    @Override
    public void stopProfiling() {
        rawInstrumentation.stopProfiling();
    }

    @Override
    public void setInTouchMode(boolean inTouch) {
        rawInstrumentation.setInTouchMode(inTouch);
    }

    @Override
    public void waitForIdle(Runnable recipient) {
        rawInstrumentation.waitForIdle(recipient);
    }

    @Override
    public void waitForIdleSync() {
        rawInstrumentation.waitForIdleSync();
    }

    @Override
    public void runOnMainSync(Runnable runner) {
        rawInstrumentation.runOnMainSync(runner);
    }

    @Override
    public Activity startActivitySync(Intent intent) {
        return rawInstrumentation.startActivitySync(intent);
    }

    @Override
    public Activity startActivitySync(Intent intent, Bundle options) {
        return rawInstrumentation.startActivitySync(intent, options);
    }

    @Override
    public void addMonitor(ActivityMonitor monitor) {
        rawInstrumentation.addMonitor(monitor);
    }

    @Override
    public ActivityMonitor addMonitor(IntentFilter filter, ActivityResult result, boolean block) {
        return rawInstrumentation.addMonitor(filter, result, block);
    }

    @Override
    public ActivityMonitor addMonitor(String cls, ActivityResult result, boolean block) {
        return rawInstrumentation.addMonitor(cls, result, block);
    }

    @Override
    public boolean checkMonitorHit(ActivityMonitor monitor, int minHits) {
        return rawInstrumentation.checkMonitorHit(monitor, minHits);
    }

    @Override
    public Activity waitForMonitor(ActivityMonitor monitor) {
        return rawInstrumentation.waitForMonitor(monitor);
    }

    @Override
    public Activity waitForMonitorWithTimeout(ActivityMonitor monitor, long timeOut) {
        return rawInstrumentation.waitForMonitorWithTimeout(monitor, timeOut);
    }

    @Override
    public void removeMonitor(ActivityMonitor monitor) {
        rawInstrumentation.removeMonitor(monitor);
    }

    @Override
    public boolean invokeMenuActionSync(Activity targetActivity, int id, int flag) {
        return rawInstrumentation.invokeMenuActionSync(targetActivity, id, flag);
    }

    @Override
    public boolean invokeContextMenuAction(Activity targetActivity, int id, int flag) {
        return rawInstrumentation.invokeContextMenuAction(targetActivity, id, flag);
    }

    @Override
    public void sendStringSync(String text) {
        rawInstrumentation.sendStringSync(text);
    }

    @Override
    public void sendKeySync(KeyEvent event) {
        rawInstrumentation.sendKeySync(event);
    }

    @Override
    public void sendKeyDownUpSync(int key) {
        rawInstrumentation.sendKeyDownUpSync(key);
    }

    @Override
    public void sendCharacterSync(int keyCode) {
        rawInstrumentation.sendCharacterSync(keyCode);
    }

    @Override
    public void sendPointerSync(MotionEvent event) {
        rawInstrumentation.sendPointerSync(event);
    }

    @Override
    public void sendTrackballEventSync(MotionEvent event) {
        rawInstrumentation.sendTrackballEventSync(event);
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return rawInstrumentation.newApplication(cl, className, context);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        rawInstrumentation.callApplicationOnCreate(app);
    }

    /*
    * 有新活动时调用
    */
    @Override
    public Activity newActivity(Class<?> clazz, Context context, IBinder token,
                                Application application, Intent intent, ActivityInfo info, CharSequence title,
                                Activity parent, String id, Object lastNonConfigurationInstance)
            throws IllegalAccessException, InstantiationException {
        return rawInstrumentation.newActivity(clazz, context, token, application, intent, info, title, parent, id, lastNonConfigurationInstance);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        if (icicle != null) {
            String className = activity.getClass().getName();
            if (ProxyUtil.isModuleProxyActivity(className)) {
                icicle.setClassLoader(ClassUtils.getModuleLoader());
            }
        }
        ProxyActivityManager.injectModuleResources(activity);
        rawInstrumentation.callActivityOnCreate(activity, icicle);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        if (icicle != null) {
            String className = activity.getClass().getName();
            if (ProxyUtil.isModuleProxyActivity(className)) {
                icicle.setClassLoader(ClassUtils.getModuleLoader());
            }
        }
        ProxyActivityManager.injectModuleResources(activity);
        rawInstrumentation.callActivityOnCreate(activity, icicle, persistentState);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        rawInstrumentation.callActivityOnDestroy(activity);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        rawInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState);
    }


    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState, PersistableBundle persistentState) {
        rawInstrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState, persistentState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle savedInstanceState) {
        rawInstrumentation.callActivityOnPostCreate(activity, savedInstanceState);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, @Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        rawInstrumentation.callActivityOnPostCreate(activity, savedInstanceState, persistentState);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        rawInstrumentation.callActivityOnNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        rawInstrumentation.callActivityOnStart(activity);
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        rawInstrumentation.callActivityOnRestart(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        rawInstrumentation.callActivityOnResume(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        rawInstrumentation.callActivityOnStop(activity);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        rawInstrumentation.callActivityOnSaveInstanceState(activity, outState);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState, PersistableBundle outPersistentState) {
        rawInstrumentation.callActivityOnSaveInstanceState(activity, outState, outPersistentState);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        rawInstrumentation.callActivityOnPause(activity);
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        rawInstrumentation.callActivityOnUserLeaving(activity);
    }

    @Override
    public void startAllocCounting() {
        rawInstrumentation.startAllocCounting();
    }

    @Override
    public void stopAllocCounting() {
        rawInstrumentation.stopAllocCounting();
    }

    @Override
    public Bundle getAllocCounts() {
        return rawInstrumentation.getAllocCounts();
    }

    @Override
    public Bundle getBinderCounts() {
        return rawInstrumentation.getBinderCounts();
    }

    @Override
    public UiAutomation getUiAutomation() {
        return rawInstrumentation.getUiAutomation();
    }

    @Override
    public UiAutomation getUiAutomation(int flags) {
        return rawInstrumentation.getUiAutomation(flags);
    }

    @Override
    public TestLooperManager acquireLooperManager(Looper looper) {
        return rawInstrumentation.acquireLooperManager(looper);
    }

}
