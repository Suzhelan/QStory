package lin.xposed.QQUtils;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.MethodUtils;

public class GroupNotifications {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "不允许QQ使用群通知服务";
        ui.info = "群申请类通知相关功能会彻底寄掉";
        ui.groupType = ClassifyMenu.mainClassify[2];
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newCommonMethod("AppRuntime", MethodUtils.findUnknownReturnMethod("mqq.app.AppRuntime", "getRuntimeService", new Class[]{Class.class, String.class})));
    }

    @XPOperate(ID = "AppRuntime")
    public HookAction hook_1() {
        return param -> {
            String name = ((Class<?>) param.args[0]).getName();
            if (name.equals("com.tencent.mobileqq.troop.api.ITroopNotificationService")) {
                param.setResult(null);
            }
        };
    }
}

