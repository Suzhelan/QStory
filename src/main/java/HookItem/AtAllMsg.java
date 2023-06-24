package HookItem;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.QQUtils.CommonClasses;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

public class AtAllMsg {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = ClassifyMenu.streamline;
        ui.name = "屏蔽普通的[@全体成员]通知";
        return ui;
    }


    public void getMethod(MethodContainer container) {

        container.addMethod("hook",
                MethodUtils.findMethod("com.tencent.mobileqq.app.QQAppInterface",
                "notifyMessageReceived", void.class,
                new Class[]{CommonClasses.Message(), boolean.class, boolean.class}));
    }

    @XPOperate(ID = "hook")
    public HookAction hookAction() {
        return param -> {

            Object message = param.args[0];
            String messageText = FieIdUtils.getField(message, "msg", String.class);
            int bizType = FieIdUtils.getField(message, "bizType", int.class);
            if (bizType == 14 && messageText.contains("@全体成员"))
                param.setResult(null);

        };
    }
}
