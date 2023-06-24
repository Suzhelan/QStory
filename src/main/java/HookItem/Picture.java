package HookItem;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.LoadItemInfo.Template.ItemAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.QQUtils.Common;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.util.List;

public class Picture extends ItemAction {

    @Override
    public HookItem.loadHook.UIInfo getBaseInfo() {
        if (this.UIInfo != null) return this.UIInfo;
        HookItem.loadHook.UIInfo UIInfo = new UIInfo();
        UIInfo.name = "闪照破解";
        UIInfo.info = "即使闪照已经查看";
        UIInfo.groupType = ClassifyMenu.base;
        return UIInfo;
    }

    @Override
    public void findMethod(MethodContainer methodContainer) {
        //按名称查找方法--可以防止类名混淆
        methodContainer.addMethod(MethodFindBuilder.newFindMethodByName("getAction",
                "FlashPicHelper",
                member -> MethodUtils.findMethod(member.getDeclaringClass(), null, boolean.class, new Class[]{ClassUtils.getClass("com.tencent.mobileqq.data.MessageRecord")})
        ));
        methodContainer.addMethod(MethodFindBuilder.newFinderWhichMethodInvoking("getAction2",
                        MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.pic.api.IPicFlash"),
                                "isFlashPicMsg", boolean.class, new Class[]{ClassUtils.getClass("com.tencent.mobileqq.data.MessageRecord")}),
                        member -> member.getDeclaringClass().getName().startsWith("com.tencent.mobileqq.activity.aio.item")
                                && !member.getDeclaringClass().getName().contains("ItemBuilder")
                )
        );
        Common.AIOMessageListAdapter_getView(methodContainer);
    }

    @XPOperate(ID = "getAction", period = XPOperate.After)
    @Override
    public HookAction getAction() {
        return param -> {
            //闪照设置为未查看状态
            boolean result = (boolean) param.getResult();
            if (result) {
                Object messageRecord = param.args[0];
                MethodUtils.callMethod(messageRecord, "saveExtInfoToExtStr", void.class, new Class[]{String.class, String.class}, "flash_pic_flag", "1");
                String UserUin = FieIdUtils.getField(messageRecord, "senderuin", String.class);
                if (!UserUin.equals(QQEnvUtils.getCurrentUin()))
                    param.setResult(false);
            }
        };
    }

    @XPOperate(ID = "getAction2", period = XPOperate.After)
    public HookAction getAction2() {
        return param -> {
            //闪照类型图片显示为普通图片
            int re = (int) param.getResult();
            if (re == 66) {
                Object message = param.args[1];
                MethodUtils.callMethod(message, "saveExtInfoToExtStr", void.class, new Class[]{String.class, String.class}, "flash_pic_flag", "1");
                param.setResult(1);
            }
        };
    }

    @XPOperate(ID = "onAIOGetView", period = XPOperate.After)
    public HookAction getAct3() {
        return param -> {
            //添加闪照标签
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
            String ExStr = FieIdUtils.getField(chatMsg, "extStr", String.class);
            if (!TextUtils.isEmpty(ExStr) && ExStr.contains("flash_pic_flag")) {
                MethodUtils.callMethod(mLayout, "setTailMessage", void.class, new Class[]{boolean.class, CharSequence.class, ClassUtils.getClass("android.view.View$OnClickListener")}, true, "闪照", null);
            } else {
                //如果没有获取到extStr
                TextView textView = mLayout.findViewById(QQEnvUtils.getTargetID("chat_item_tail_message"));
                if (textView != null) {
                    String text = textView.getText().toString();
                    if (text.equals("闪照")) {
                        MethodUtils.callMethod(mLayout, "setTailMessage", void.class, new Class[]{boolean.class, CharSequence.class, ClassUtils.getClass("android.view.View$OnClickListener")}, false, "", null);
                    }
                }
            }
        };
    }
}
