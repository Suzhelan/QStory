package HookItem.LoadItemInfo.Template;

import HookItem.LoadItemInfo.MethodContainer;

//基础功能信息模板
public abstract class ItemAction {
    protected HookItem.loadHook.UIInfo UIInfo;

    public abstract HookItem.loadHook.UIInfo getBaseInfo();

    public abstract void findMethod(MethodContainer methodContainer);

    public abstract HookAction getAction();
}
