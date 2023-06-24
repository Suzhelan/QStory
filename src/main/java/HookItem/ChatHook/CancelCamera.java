package HookItem.ChatHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.annotation.SuppressLint;
import android.view.View;
import android.widget.LinearLayout;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

public class CancelCamera {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "精简聊天界面相机入口";
        ui.info = "精简聊天界面聊天框上的相机入口";
        ui.groupType = "净化";
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("Delete", MethodUtils.findMethod("com.tencent.mobileqq.activity.aio.panel.PanelIconLinearLayout",
                null, void.class, new Class[]{ClassUtils.getClass("com.tencent.mobileqq.activity.aio.core.BaseChatPie")}));
    }

    @XPOperate(ID = "Delete", period = XPOperate.After)
    public HookAction action() {
        return param -> {
            LinearLayout layout = (LinearLayout) param.thisObject;
            //好友和群聊情况
            if (layout.getChildCount() >= 4) {
                @SuppressLint("ResourceType")
                View v = layout.getChildAt(2);
                if (v == null) return;
                layout.post(() -> {
                    layout.removeView(v);
                });
            } else if (layout.getChildCount() >= 2) {
                layout.post(() -> {
                    layout.removeViewAt(1);
                });
            }
        };
    }
}
