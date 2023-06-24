package lin.xposed.Initialize;

import HookItem.loadHook.HookItemMainInfo;
import HookItem.loadHook.MethodScannerWorks;
import HookItem.note.QQVersion;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HookUtils.XPBridge;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.BaseDialog;
import lin.xposed.ReflectUtils.*;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InjectSettingsLayout {

    public static void startHookSetUpLayout() {

        Class<?> clazz = ClassUtils.getClass("com.tencent.mobileqq.activity.QQSettingSettingActivity");
        Method m1 = MethodUtils.findMethod(clazz, "doOnCreate", boolean.class, new Class[]{Bundle.class});
        Method m2 = MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.fragment.QQSettingSettingFragment"), "doOnCreateView", void.class, new Class[]{LayoutInflater.class, ViewGroup.class, Bundle.class});
        XPBridge.Action action = param -> PostMain.postMain(() -> {
            try {
                Activity activity;
                if (param.thisObject instanceof Activity) {
                    activity = (Activity) param.thisObject;
                } else {
                    Method getAct = MethodUtils.findUnknownReturnMethod(param.thisObject.getClass().getName(), "getActivity", new Class[0]);
                    activity = (Activity) getAct.invoke(param.thisObject);

                }

                CommonTool.InjectResourcesToContext(activity);
                ViewGroup viewGroup = null;
                final Activity ACTIVITY = activity;

                Class<?> clz = ClassUtils.getClass("com.tencent.mobileqq.widget.FormSimpleItem");
                Field[] fields = param.thisObject.getClass().getDeclaredFields();

                for (Field field : fields) {
                    if (clz.equals(field.getType())) {
                        try {
                            field.setAccessible(true);
                            View itemView = (View) field.get(param.thisObject);//获得其中一个item
                            viewGroup = (ViewGroup) itemView.getParent();//通过item获取到父容器
                            if (viewGroup instanceof LinearLayout) {
                                break;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (viewGroup == null) {
                    LogUtils.addRunLog("没有获取到设置页布局容器");
                    return;
                }
                setItemInfo(activity, viewGroup, ACTIVITY, clz);
            } catch (Exception e) {
                LogUtils.addError("设置入口项没有成功注入", e);
            }
        }, 100);

        XPBridge.hookAfter(m1, action);
        if (m2 != null) {
            XPBridge.hookAfter(m2, action);
        }

    }

    public static void inject() {
        Class<?> clazz = ClassUtils.getClass("com.tencent.mobileqq.activity.QQSettingSettingActivity");
        Method m1 = MethodUtils.findMethod(clazz, "doOnCreate", boolean.class, new Class[]{Bundle.class});
        Method m2 = MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.fragment.QQSettingSettingFragment"), "doOnCreateView", void.class, new Class[]{LayoutInflater.class, ViewGroup.class, Bundle.class});
        XPBridge.Action action = param -> PostMain.postMain(() -> {
            try {
                Activity activity;
                if (param.thisObject instanceof Activity) {
                    activity = (Activity) param.thisObject;
                } else {
                    Method getAct = MethodUtils.findUnknownReturnMethod(param.thisObject.getClass().getName(), "getActivity", new Class[0]);
                    activity = (Activity) getAct.invoke(param.thisObject);

                }
                CommonTool.InjectResourcesToContext(activity);
                ViewGroup rootView;
                final Activity ACTIVITY = activity;

                View qqItem = activity.findViewById(activity.getResources().getIdentifier("qqsetting2_msg_notify", "id", HostEnv.packageName));
                rootView = (LinearLayout) qqItem.getParent().getParent();
                Class<?> clz = qqItem.getClass();
                setItemInfo(activity, rootView, ACTIVITY, clz);
            } catch (Exception e) {
                LogUtils.addError("设置入口项没有成功注入", e);
            }
        }, 100);

        XPBridge.hookAfter(m1, action);
        if (m2 != null) {
            XPBridge.hookAfter(m2, action);
        }
    }

    public static void setItemInfo(Activity activity, ViewGroup viewGroup, Activity ACTIVITY, Class<?> clz) throws Exception {
        View view = ConstructorUtils.newInstance(clz, activity);//new一个片段布局出来
        MethodUtils.callMethod(view, "setLeftText", void.class, new Class[]{CharSequence.class}, HostEnv.moduleName);
        MethodUtils.callMethod(view, "setRightText", void.class, new Class[]{String.class}, HostEnv.moduleVersionName);
        view.setOnClickListener(v -> {
            try {
                if (QQVersion.isQQNT() && !MethodScannerWorks.IsAvailable()) {
                    new Thread(HookItemMainInfo::onAfter).start();
//                    return;
                }
                if (!MethodScannerWorks.IsAvailable()) return;

                if (PathInit.StoragePermissions.get()) {
                    //跳转或弹出模块功能
                    BaseDialog.create(ACTIVITY);
                } else {
                    PathInit.dialog();
                }
            } catch (Exception e) {
                LogUtils.addRunLog(e);
            }
        });
        viewGroup.addView(view, 0);//view的索引位置 不填就是最后
        TextView textView = FieIdUtils.getFirstField(view, TextView.class);
        setTextViewAttribute(textView);
    }

    private static void setTextViewAttribute(TextView textView) {
        if (textView != null) {
            PostMain.postMain(() -> {
               /* String htmlFor = HostEnv.moduleName + "             <img src=" + R.mipmap.icon + "> ";
                textView.setText(Html.fromHtml(htmlFor, source -> {

                    int id = Integer.parseInt(source);

                    Drawable drawable = HostEnv.context.getResources().getDrawable(id, null);
                    drawable.setBounds(0, 0,textView.getHeight(),textView.getHeight());
                    return drawable;
                }, null));*/
                textView.setText(HostEnv.moduleName);
                ViewGroup.LayoutParams params = textView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                textView.setLayoutParams(params);
            });
        }
    }

}
