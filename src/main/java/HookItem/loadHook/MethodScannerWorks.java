package HookItem.loadHook;

import ConfigTool.GlobalConfig;
import DexFinder.DexHelperFinder;
import DexFinder.DexKitFinder;
import DexFinder.IDexFinder;
import HookItem.LoadItemInfo.*;
import android.util.Log;
import de.robv.android.xposed.XposedBridge;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.ReflectUtils.PostMain;
import lin.xposed.SettingsLoader;
import lin.xposed.Utils.FileUtils;
import lin.xposed.Utils.LogUtils;
import lin.xposed.Utils.MFileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MethodScannerWorks {
    private static final AtomicBoolean isLoaded = new AtomicBoolean();
    public static HashMap<String, HashMap<String, Object>> MethodInfo = new HashMap<>();
    static volatile boolean Init;
    static AtomicBoolean result = new AtomicBoolean();
    static ArrayList<IDexFinder> dexKit = new ArrayList<>();

    //可用?
    public static boolean IsAvailable() {
        if (Init) {
            return result.get();
        }
        //判断版本是否过期 没过期的话就不用查找方法
        result.set(NotExpired());
        return result.get();
    }

    //相对版本是否过期
    private static boolean NotExpired() {
        //当前的版本
        String s2 = "q->" + HostEnv.QQVersionName
                + "&module->" + HostEnv.moduleVersionName;
        if (!GlobalConfig.getString("Relative version").equals(s2)) {
            return false;
        }
        //没过期 加载已保存方法
        loadSaveMethod();
        return true;
    }

    private static void loadSaveMethod() {
        if (isLoaded.getAndSet(true)) return;
        try {
            MethodInfo = (HashMap<String, HashMap<String, Object>>) MFileUtils.readFileObject(new File(PathInit.paths[1] + "MethodInfo"));
        } catch (Exception e) {
            CommonTool.Toast(e);
            LogUtils.addRunLog("read File Method  " + Log.getStackTraceString(e));
            initStartFindMethod();
            return;
        }
        for (Map.Entry<Class<?>, HookItemMainInfo.XPItemInfo> entry : HookItemMainInfo.itemInstance.entrySet()) {
            HookItemMainInfo.XPItemInfo info = entry.getValue();
            for (BaseMethodInfo baseInfo : info.NeedMethodInfo.values()) {
                String id = info.id + "_" + baseInfo.HookID;
                if (baseInfo instanceof CommonBaseMethodInfo) {
                    info.scanResult.put(baseInfo.HookID, ((CommonBaseMethodInfo) baseInfo).member);
                } else {
                    try {
                        info.scanResult.put(baseInfo.HookID, getSaveMethod(id));
                    } catch (Exception e) {
                        LogUtils.addError(e);
                    }
                }
            }
        }
        //方法查找好加载本地设置
        SettingsLoader.loadSettings();
    }


    //初始化查找方法
    public static void initStartFindMethod() {

        new Thread(() -> FileUtils.deleteFile(new File(PathInit.paths[0]))).start();
        CommonTool.Toast("QStory开始查找方法");
        DexHelperFinder dexTool = new DexHelperFinder();
        dexTool.init(HostEnv.apkPath, ClassUtils.getHostLoader());
        dexKit.add(dexTool);
        DexKitFinder dexTool2 = new DexKitFinder();
        dexTool2.init(HostEnv.apkPath, ClassUtils.getHostLoader());
        dexKit.add(dexTool2);

        for (Map.Entry<Class<?>, HookItemMainInfo.XPItemInfo> entry : HookItemMainInfo.itemInstance.entrySet()) {
            HookItemMainInfo.XPItemInfo itemInfo = entry.getValue();
            if (itemInfo.NeedMethodInfo.isEmpty()) continue;
            for (BaseMethodInfo info : itemInfo.NeedMethodInfo.values()) {
                Method member = findMethod(info);
                if (member == null) {
                    LogUtils.addError("Method not found : " + info.HookID);
                } else {
                    itemInfo.scanResult.put(info.HookID, member);
                    if (!(info instanceof CommonBaseMethodInfo)) {
                        SaveMethodInfo(itemInfo.id + "_" + info.HookID, member);
                    }
                }
            }
        }
        /*
         * findAllMethodEndAfter Save AllMethodInfo To File
         * Save This AppVersion
         */
        try {
            //catch 如果错误了不会保存当前相对版本
            MFileUtils.writeObjectToFile(new File(PathInit.paths[1] + "MethodInfo"), MethodInfo);
            GlobalConfig.putString("Relative version", "q->" + HostEnv.QQVersionName
                    + "&module->" + HostEnv.moduleVersionName);
            PostMain.postMain(CommonTool::killAppProcess, 500);
        } catch (IOException e) {
            LogUtils.addError(e);
            LogUtils.addRunLog("尝试将查找到的方法保存本地 失败");
        }
    }

    //查找方法
    private static Method findMethod(BaseMethodInfo info) {
        try {
            if (info instanceof CommonBaseMethodInfo) {
                return (Method) ((CommonBaseMethodInfo) info).member;
            } else if (info instanceof FindBaseMethodByName nameInfo) {
                for (IDexFinder dexFinder : dexKit) {
                    Method[] methods = dexFinder.findMethodByString(nameInfo.name);
                    for (Method m : methods) {
                        Object rec = nameInfo.checker.onMethod(m);
                        if (rec instanceof Boolean) {
                            if ((Boolean) rec) return m;
                        }
                        if (rec instanceof Method) {
                            return (Method) rec;
                        }
                    }
                }
            } else if (info instanceof FindMethodInvokingMethod invokingMethod) {
                Method linkMethod;
                if (invokingMethod.checkMethod != null) {
                    linkMethod = (Method) invokingMethod.checkMethod;
                } else if (invokingMethod.LinkedToMethodID != null) {
                    linkMethod = getSaveMethod(info.bandToInfo.id + "_" + invokingMethod.LinkedToMethodID);
                } else {
                    return null;
                }
                for (IDexFinder dexFinder : dexKit) {
                    Method[] methods = dexFinder.findMethodInvoking(linkMethod);
                    for (Method m : methods) {
                        Object rec = invokingMethod.checker.onMethod(m);
                        if (rec instanceof Boolean) {
                            if ((boolean) rec)
                                return m;
                        }
                        if (rec instanceof Method) {
                            return (Method) rec;
                        }
                    }
                }
            } else if (info instanceof FindMethodsWhichInvokeToTargetMethod invoke) {
                Method linkMethod;
                if (invoke.checkMethod != null) {
                    linkMethod = (Method) invoke.checkMethod;
                } else if (invoke.LinkedToMethodID != null) {
                    linkMethod = getSaveMethod(info.bandToInfo.id + "_" + invoke.LinkedToMethodID);
                } else {
                    return null;
                }
                for (IDexFinder dexFinder : dexKit) {
                    Method[] methods = dexFinder.findMethodBeInvoked(linkMethod);
                    for (Method m : methods) {
                        Object rec = invoke.checker.onMethod(m);
                        if (rec instanceof Boolean) {
                            if ((boolean) rec)
                                return m;
                        }
                        if (rec instanceof Method) {
                            return (Method) rec;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
        return null;
    }

    private static Method getSaveMethod(String id) {

        try {
            Map<String, Object> methodInfo = MethodInfo.get(id);
            ArrayList<String> arrayList = (ArrayList<String>) methodInfo.get("params");
            if (arrayList == null) CommonTool.Toast("array null");
            Class<?>[] params = new Class[arrayList.size()];

            for (int i = 0; i < arrayList.size(); i++) {
                params[i] = ClassUtils.getClass(arrayList.get(i));
            }
            try {
                return MethodUtils.findMethod(
                        ClassUtils.getClass((String) methodInfo.get("class")),
                        (String) methodInfo.get("name"),
                        ClassUtils.getClass((String) methodInfo.get("recName")),
                        params
                );
            } catch (Exception e) {
                XposedBridge.log(Log.getStackTraceString(e));
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

    private static void SaveMethodInfo(String id, Method method) {
        HashMap<String, Object> methodInfo = new HashMap<>();
        methodInfo.put("class", method.getDeclaringClass().getName());
        methodInfo.put("name", method.getName());
        methodInfo.put("recName", method.getReturnType().getName());
        ArrayList<String> params = new ArrayList<>();
        for (Class<?> type : method.getParameterTypes()) {
            params.add(type.getName());
        }
        methodInfo.put("params", params);
        MethodInfo.put(id, methodInfo);
    }

}
