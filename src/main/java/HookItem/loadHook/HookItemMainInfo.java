package HookItem.loadHook;

import HookItem.AtAllMsg;
import HookItem.AtMsg;
import HookItem.ChatHook.*;
import HookItem.ChatHook.MessageProcessingCenter.onMessageInit;
import HookItem.ChatHook.RecallMessage.TestItemHook;
import HookItem.ChatHook.Repetition.MainRepetitionIcon;
import HookItem.ChatHook.Voice.AddVoiceMenu;
import HookItem.ChatHook.Voice.HideChatTitleHandsetIcon;
import HookItem.ChatHook.Voice.HookOnVoice;
import HookItem.LoadItemInfo.BaseMethodInfo;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.Picture;
import HookItem.note.ApiMark;
import HookItem.note.Versioning;
import HookItem.note.XPOperate;
import lin.xposed.BuildConfig;
import lin.xposed.FloatingWindows.HookAddFloatingWindows;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HookUtils.XPBridge;
import lin.xposed.QQUtils.API.Builder_Pic;
import lin.xposed.QQUtils.API.MsgApi_GetMessageByTimeSeq;
import lin.xposed.QQUtils.API.MsgApi_revokeMsg;
import lin.xposed.QQUtils.API.MsgApi_sendReply;
import lin.xposed.QQUtils.GroupNotifications;
import lin.xposed.Utils.LogUtils;
import lin.xposed.main.QQHook.CloseHook.CloseLogWriteThread;
import lin.xposed.main.QQHook.CloseHook.SpaceAdvertising;
import lin.xposed.main.QQHook.*;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class HookItemMainInfo {

    public static final Map<Class<?>, XPItemInfo> itemInstance = new LinkedHashMap<>();

    public static List<Class<?>> allClzList = new ArrayList<>();

    static {
        allClzList.add(Picture.class);
        allClzList.add(TestItemHook.class);
        allClzList.add(Builder_Pic.class);
        allClzList.add(AddVoiceMenu.class);
        allClzList.add(GroupNotifications.class);
//        allClzList.add(Redirect.class);
        allClzList.add(HookAddFloatingWindows.class);
        allClzList.add(CancelCamera.class);
        allClzList.add(PictureDisplaysText.class);
        allClzList.add(onMessageInit.class);
//        allClzList.add(VoiceRedPacketFlag.class);
        allClzList.add(DisableMusicCard.class);
        allClzList.add(BlockIPCards.class);
        allClzList.add(MainRepetitionIcon.class);
        allClzList.add(CloseLogWriteThread.class);
        allClzList.add(SpaceAdvertising.class);
        allClzList.add(SidebarViewReduced.class);
        allClzList.add(FriendIdentification.class);
        allClzList.add(DeviceTypePad.class);
        allClzList.add(DownloadUpdate.class);
        allClzList.add(AtMsg.class);
        allClzList.add(DPI_SET.class);
        allClzList.add(AtAllMsg.class);
        allClzList.add(ChatPicDisplaySendUin.class);
        allClzList.add(QQSettingMeView_X.class);
        allClzList.add(HookOnVoice.class);
        allClzList.add(HideChatTitleHandsetIcon.class);
        allClzList.add(GetGameFriends.class);
        //API
        allClzList.add(ChatItemInit.class);
        allClzList.add(MsgApi_sendReply.class);
        allClzList.add(MsgApi_revokeMsg.class);
        allClzList.add(MsgApi_GetMessageByTimeSeq.class);
        allClzList.add(MiniProgram.class);
        allClzList.add(MsgApi_sendReply.class);

//        allClzList = ClassUtils.getAllClass(HookItemMainInfo.class);
        //new所有类
        for (Class<?> clz : allClzList) {
            Method[] methods = clz.getDeclaredMethods();
            boolean isHook = false;
            for (Method m : methods) {
                if (m.getParameterTypes().length == 1 &&
                        m.getParameterTypes()[0].equals(MethodContainer.class)) {
                    isHook = true;
                } else if (m.getReturnType().equals(UIInfo.class)) {
                    isHook = true;
                } else if (m.getReturnType().equals(HookAction.class)) {
                    isHook = true;
                }
            }
            if (!isHook) continue;

            XPItemInfo itemInfo = new XPItemInfo();
            try {
                itemInfo.instance = clz.newInstance();
                itemInfo.id = clz.getName();
                itemInstance.put(clz, itemInfo);
            } catch (Exception e) {
                LogUtils.addError(e);
            }
            try {
                //修改对象里的父容器信息
                for (Field f : clz.getDeclaredFields()) {
                    if (f.getType().equals(XPItemInfo.class)) {
                        f.setAccessible(true);
                        f.set(itemInstance.get(clz).instance, itemInfo);
                    }
                }

            } catch (Exception e) {
                CommonTool.Toast(" f.setItem " + e);
            }
        }


    }


    public static void onBefore() {

        //添加方法--待完善
        for (Map.Entry<Class<?>, XPItemInfo> entry : itemInstance.entrySet()) {
            Class<?> clz = entry.getKey();
            XPItemInfo itemInfo = entry.getValue();
            for (Method m : clz.getDeclaredMethods()) {
                Versioning versioning = m.getAnnotation(Versioning.class);
                itemInfo.methods.add(m);
            }
        }
        //扫描所有方法信息
        for (XPItemInfo info : itemInstance.values()) {
            for (Method method : info.methods) {
                //是查找方法的方法
                if (method.getParameterCount() == 1 &&
                        method.getParameterTypes()[0] == MethodContainer.class) {
                    MethodContainer methodContainer = new MethodContainer();
                    try {
                        method.invoke(info.instance, methodContainer);
                        for (BaseMethodInfo baseInfo : methodContainer.getMethodList()) {
                            baseInfo.bandToInfo = info;
                            info.NeedMethodInfo.put(baseInfo.HookID, baseInfo);
                        }
                    } catch (Exception e) {
//                        LogUtils.addRunLog(e);
                    }
                }
                //UI方法
                if (method.getReturnType().equals(UIInfo.class)) {
                    try {
                        info.ui = (UIInfo) method.invoke(info.instance);
                        info.isLoadEarly = info.ui.period == UIInfo.PERIOD;
                    } catch (Exception e) {
                        LogUtils.addRunLog(e);
                    }
                }
                ApiMark apiMark = method.getAnnotation(ApiMark.class);
                if (apiMark != null) {
                    info.ApiMethod = method;
                }
            }

        }
        //判断可用性和加载之前查找到的方法
        if (MethodScannerWorks.IsAvailable()) {
            startHook(true);
        }
    }

    static AtomicBoolean isStartFindMethod = new AtomicBoolean();
    public static void onAfter() {
        if (isStartFindMethod.getAndSet(true)) {
            CommonTool.Toast("正在查找 请等待");
            return;
        }
        if (MethodScannerWorks.IsAvailable()) {
            startHook(false);
        } else {
            MethodScannerWorks.initStartFindMethod();
        }
    }

    private static void startHook(boolean delay) {
        for (XPItemInfo info : itemInstance.values()) {
            for (Method m : info.methods) {
                //不延迟
                if (info.isLoadEarly == delay) {
                    XPOperate xpOperate = m.getAnnotation(XPOperate.class);
                    // 标记与开关
                    if (xpOperate != null) {
                        try {
                            Method member = (Method) info.scanResult.get(xpOperate.ID());
                            if (member == null) {
                                info.yesMethod = false;
                                if (BuildConfig.DEBUG)
                                    LogUtils.addError("hook method null ->" + info.id + "." + xpOperate.ID());
                                continue;
                            }
                            HookAction action = (HookAction) m.invoke(info.instance);
                            //挂钩前后
                            if (xpOperate.period() == XPOperate.After) {
                                XPBridge.hookAfter(member, param -> {
                                    //开关放在方法内植入 实时判断hook
                                    if (info.Enabled && info.NeedMethodInfo.get(xpOperate.ID()).TheHookOpens) {
                                        try {
                                            action.Action(param);
                                        } catch (Throwable e) {
                                            LogUtils.addError(e);
                                        }
                                        //如果没有ui
                                    } else if (info.ui == null) {
                                        try {
                                            action.Action(param);
                                        } catch (Throwable e) {
                                            LogUtils.addError(e);
                                        }
                                    }
                                }, xpOperate.hook_period());
                            } else {
                                XPBridge.hookBefore(member, param -> {
                                    if (info.Enabled && info.NeedMethodInfo.get(xpOperate.ID()).TheHookOpens) {
                                        try {
                                            action.Action(param);
                                        } catch (Throwable e) {
                                            LogUtils.addRunLog(e);
                                        }
                                    } else if (info.ui == null) {
                                        try {
                                            action.Action(param);
                                        } catch (Throwable e) {
                                            LogUtils.addError(e);
                                        }
                                    }
                                }, xpOperate.hook_period());
                            }
                        } catch (Exception e) {
                            LogUtils.addError("Hook", e);
                        }
                    }
                }
            }
        }
    }

    //包裹一个功能的类 包含了
    public static class XPItemInfo {
        public boolean yesMethod = true;
        public String id;//Item Class.getName
        public Method ApiMethod;
        public boolean Enabled;//开关状态
        public boolean isLoadEarly;//加载时期
        public UIInfo ui;//ui信息
        public Object instance;//item对象
        public HashMap<Method, HookAction> baseAndHook = new LinkedHashMap<>();//被hook的方法,对应的hook方法对象
        public ArrayList<Method> methods = new ArrayList<>();
        //HashMap<Member, HookAction> hookActionLis = new HashMap<>();//查找到的hook方法,自身的hook方法
        public HashMap<String, BaseMethodInfo> NeedMethodInfo = new LinkedHashMap<>();//hookID,baseInfo对象
        public HashMap<String, Member> scanResult = new LinkedHashMap<>();//id 反射到的hook方法
    }

}
