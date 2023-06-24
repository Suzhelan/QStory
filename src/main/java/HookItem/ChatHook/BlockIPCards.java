package HookItem.ChatHook;

import ConfigTool.GlobalConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.widget.RelativeLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.QQUtils.Common;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BlockIPCards {
    private final HashMap<Object, String> cacheUrlList = new HashMap<>();
    private final String TAG = "isRecordingIPCard";
    private String appXML;
    private String log;
    private boolean isRecording = false;

    public UIInfo getBaseInfo() {
        HookItem.loadHook.UIInfo UIInfo = new UIInfo();
        UIInfo.name = "拦截探IP卡片";
        UIInfo.info = "点击可开关是否在本地记录";
        UIInfo.groupType = ClassifyMenu.mainClassify[2];
        UIInfo.onClick = v -> {
            if (GlobalConfig.getBoolean(TAG)) {
                GlobalConfig.putBoolean(TAG, false);
                CommonTool.Toast("已关闭记录拦截到的ip卡片");
            } else {
                GlobalConfig.putBoolean(TAG, true);
                CommonTool.Toast("已开启记录拦截的ip卡片 可在模块日志目录下的Run logs文件查看");
            }
            isRecording = GlobalConfig.getBoolean(TAG);
        };
        isRecording = GlobalConfig.getBoolean(TAG);
        return UIInfo;
    }

    public void findMethod(MethodContainer methodContainer) {
        methodContainer.addMethod("hook", MethodUtils.findNoParamsMethod("com.tencent.mobileqq.data.MessageForArkApp", "doParse", void.class));
        Common.AIOMessageListAdapter_getView(methodContainer);
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction setUrl() {
        return param -> {
            Object message = param.thisObject;
            Object ark = FieIdUtils.getFirstField(message, ClassUtils.getClass("com.tencent.mobileqq.data.ArkAppMessage"));
            if (cacheUrlList.containsKey(ark)) {
                return;
            }
            String str = FieIdUtils.getField(ark, "metaList", String.class);
            if (str == null) return;
            JSONObject jsonCard = new JSONObject(str);
            if (!parseJson(jsonCard)) {
                cacheUrlList.put(ark, "");
                appXML = MethodUtils.callNoParamsMethod(ark, "toAppXml", String.class);
                if (isRecording) {
                    //解析发送者
                    String info = "";
                    if ((int) FieIdUtils.getField(message, "istroop", int.class) == 1) {
                        info += "来自群聊 : " + FieIdUtils.getField(message, "frienduin", String.class);
                    }
                    info += " 发送者 : " + FieIdUtils.getField(message, "senderuin", String.class);
                    LogUtils.addRunLog("IP卡片已拦截 json信息 : " + appXML
                            + "\n" + log
                            + "\n" + info);
                }
                FieIdUtils.setField(ark, "metaList", jsonCard.toString());
            }
        };
    }

    private boolean parseJson(JSONObject jsonObject) {
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            try {
                String key = iterator.next();
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    //如果有JSONObject继续向下解析
                    return parseJson((JSONObject) value);
                } else if (value instanceof String) {
                    String urlStr = (String) value;
                    if (urlStr.startsWith("http")) {
                        URL url = new URL(urlStr);
                        if (url.getHost().matches(".*(qpic|gtimg|qlogo)\\.cn$") || url.getHost().matches(".*(QQ||qq)\\.com$")) {

                        } else {
                            log = "探IP卡片已尝试拦截 拦截依据 : " + url;
                            jsonObject.put(key, "");
                            return false;
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        return true;
    }

    @XPOperate(ID = "onAIOGetView", period = XPOperate.After)
    public HookAction getView() {
        return param -> {
            Object mGroupView = param.getResult();
            RelativeLayout mLayout;
            //获取消息布局
            if (mGroupView instanceof RelativeLayout) mLayout = (RelativeLayout) mGroupView;
            else return;
            //获取此对象下的消息列表
            List<Object> message = FieIdUtils.getFirstField(param.thisObject, List.class);
            if (message == null) {
                return;
            }
            //获取具体的消息对象
            Object chatMsg = message.get((int) param.args[0]);
            //json卡片消息
            if (chatMsg.getClass().getName().equalsIgnoreCase("com.tencent.mobileqq.data.MessageForArkApp")) {
                Object ark = FieIdUtils.getFirstField(chatMsg, ClassUtils.getClass("com.tencent.mobileqq.data.ArkAppMessage"));
                if (cacheUrlList.containsKey(ark)) {
                    CharSequence text = "该卡片可能是探ip卡片 已拦截";
                    if (isRecording) text += " 可在日志/Run logs查看";
                    MethodUtils.callMethod(mLayout, "setTailMessage", void.class, new Class[]{boolean.class, CharSequence.class, ClassUtils.getClass("android.view.View$OnClickListener")}, true, text, null);
                }
            }
        };
    }

}
