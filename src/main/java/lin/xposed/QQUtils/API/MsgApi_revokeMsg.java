package lin.xposed.QQUtils.API;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.loadHook.HookItemMainInfo;
import HookItem.note.ApiMark;
import lin.xposed.HostEnv;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Method;


public class MsgApi_revokeMsg {
    HookItemMainInfo.XPItemInfo info;
    private Class<?> updateCache;

    @ApiMark
    public void revoke(Object msg) throws Exception {
        Object MessageFacade = MethodUtils.callNoParamsMethod(HostEnv.AppInterface, "getMessageFacade",
                ClassUtils.getClass("com.tencent.imcore.message.QQMessageFacade"));
        Object MsgCache = MethodUtils.callMethodByName(HostEnv.AppInterface, "getMsgCache");
        ((Method) info.scanResult.get("updateCache")).invoke(MsgCache, true);
        ((Method) info.scanResult.get("revoke")).invoke(MessageFacade, msg);
    }


    public void getRevoke_8893(MethodContainer container) {
        Method target = MethodUtils.findMethod(ClassUtils.getClass("com.tencent.imcore.message.QQMessageFacade"), null, void.class, new Class[]{
                ClassUtils.getClass("com.tencent.mobileqq.data.MessageForFile")
        });
        container.addMethod(MethodFindBuilder.newFinderWhichMethodInvoking("revoke", target, m -> m.getDeclaringClass().getName().equals("com.tencent.imcore.message.QQMessageFacade")));
    }


    public void getUpdateCache_890(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("get_update_cache", "--->>getBuddyMsgLastSeq: ", m -> {
            updateCache = m.getDeclaringClass();
            return true;
        }));
        container.addMethod(MethodFindBuilder.newFindMethodByName("getMethod_Before", "qq queryEmojiInfo: result:", m -> true));
        container.addMethod(MethodFindBuilder.newFinderByMethodInvokingLinked("updateCache", "getMethod_Before", m -> m.getDeclaringClass().equals(updateCache) && ((Method) m).getReturnType().equals(void.class) && ((Method) m).getParameterCount() == 1 && ((Method) m).getParameterTypes()[0].equals(boolean.class)));
    }
}
