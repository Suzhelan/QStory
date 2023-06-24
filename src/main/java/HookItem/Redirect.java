package HookItem;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

public class Redirect {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = ClassifyMenu.streamline;
        ui.name = "尝试修改补丁中心重定向器";
        ui.info = "具体用途未知";
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("redirect", MethodUtils.findMethod("com.tencent.mobileqq.qfix.redirect.PatchRedirectCenter", "getRedirector", ClassUtils.getClass("com.tencent.mobileqq.qfix.redirect.IPatchRedirector"), new Class<?>[]{int.class, short.class}));
    }

    @XPOperate(ID = "redirect")
    public HookAction hookAction() {
        return param -> {
            int params1 = (int) param.args[0];
            short params2 = (short) param.args[1];
            Class<?> IPatchRedirector = ClassUtils.getClass("com.tencent.mobileqq.qfix.redirect.IPatchRedirector");
            if (IPatchRedirector == null) return;
            //已退出 49880, (short) 2
            if (params1 == 49880 && params2 == 2) {
                param.setResult(null);
            }
        };
    }
}
