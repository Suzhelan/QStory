package HookItem.ChatHook.Voice;

import HookItem.loadHook.UIInfo;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;

public class CopyVoiceUrl {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "语音外显";
        ui.groupType = ClassifyMenu.base;
        return ui;
    }

}
