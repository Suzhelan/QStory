package HookItem.ChatHook.MessageProcessingCenter;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.note.XPOperate;
import lin.xposed.QQUtils.CommonClasses;

public class onMessageInit {
    private static final long StartTime = System.currentTimeMillis();


    public void getMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("hook", "addMessage set sendmsg extra ", m -> m.getDeclaringClass().getName().startsWith("com.tencent.imcore.message.")));
    }

    @XPOperate(ID = "hook")
    public HookAction action() {
        return param -> {
            Object MessageRecord = param.args[0];
            if (MessageRecord == null) return;
            //防止启动过早加载非及时监听消息
            if (System.currentTimeMillis() - StartTime < 10 * 1000) return;

            if (!CommonClasses.MessageRecord().isAssignableFrom(MessageRecord.getClass())) return;
            //参数传回消息处理中心
            MessageProcessingder.parseMessageSender(MessageRecord);
        };
    }
}
