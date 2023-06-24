package HookItem.ChatHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.note.XPOperate;
import lin.xposed.HostEnv;
import lin.xposed.QQUtils.Common;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;

//查找界面和会话对象
public class ChatItemInit {
    public static Object ChatPie;

    public void addMethod(MethodContainer container) {
        Common.BaseChatPieInit(container);
    }

    @XPOperate(ID = "basechatpie_init", period = XPOperate.After)
    public HookAction init() {
        return param -> {
            ChatPie = param.thisObject;
            HostEnv.AppInterface = FieIdUtils.getFirstField(ChatPie, ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"));
            HostEnv.SessionInfo = FieIdUtils.getFirstField(ChatPie, ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"));
        };
    }
}
