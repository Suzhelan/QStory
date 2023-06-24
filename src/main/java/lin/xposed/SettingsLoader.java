package lin.xposed;

import HookItem.LoadItemInfo.BaseMethodInfo;
import HookItem.loadHook.HookItemMainInfo;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.Initialize.PathInit;
import lin.xposed.Utils.LogUtils;
import lin.xposed.Utils.MFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class SettingsLoader {

    private final static String KEY = "linjiangtayubufan";
    private static HashMap<String, SwitchInfo> switchSettings = new LinkedHashMap<>();

    public static void saveSettings() {
        if (switchSettings == null) switchSettings = new LinkedHashMap<>();
        //保存所有的设置到本地
        for (HookItemMainInfo.XPItemInfo info : HookItemMainInfo.itemInstance.values()) {
            SwitchInfo switchInfo = new SwitchInfo();
            switchInfo.ItemId = info.id;
            switchInfo.Enabled = info.Enabled;
            //具体哪个方法钩的开关
            for (BaseMethodInfo baseInfo : info.NeedMethodInfo.values()) {
                switchInfo.hookOpen.put(baseInfo.HookID, baseInfo.TheHookOpens);
            }
            switchSettings.put(info.id, switchInfo);
        }
        try {
            MFileUtils.writeObjectToFile(new File(PathInit.paths[1] + "Switch_Settings"), switchSettings);
        } catch (IOException e) {
            LogUtils.addError(e);
            CommonTool.Toast("[QStory]保存设置失败 该错误通常不是用户可以解决的 请尝试反馈给开发者");
        }
    }

    public static void loadSettings() {
        //读取本地设置 在查找完方法之后hook之前调用
        //如果设置文件不存在则保存一次
        if (!new File(PathInit.paths[1] + "Switch_Settings").exists()) {
            saveSettings();
        }
        try {
            switchSettings = (LinkedHashMap<String, SwitchInfo>) MFileUtils.readFileObject(new File(PathInit.paths[1] + "Switch_Settings"));
        } catch (Exception e) {
            saveSettings();
            LogUtils.addError(e);
            CommonTool.Toast("加载设置失败");
            return;
        }
        for (HookItemMainInfo.XPItemInfo info : HookItemMainInfo.itemInstance.values()) {
            SwitchInfo switchInfo = switchSettings.get(info.id);
            //如果设置里没有这个item很可能是新加的功能 略过
            if (switchInfo == null) continue;
            info.Enabled = switchInfo.Enabled;
            for (BaseMethodInfo baseInfo : info.NeedMethodInfo.values()) {
                Boolean open = switchInfo.hookOpen.get(baseInfo.HookID);
                //防止是新加的钩方法
                if (open == null) continue;
                baseInfo.TheHookOpens = open;
            }
        }
    }

    public static class SwitchInfo implements Serializable {
        public String ItemId;
        public boolean Enabled;
        public HashMap<String, Boolean> hookOpen = new HashMap<>();
    }




}
