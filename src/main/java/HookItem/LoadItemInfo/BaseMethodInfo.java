package HookItem.LoadItemInfo;

import HookItem.loadHook.HookItemMainInfo;

import java.lang.reflect.Member;

public class BaseMethodInfo {
    public static final int TYPE_METHOD = 1;
    public static final int TYPE_FINDER_INFO = 0;

    public String HookID;
    public int type = TYPE_METHOD;
    public boolean TheHookOpens = true;
    public HookItemMainInfo.XPItemInfo bandToInfo;//等着被反射修改的上一级容器

    //需要方法的返回结果时使用此接口
    public interface MethodChecker {
        Object onMethod(Member member);
    }

}
