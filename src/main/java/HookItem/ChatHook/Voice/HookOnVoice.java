package HookItem.ChatHook.Voice;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.ReflectUtils.MethodUtils;

public class HookOnVoice {

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "禁用语音自动切换听筒";
        ui.info = "禁用语音播放时自动来回切换";
        ui.groupType = "功能";
        return ui;
    }

    public void getMethod(MethodContainer container) {

        container.addMethod("hook", MethodUtils.findMethod("com.tencent.mobileqq.qqaudio.audioplayer.impl.AudioDeviceServiceImpl","notifyAllDeviceStatusChanged",void.class,new Class[]{
                int.class,
                boolean.class
        }));
    }

    @XPOperate(ID = "hook")
    public HookAction action() {
        return param -> {
            param.setResult(null);
        };
    }

}
