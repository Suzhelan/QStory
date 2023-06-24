package lin.xposed.QQUtils;

import lin.xposed.ReflectUtils.ClassUtils;

public class CommonClasses {

    public static Class<?> MessageRecord() {
        return ClassUtils.getClass("com.tencent.mobileqq.data.MessageRecord");
    }

    public static Class<?> Message() {
        return ClassUtils.getClass("com.tencent.imcore.message.Message");
    }
}
