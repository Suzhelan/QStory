package lin.xposed.main.QQHook.CloseHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.content.Context;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.util.HashMap;
import java.util.Map;

public class SpaceAdvertising {
    private static final HashMap<String, String> qz = new HashMap<>();

    static {
        qz.put("base", "com.qzone.module.feedcomponent.ui.FeedView");
        qz.put("朋友庆祝视图", "FriendAnniversaryFeedView");
        qz.put("负面反馈视图", "NegativeFeedbackFeedView");
        qz.put("推荐关注垂直提要视图", "RecomFollowVerticalFeedView");
        qz.put("朋友视频", "FriendVideoFeedView");
        qz.put("粘滞笔记源视图", "StickyNoteFeedView");
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "屏蔽空间广告";
        ui.groupType = ClassifyMenu.streamline;
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("hook_2", MethodUtils.findMethod("com.qzone.module.feedcomponent.ui.FeedViewBuilder", "setFeedViewData", void.class,
                new Class[]{Context.class, ClassUtils.getClass("com.qzone.proxy.feedcomponent.ui.AbsFeedView"), ClassUtils.getClass("com.qzone.proxy.feedcomponent.model.BusinessFeedData"), boolean.class, boolean.class}));

    }

    @XPOperate(ID = "hook_2")
    public HookAction hook_2() {
        return param -> {
            Object CellOperationInfo = MethodUtils.callNoParamsMethod(param.args[2], "getOperationInfo", ClassUtils.getClass("com.qzone.proxy.feedcomponent.model.CellOperationInfo"));
            Map<Integer, String> busiParam = FieIdUtils.getField(CellOperationInfo, "busiParam", Map.class);
            if (busiParam.containsKey(194)) {
                param.setResult(null);
            } else if (busiParam.containsKey(101) && busiParam.get(101).contains("qq.com")) {
                param.setResult(null);
            }
        };
    }

}
