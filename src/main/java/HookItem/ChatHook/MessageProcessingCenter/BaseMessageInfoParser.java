package HookItem.ChatHook.MessageProcessingCenter;

import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.Utils.LogUtils;

//解析消息内容
public class BaseMessageInfoParser {
    public static BaseMessageInfo getMessageInfo(Object message) {
        BaseMessageInfo baseMessageInfo = new BaseMessageInfo();
        baseMessageInfo.msg = message;
        try {
            Integer istroop = FieIdUtils.getField(message, "istroop", int.class);
            baseMessageInfo.IsGroup = istroop == 1;
            if (istroop == 1) {
                baseMessageInfo.GroupUin = FieIdUtils.getField(message, "frienduin", String.class);
            }
            baseMessageInfo.Time = FieIdUtils.getField(message, "time", long.class);
            baseMessageInfo.sendUin = FieIdUtils.getField(message, "senderuin", String.class);

            String type = message.getClass().getSimpleName();
            if (type.matches("MessageForFoldMsg|MessageForLongTextMsg|MessageForText")) {
                baseMessageInfo.messageType = 0;
                baseMessageInfo.message = FieIdUtils.getField(message, "msg", String.class);
            } else if (type.equals("MessageForPic")) {
                baseMessageInfo.messageType = 1;
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
        return baseMessageInfo;
    }

    public static class BaseMessageInfo {
        public boolean IsGroup;
        public String sendUin;
        public String GroupUin;
        public int messageType;
        public String message;
        public Object msg;
        public long Time;
    }
}
