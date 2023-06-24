package HookItem.ChatHook.Voice;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.ReflectUtils.MethodUtils;

public class HideChatTitleHandsetIcon {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "去除聊天窗口标题栏听筒标识";
        ui.info = "在听筒模式时触发";
        ui.groupType = "净化";
        return ui;
    }


    public void getMethod(MethodContainer container) {

        container.addMethod("hook",
                MethodUtils.findMethod("com.tencent.mobileqq.widget.navbar.NavBarAIO",
                "setEarIconVisible",
                void.class,new Class[]{boolean.class}));

    }

    @XPOperate(ID = "hook")
    public HookAction action() {
        return param -> {
            if ((boolean) param.args[0]) {
                param.args[0] = false;
            }
        };
    }
}
