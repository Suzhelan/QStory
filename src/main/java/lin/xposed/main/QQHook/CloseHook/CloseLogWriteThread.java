package lin.xposed.main.QQHook.CloseHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.HookUtils.XPBridge;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class CloseLogWriteThread {
    List<String> logLevelNameList = Arrays.asList("w", "i", "d", "q");

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "禁用日志写入";
        ui.groupType = ClassifyMenu.streamline;
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("hook", MethodUtils.findMethod("com.tencent.qphone.base.util.QLogItemManager$WriteHandler", "tryInit", void.class, new Class[0]));
    }

    @XPOperate(ID = "hook")
    public HookAction hook_1() {
        return param -> {
            Class<?> logClass = ClassUtils.getClass("com.tencent.qphone.base.util.QLog");
            if (logClass != null) {
                for (Method m : logClass.getDeclaredMethods()) {
                    m.setAccessible(true);
                    if (logLevelNameList.contains(m.getName())) {
                        XPBridge.hookBefore(m, param1 -> {
                            if (((Method) param1.method).getReturnType().equals(void.class)) {
                                param1.setResult(null);
                            }
                        });
                    }
                }
            }
            param.setResult(null);
        };
    }
}
