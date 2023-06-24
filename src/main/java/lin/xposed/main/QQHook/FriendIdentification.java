package lin.xposed.main.QQHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;

public class FriendIdentification {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "去除关系标识";
        ui.info = "去除陌生人资料页-你们的关系标识";
        ui.groupType = ClassifyMenu.streamline;
        return ui;
    }

    public void getMethod(MethodContainer container) {

        container.addMethod("hook", MethodUtils.findMethod("com.tencent.mobileqq.profilecard.component.ProfileInStepComponent", "onDataUpdate",
                boolean.class, new Class[]{ClassUtils.getClass("com.tencent.mobileqq.profilecard.data.ProfileCardInfo")}));
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction hook_1() {
        return param -> {
            Object recyclerView = FieIdUtils.getFirstField(param.thisObject, ClassUtils.getClass("com.tencent.biz.richframework.widget.listview.card.RFWCardListView"));
            if (recyclerView == null) return;
            LinearLayout parent = MethodUtils.callNoParamsMethod(recyclerView, "getParent", ViewParent.class);
            if (parent == null) {
                LogUtils.addRunLog("好友标识屏蔽", "parent==null");
                return;
            }
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (parent.getChildAt(i).getVisibility() != View.GONE) parent.getChildAt(i).setVisibility(View.GONE);
            }
            if (parent.getVisibility() != View.GONE) parent.setVisibility(View.GONE);

        };
    }
}
