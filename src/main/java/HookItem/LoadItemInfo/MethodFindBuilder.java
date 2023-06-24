package HookItem.LoadItemInfo;

import java.lang.reflect.Member;

//工具工厂
public class MethodFindBuilder {
    public static BaseMethodInfo newCommonMethod(String id, Member member) {
        CommonBaseMethodInfo c = new CommonBaseMethodInfo();
        c.member = member;
        c.HookID = id;
        return c;
    }

    //查找方法内出现的字符串
    public static BaseMethodInfo newFindMethodByName(String id, String name, BaseMethodInfo.MethodChecker methodChecker) {
        FindBaseMethodByName info = new FindBaseMethodByName();
        info.type = BaseMethodInfo.TYPE_FINDER_INFO;
        info.HookID = id;
        info.name = name;
        info.checker = methodChecker;
        return info;
    }

    //查找方法调用哪个方法 参数二为调用的目标方法 参数三为本方法的执行条件
    public static BaseMethodInfo newFinderByMethodInvokingLinked(String ID, String LinkTarget, BaseMethodInfo.MethodChecker findCallback) {
        FindMethodInvokingMethod newInfo = new FindMethodInvokingMethod();
        newInfo.type = BaseMethodInfo.TYPE_FINDER_INFO;
        newInfo.HookID = ID;
        newInfo.LinkedToMethodID = LinkTarget;
        newInfo.checker = findCallback;
        return newInfo;
    }


    public static BaseMethodInfo newFinderWhichMethodInvoking(String ID, Member targetMethod, BaseMethodInfo.MethodChecker findCallback) {
        FindMethodsWhichInvokeToTargetMethod newInfo = new FindMethodsWhichInvokeToTargetMethod();
        newInfo.type = BaseMethodInfo.TYPE_FINDER_INFO;
        newInfo.HookID = ID;
        newInfo.checkMethod = targetMethod;
        newInfo.checker = findCallback;
        return newInfo;
    }

    public static BaseMethodInfo newFinderWhichMethodInvokingLinked(String ID, String LinkedMethod, BaseMethodInfo.MethodChecker findCallback) {
        FindMethodsWhichInvokeToTargetMethod newInfo = new FindMethodsWhichInvokeToTargetMethod();
        newInfo.type = BaseMethodInfo.TYPE_FINDER_INFO;
        newInfo.HookID = ID;
        newInfo.LinkedToMethodID = LinkedMethod;
        newInfo.checker = findCallback;
        return newInfo;
    }

}
