package lin.xposed.QQUtils;

import lin.xposed.QQUtils.API.FinalApiBuilder;
import lin.xposed.QQUtils.API.MsgApi_GetMessageByTimeSeq;

public class MessageUtils {
    public static Object FindMessageByTime(String uin, int istroop, long msgseq) {
        return FinalApiBuilder.builder(MsgApi_GetMessageByTimeSeq.class, uin, istroop, msgseq);
    }
}
