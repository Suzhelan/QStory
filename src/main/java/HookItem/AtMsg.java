package HookItem;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.FieIdUtils;

public class AtMsg {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = ClassifyMenu.streamline;
        ui.name = "屏蔽异常AT全体通知";
        return ui;
    }


    public void getMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("hook",
                " notificationElement: null ", member -> true));

    }

    @XPOperate(ID = "hook")
    public HookAction hookAction() {
        return param -> {
            Object message = param.args[1];
            if (isExceptionAtMsg(message)) {
                param.setResult(null);
            }
        };
    }

//    @XPOperate(ID = "hook_2")
    public HookAction hookAction_2() {
        return param -> {
            if (isExceptionAtMsg(param.args[0])) {
                param.setResult("[异常艾特全体]");
            }
        };
    }

    private boolean isExceptionAtMsg(Object message) throws Exception {
        String messageText = FieIdUtils.getField(message, "msg", String.class);
        int bizType = FieIdUtils.getField(message, "bizType", int.class);
        if (bizType == 25) {
            if (!messageText.contains("@")) {
                return true;
            } else return (messageText.length() - messageText.replaceAll("[^@]", "").length()) > 20;
        }
        return false;
    }
    /*@XPOperate(ID = "onAIOGetView", period = XPOperate.After)
    public HookAction getAct3() {
        return param -> {
            Object mGroupView = param.getResult();
            RelativeLayout mLayout;
            //获取适配器视图
            if (mGroupView instanceof RelativeLayout) mLayout = (RelativeLayout) mGroupView;
            else return;
            //获取消息列表
            List<Object> message = FieIdUtils.getFirstField(param.thisObject, List.class);
            if (message == null) {
                return;
            }
            //获取具体的消息
            Object chatMsg = message.get((int) param.args[0]);
            switch (chatMsg.getClass().getSimpleName()) {
                case "MessageForText":
                case "MessageForLongTextMsg":
                case "MessageForFoldMsg":
                int bizType = FieIdUtils.getField(chatMsg, "bizType", int.class);
                if (bizType == 25) {
                    String text = FieIdUtils.getField(chatMsg, "msg", String.class);
                    if (!text.contains("@")) {
                        MethodUtils.callMethod(mLayout, "setTailMessage", void.class, new Class[]{boolean.class, CharSequence.class, ClassUtils.getClass("android.view.View$OnClickListener")}, true, "该消息可能是异常的艾特消息", null);
                    }
                }
            }
        };
    }*/
}
