package lin.xposed.QQUtils.API;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.loadHook.HookItemMainInfo;
import HookItem.note.ApiMark;
import lin.xposed.HostEnv;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Method;

public class MsgApi_GetMessageByTimeSeq {
    HookItemMainInfo.XPItemInfo info;

    @ApiMark
    public Object api_invoker(String uin, int istroop, long msgseq) throws Exception {
        if (HostEnv.AppInterface == null) return null;
        Object MessageFacade = MethodUtils.callNoParamsMethod(HostEnv.AppInterface, "getMessageFacade",
                ClassUtils.getClass("com.tencent.imcore.message.QQMessageFacade"));
        return ((Method) info.scanResult.get("invoker")).invoke(MessageFacade, uin, istroop, msgseq);
    }

    public void getMethod_8_8_93(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("get_before", "counter", m -> m.getDeclaringClass().getName().contains("com.tencent.mobileqq.guild.chatpie.msgviewbuild.builder.GuildReplyTextItemBuilder")));
        container.addMethod(MethodFindBuilder.newFinderByMethodInvokingLinked("invoker", "get_before", m -> m.getDeclaringClass().getName().equals("com.tencent.imcore.message.BaseQQMessageFacade")));
    }
}
