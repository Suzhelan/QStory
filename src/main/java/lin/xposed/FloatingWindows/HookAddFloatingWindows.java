package lin.xposed.FloatingWindows;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.R;
import lin.xposed.ReflectUtils.PostMain;
import lin.xposed.Utils.MFileUtils;

import java.io.File;

//添加聊天界面悬浮窗入口项
public class HookAddFloatingWindows {
    public static Drawable icon;
    private final String iconPath = PathInit.path + "悬浮窗.png";

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = ClassifyMenu.base;
        ui.name = "悬浮窗";
        ui.info = "打开聊天界面时显示";
        InitIcon();
        return ui;
    }

    public void InitIcon() {
        new Thread(() ->
        {
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                icon = MFileUtils.iconDrawable(iconPath);
            } else {
                MFileUtils.drawableToFile(HostEnv.context.getDrawable(R.mipmap.icon), iconPath, Bitmap.CompressFormat.PNG);
                CommonTool.Toast("悬浮窗图标已初始化完成");
            }
        }).start();
    }

    public void addMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("ChatOnShow", "loadBackgroundAsync: skip for mosaic is on", m -> m.getDeclaringClass().getName().equals("com.tencent.mobileqq.activity.aio.core.BaseChatPie")));
        container.addMethod(MethodFindBuilder.newFindMethodByName("ChatOnHide", "doOnStop", m -> m.getDeclaringClass().getName().equals("com.tencent.mobileqq.activity.aio.core.BaseChatPie")));
    }

    //以下两个方法的运行时对象都会提供 SessionInfo
    @XPOperate(ID = "ChatOnShow", period = XPOperate.After)
    public HookAction hook_1() {
        return param -> new Thread(() -> PostMain.postMain(() -> FloatingWindowsButton.Display(true), 300)).start();
    }

    @XPOperate(ID = "ChatOnHide", period = XPOperate.After)
    public HookAction hook_2() {
        return param -> new Thread(() -> PostMain.postMain(() -> FloatingWindowsButton.Display(false), 300)).start();
    }
}
