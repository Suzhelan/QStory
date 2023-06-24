package HookItem.ChatHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.view.View;
import android.widget.RelativeLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.QQUtils.Common;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class DisableMusicCard {
    private final HashMap<Object, String> cacheUrlList = new HashMap<>();
    private final HashMap<Object, Boolean> isPlay = new HashMap<>();

    public UIInfo getBaseInfo() {
        HookItem.loadHook.UIInfo UIInfo = new UIInfo();
        UIInfo.name = "拦截音乐卡片";
        UIInfo.info = "防止音乐卡片自动播放社死";
        UIInfo.groupType = "净化";
        return UIInfo;
    }

    public void findMethod(MethodContainer methodContainer) {
        methodContainer.addMethod("hook", MethodUtils.findNoParamsMethod("com.tencent.mobileqq.data.MessageForArkApp", "doParse", void.class));
        methodContainer.addMethod("hook_1", MethodUtils.findNoParamsMethod("com.tencent.mobileqq.data.ArkAppMessage", "toAppXml", String.class));
        Common.AIOMessageListAdapter_getView(methodContainer);
    }

    @XPOperate(ID = "hook_1", period = XPOperate.After)
    public HookAction hook_1() {
        return param -> {
            Object ark = param.thisObject;
            if (cacheUrlList.containsKey(ark)) {
                JSONObject jsonCard = new JSONObject((String) param.getResult());
                JSONObject meta = jsonCard.getJSONObject("meta");
                JSONObject shareData = meta.getJSONObject("shareData");
                shareData.put("url", cacheUrlList.get(ark));
                meta.put("shareData", shareData);
                jsonCard.put("meta", meta);
                param.setResult(jsonCard.toString());
            }
        };
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction setUrl() {
        return param -> {
            Object message = param.thisObject;
            Object ark = FieIdUtils.getFirstField(message, ClassUtils.getClass("com.tencent.mobileqq.data.ArkAppMessage"));
            if (cacheUrlList.containsKey(ark)) {
                return;
            }
            String name = FieIdUtils.getField(ark, "appName", String.class);
            if (name == null) return;
            if (name.equals("com.tencent.gamecenter.gameshare")) {
                String str = FieIdUtils.getField(ark, "metaList", String.class);
                if (str == null) return;
                JSONObject jsonCard = new JSONObject(str);
                JSONObject metaList = new JSONObject(jsonCard.get("shareData").toString());
                cacheUrlList.put(ark, (String) metaList.get("url"));
                metaList.put("url", "");
                jsonCard.put("shareData", metaList);
                FieIdUtils.setField(ark, "metaList", jsonCard.toString());
            }
        };
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
                    CharSequence text = "音乐卡片已拦截 点击可恢复";
                    if (isPlay.containsKey(ark)) {
                        text = "点击可重新拦截";
                    }
                    MethodUtils.callMethod(mLayout, "setTailMessage", void.class, new Class[]{boolean.class, CharSequence.class, ClassUtils.getClass("android.view.View$OnClickListener")}, true, text, (View.OnClickListener) v -> {
                        try {
                            String str = FieIdUtils.getField(ark, "metaList", String.class);
                            if (str == null) return;
                            JSONObject jsonCard = new JSONObject(str);
                            JSONObject metaList = new JSONObject(jsonCard.get("shareData").toString());
                            if (!isPlay.containsKey(ark)) {
                                metaList.put("url", cacheUrlList.get(ark));
                                jsonCard.put("shareData", metaList);
                                FieIdUtils.setField(ark, "metaList", jsonCard.toString());
                                CommonTool.Toast("播放信息已恢复 重新进入此界面可播放");
                                isPlay.put(ark, true);
                            } else {
                                metaList.put("url", "");
                                jsonCard.put("shareData", metaList);
                                FieIdUtils.setField(ark, "metaList", jsonCard.toString());
                                CommonTool.Toast("播放信息已重新拦截");
                                isPlay.remove(ark);
                            }
                        } catch (Exception e) {
                            CommonTool.Toast(e);
                        }
                    });
                }
            }
        };
    }
}
