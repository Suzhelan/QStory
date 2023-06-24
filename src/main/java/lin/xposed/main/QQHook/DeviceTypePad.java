package lin.xposed.main.QQHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.HookItemMainInfo;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Field;

public class DeviceTypePad {

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "平板模式";
        ui.info = "重启QQ生效";
        ui.groupType = ClassifyMenu.base;
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("hook", "initDeviceType type = ", member -> true));
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction hook_1() {
        return param -> {
            Class<?> clz = ClassUtils.getClass("com.tencent.common.config.pad.DeviceType");
            Object type = MethodUtils.callStaticMethod(clz, "valueOf", clz, new Class[]{String.class}, "TABLET");
            Field field = FieIdUtils.findFirstField(HookItemMainInfo.itemInstance.get(this.getClass()).scanResult.get("hook").getDeclaringClass(), clz);
            field.set(null, type);
        };
    }
}
