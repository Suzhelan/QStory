package lin.xposed.QQUtils;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;

public class Common {


    public static void BaseChatPieInit(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("basechatpie_init", "AIO_doOnCreate_initUI", m -> m.getDeclaringClass().getName().equals("com.tencent.mobileqq.activity.aio.core.BaseChatPie")));
    }

    //消息
    public static void AIOMessageListAdapter_getView(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("onAIOGetView", "AIO_ChatAdapter_getView", m -> m.getDeclaringClass().getName().startsWith("com.tencent.mobileqq.activity.aio")));
    }
}
