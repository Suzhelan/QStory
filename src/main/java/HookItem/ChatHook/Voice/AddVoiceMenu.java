package HookItem.ChatHook.Voice;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.content.Context;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.QQUtils.FindQQMethod;
import lin.xposed.ReflectUtils.*;

import java.lang.reflect.Array;

public class AddVoiceMenu {
    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "保存语音";
        ui.groupType = ClassifyMenu.base;
        return ui;
    }

    public void addMethod(MethodContainer container) {
        container.addMethod("addButtonToVoiceMenu", FindQQMethod.findMessageMenu(ClassUtils.getClass("com.tencent.mobileqq.activity.aio.item.PttItemBuilder")));
        container.addMethod("addItemButtonListener", MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.activity.aio.item.PttItemBuilder"), "a", void.class, new Class[]{
                int.class, Context.class, ClassUtils.getClass("com.tencent.mobileqq.data.ChatMessage")}));
    }

    @XPOperate(ID = "addButtonToVoiceMenu", period = XPOperate.After)
    public HookAction Hook_1() {
        return param -> {
            //添加menu
            //获取构造器构造好的View
            Object arr = param.getResult();
            Object ret = Array.newInstance(arr.getClass().getComponentType(), Array.getLength(arr) + 1);
            //复制数组
            System.arraycopy(arr, 0, ret, 1, Array.getLength(arr));
            //new一个menuitem
            Object MenuItem = ConstructorUtils.newInstance(arr.getClass().getComponentType(), 4192, "保存到An");
            //设置该菜单的展示优先级
            FieIdUtils.setField(MenuItem, "c", Integer.MAX_VALUE - 2);
            Array.set(ret, 0, MenuItem);
            param.setResult(ret);
        };
    }

    @XPOperate(ID = "addItemButtonListener", period = XPOperate.After)
    public HookAction hook_2() {
        return param -> {
            int InvokeID = (int) param.args[0];
            Context mContext = (Context) param.args[1];
            Object chatMsg = param.args[2];
            if (InvokeID == 4192) {
                String PTTPath = MethodUtils.callNoParamsMethod(chatMsg, "getLocalFilePath", String.class);
                //保存语音
                VoiceTools.createSaveVoiceDialog(mContext, PTTPath);
            }
        };
    }
}
