package lin.xposed.main.QQHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.app.Activity;
import android.view.ViewGroup;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

public class MiniProgram {

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "屏蔽主页下拉小程序";
        ui.groupType = ClassifyMenu.streamline;
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("hook", MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.mini.api.impl.MiniAppServiceImpl"), "createMiniAppEntryManager", ClassUtils.getClass("com.tencent.mobileqq.mini.entry.MiniAppPullInterface"), new Class[]{
                boolean.class, Activity.class, Object.class, Object.class, Object.class, Object.class, ViewGroup.class
        }));
    }

    @XPOperate(ID = "hook")
    public HookAction hook_1() {
        return param -> {
            param.setResult(null);
        };
    }
}
