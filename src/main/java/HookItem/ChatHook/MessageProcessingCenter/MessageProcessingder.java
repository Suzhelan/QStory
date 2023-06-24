package HookItem.ChatHook.MessageProcessingCenter;

import HookItem.ChatHook.PictureDisplaysText;
import HookItem.ChatHook.Voice.VoiceRedPacketFlag;
import HookItem.loadHook.HookItemMainInfo;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.ConstructorUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;

import static HookItem.ChatHook.PictureDisplaysText.PicText;

public class MessageProcessingder {
    //解析消息确定发送者
    public static void parseMessageSender(Object MessageRecord) {
        try {
            int sendType = FieIdUtils.getField(MessageRecord, "istroop", int.class);
            if (sendType == 1 || sendType == 0) {
                String sendUin = FieIdUtils.getField(MessageRecord, "senderuin", String.class);
                if (sendUin.equals(QQEnvUtils.getCurrentUin())) {
                    MyselfMessageType(MessageRecord);
                } else {

                }
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
    }

    public static void OnAllMessage(Object MessageRecord) {
        String simpleName = MessageRecord.getClass().getSimpleName();
        if (simpleName.equalsIgnoreCase("MessageForPic")) {

        } else if (simpleName.equalsIgnoreCase("MessageForPtt")) {

        } else if (simpleName.equalsIgnoreCase("MessageForArkApp") && simpleName.equalsIgnoreCase("MessageForStructing")) {

        }
    }

    public static String getCardMsg(Object msg) {
        try {
            String clzName = msg.getClass().getSimpleName();
            if (clzName.equalsIgnoreCase("MessageForStructing")) {
                Object Structing = FieIdUtils.getField(msg, "structingMsg", ClassUtils.getClass("com.tencent.mobileqq.structmsg.AbsStructMsg"));
                return MethodUtils.callNoParamsMethod(Structing, "getXml", String.class);
            }
            if (clzName.equalsIgnoreCase("MessageForArkApp")) {
                Object ArkAppMsg = FieIdUtils.getField(msg, "ark_app_message", ClassUtils.getClass("com.tencent.mobileqq.data.ArkAppMessage"));
                return MethodUtils.callNoParamsMethod(ArkAppMsg, "toAppXml", String.class);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    //处理自己的消息类型
    public static void MyselfMessageType(Object MessageRecord) {
        String simpleName = MessageRecord.getClass().getSimpleName();
        if (simpleName.equalsIgnoreCase("MessageForPic")) {
            onMyselfPicMessage(MessageRecord);
        } else if (simpleName.equalsIgnoreCase("MessageForPtt")) {
            onMyselfVoiceMessage(MessageRecord);
        } else if (simpleName.equalsIgnoreCase("MessageForArkApp") && simpleName.equalsIgnoreCase("MessageForStructing")) {

        }
    }

    //语音处理
    public static void onMyselfVoiceMessage(Object MessageRecord) {
        setVoiceRedPacketFlag(MessageRecord);
    }

    public static void setVoiceRedPacketFlag(Object MessageRecord) {
        int flag = VoiceRedPacketFlag.VoiceFlag;
        if (flag == 0) return;
        if (!HookItemMainInfo.itemInstance.containsKey(VoiceRedPacketFlag.class)) return;
        if (HookItemMainInfo.itemInstance.get(VoiceRedPacketFlag.class).Enabled) {
            try {
                LogUtils.addRunLog("static voice SSS flag==" + flag);
                FieIdUtils.setField(MessageRecord, "sttText", "sttText");
                FieIdUtils.setField(MessageRecord, "voiceType", flag);
                FieIdUtils.setField(MessageRecord, "voiceRedPacketFlag", flag);
                LogUtils.addRunLog("hook SSS end " + FieIdUtils.getField(MessageRecord, "voiceType", int.class));
            } catch (Exception e) {
                LogUtils.addError(e);
            }
        }
    }

    //图片处理
    public static void onMyselfPicMessage(Object MessageRecord) {
        setPicText(MessageRecord);
    }

    public static void setPicText(Object MessageRecord) {
        if (PicText.equals("")) return;
        try {
            if (HookItemMainInfo.itemInstance.get(PictureDisplaysText.class).Enabled) {
                Class<?> PicData = ClassUtils.getClass("com.tencent.mobileqq.data.PicMessageExtraData");
                Object PicInfo = FieIdUtils.getField(MessageRecord, "picExtraData", PicData);
                if (PicInfo == null) {
                    FieIdUtils.setField(MessageRecord, "picExtraData", ConstructorUtils.newInstance(PicData));
                    PicInfo = FieIdUtils.getField(MessageRecord, "picExtraData", PicData);
                }
                FieIdUtils.setField(PicInfo, "textSummary", PicText);
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
    }
}
