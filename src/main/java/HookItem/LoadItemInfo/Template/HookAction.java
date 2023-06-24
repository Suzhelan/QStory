package HookItem.LoadItemInfo.Template;

import de.robv.android.xposed.XC_MethodHook;

public interface HookAction {
    void Action(XC_MethodHook.MethodHookParam param) throws Throwable;
}