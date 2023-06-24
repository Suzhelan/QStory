package HookItem.LoadItemInfo;


import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

//方法容器
public class MethodContainer {
    private final List<BaseMethodInfo> methodList = new ArrayList<>();

    public void addMethod(String id, Member m) {
        methodList.add(MethodFindBuilder.newCommonMethod(id, m));
    }

    public void addMethod(BaseMethodInfo baseMethodInfo) {
        methodList.add(baseMethodInfo);
    }

    public List<BaseMethodInfo> getMethodList() {
        return methodList;
    }
}
