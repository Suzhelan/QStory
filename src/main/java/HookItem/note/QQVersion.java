package HookItem.note;

import lin.xposed.ReflectUtils.ClassUtils;

public class QQVersion {
    public static final int QQ_8_9_35 = 3814;
    public static final int QQ_8_9_38 = 3856;
    public static final int QQ_8_9_50 = 3898;
    public static final int QQ_8_9_58 = 4106;

    public static boolean isQQNT() {
        try {
            ClassUtils.getClass("com.tencent.mobileqq.startup.step.LoadData");
            return false;
        } catch (Exception e) {
            return true;
        }
    }
}
