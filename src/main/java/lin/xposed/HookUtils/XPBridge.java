package lin.xposed.HookUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class XPBridge {
    public static void hookClassAllMethodBefore(Class<?> clz, Action action) {
        for (Method method : clz.getDeclaredMethods()) {
            method.setAccessible(true);
            XPBridge.hookBefore(method, action);
        }
    }

    public static void hookReplace(Member m, replaceMethod replaceMethod) {
        XposedBridge.hookMethod(m, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return replaceMethod.execute(methodHookParam);
            }
        });
    }

    public static void hookBefore(Member member, Action action) {
        XposedBridge.hookMethod(member, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                action.execute(param);
            }
        });
    }

    public static void hookBefore(Member member, Action action, int i) {
        XposedBridge.hookMethod(member, new XC_MethodHook(i) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                action.execute(param);
            }
        });
    }

    public static void hookAfter(Member member, Action action, int i) {
        XposedBridge.hookMethod(member, new XC_MethodHook(i) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                action.execute(param);
            }
        });
    }

    public static void hookAfter(Member member, Action action) {
        XposedBridge.hookMethod(member, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                action.execute(param);
            }
        });
    }

    public static void HookAfterOnce(Member member, Action action) {
        AtomicReference<XC_MethodHook.Unhook> unAr = new AtomicReference<>();
        unAr.set(XposedBridge.hookMethod(member, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Unhook unhook = unAr.getAndSet(null);
                if (unhook != null) {
                    unhook.unhook();
                    action.execute(param);
                }
            }
        }));
    }

    public static void HookBeforeOnce(Member member, Action action) {
        AtomicReference<XC_MethodHook.Unhook> unAr = new AtomicReference<>();
        unAr.set(XposedBridge.hookMethod(member, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Unhook unhook = unAr.getAndSet(null);
                if (unhook != null) {
                    unhook.unhook();
                    action.execute(param);
                }
            }
        }));
    }

    public interface replaceMethod {
        Object execute(XC_MethodHook.MethodHookParam param);
    }

    public interface Action {
        void execute(XC_MethodHook.MethodHookParam param);
    }
}

