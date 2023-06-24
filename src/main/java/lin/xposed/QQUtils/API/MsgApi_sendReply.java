package lin.xposed.QQUtils.API;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.loadHook.HookItemMainInfo;
import HookItem.note.ApiMark;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Method;

public class MsgApi_sendReply {
    HookItemMainInfo.XPItemInfo info;


    public void MethodScaner(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("method", "sendReplyMessage chatMessage is null", m -> true));
    }


    @ApiMark
    public void send_890(Object _Session, Object replyRecord) throws Exception {
        Method mMethod = MethodUtils.findMethod(info.scanResult.get("method").getDeclaringClass(), null, void.class, new Class[]{
                ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"),
                ClassUtils.getClass("com.tencent.mobileqq.data.ChatMessage"),
                ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"),
                int.class,
                int.class,
                boolean.class
        });
        Object Call = MethodUtils.callStaticNoParamsMethod(info.scanResult.get("method").getDeclaringClass(), null, info.scanResult.get("method").getDeclaringClass());
        mMethod.invoke(Call, QQEnvUtils.getAppRuntime(), replyRecord, _Session, 2, 0, false);
    }
}
