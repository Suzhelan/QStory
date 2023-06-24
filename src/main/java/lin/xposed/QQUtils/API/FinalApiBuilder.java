package lin.xposed.QQUtils.API;

import HookItem.loadHook.HookItemMainInfo;
import lin.xposed.Utils.LogUtils;

public class FinalApiBuilder {
    public static <Any> Any builder(Class<?> c, Object... params) {
        HookItemMainInfo.XPItemInfo info = HookItemMainInfo.itemInstance.get(c);
        if (info != null) {
            try {
                return (Any) info.ApiMethod.invoke(info.instance, params);
            } catch (Exception e) {
                LogUtils.addError("buider Error", e);
            }
        }
        LogUtils.addError("Api buider null " + c.getName());
        return null;
    }
}
