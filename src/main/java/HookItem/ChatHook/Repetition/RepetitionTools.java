package HookItem.ChatHook.Repetition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.ScreenParamUtils;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.QQUtils.SendUtils;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.ConstructorUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RepetitionTools {

    private static final String TAG = "复读";
    private static final HashMap<String, String> supportMessageTypes = new HashMap<>();
    private static final HashMap<String, RepeatAction> msgTypeAndActionList = new HashMap<>();
    private static long double_click_time = 0;

    static {
        supportMessageTypes.put("MessageForPic", "RelativeLayout");
        supportMessageTypes.put("MessageForText", "ETTextView");
        supportMessageTypes.put("MessageForLongTextMsg", "ETTextView");
        supportMessageTypes.put("MessageForFoldMsg", "ETTextView");
        supportMessageTypes.put("MessageForPtt", "BreathAnimationLayout");
//        supportMessageTypes.put("MessageForMixedMsg", "MixedMsgLinearLayout");
        supportMessageTypes.put("MessageForReplyText", "SelectableLinearLayout");
//        supportMessageTypes.put("MessageForScribble", "RelativeLayout");
//        supportMessageTypes.put("MessageForMarketFace", "RelativeLayout");
//        supportMessageTypes.put("MessageForArkApp", "ArkAppRootLayout");
//        supportMessageTypes.put("MessageForStructing", "RelativeLayout");
//        supportMessageTypes.put("MessageForTroopEffectPic", "RelativeLayout");
//        supportMessageTypes.put("MessageForAniSticker", "FrameLayout");
//        supportMessageTypes.put("MessageForArkFlashChat", "ArkAppRootLayout");
//        supportMessageTypes.put("MessageForShortVideo", "RelativeLayout");
//        supportMessageTypes.put("MessageForPokeEmo", "RelativeLayout");
    }

    static {
        RepeatAction text = (session, chatMsg) -> {
            try {
                sendText(session, chatMsg);
            } catch (Exception e) {
                LogUtils.addRunLog(TAG, e);
            }
        };
        msgTypeAndActionList.put("MessageForFoldMsg", text);
        msgTypeAndActionList.put("MessageForLongTextMsg", text);
        msgTypeAndActionList.put("MessageForText", text);
        msgTypeAndActionList.put("MessageForPic", SendUtils::sendPic);
        msgTypeAndActionList.put("MessageForPtt", null);
        msgTypeAndActionList.put("MessageForReplyText", null);
    }

    public static boolean sendText(Object session, Object chatMsg) throws Exception {
        ArrayList atList = FieIdUtils.getField(chatMsg, "atInfoTempList", ArrayList.class);
        ArrayList atList2 = FieIdUtils.getField(chatMsg, "atInfoList", ArrayList.class);
        String str = FieIdUtils.getField(chatMsg, "extStr", String.class);
        JSONObject json = new JSONObject(str);
        str = json.optString("troop_at_info_list");
        ArrayList atList3 = MethodUtils.callStaticMethod(ClassUtils.getClass("com.tencent.mobileqq.data.MessageForText"), "getTroopMemberInfoFromExtrJson", ArrayList.class, new Class[0]);
        if (atList == null) atList = atList2;
        if (atList == null) atList = atList3;
        String text = FieIdUtils.getField(chatMsg, "msg", String.class);
        ArrayList newAtList = new ArrayList<>();
        try {
            JSONArray newArray = new JSONArray(str);
            for (int i = 0; i < newArray.length(); i++) {
                JSONObject item = newArray.getJSONObject(i);
                newAtList.add(buildAtInfo(String.valueOf(item.getLong("uin")), new String(new char[item.getInt("textLen")]), (short) item.getInt("startPos"), item.optLong("channelId")));
            }
        } catch (Exception e) {
            return false;
        }
        SendUtils.sendTextMsg(session, text, newAtList);
        return true;
    }

    private static boolean sendContent(Object session, Object chatMsg) throws Exception {

        String name = chatMsg.getClass().getSimpleName();
        switch (name) {
            case "MessageForText":
            case "MessageForLongTextMsg":
            case "MessageForFoldMsg":
                ArrayList atList = FieIdUtils.getField(chatMsg, "atInfoTempList", ArrayList.class);
                ArrayList atList2 = FieIdUtils.getField(chatMsg, "atInfoList", ArrayList.class);
                String str = FieIdUtils.getField(chatMsg, "extStr", String.class);
                JSONObject json = new JSONObject(str);
                str = json.optString("troop_at_info_list");
                ArrayList atList3 = MethodUtils.callStaticMethod(ClassUtils.getClass("com.tencent.mobileqq.data.MessageForText"), "getTroopMemberInfoFromExtrJson", ArrayList.class, new Class[]{String.class}, str);
                if (atList == null) atList = atList2;
                if (atList == null) atList = atList3;
                String text = FieIdUtils.getField(chatMsg, "msg", String.class);
                ArrayList newAtList = new ArrayList();
                try {
                    JSONArray newArray = new JSONArray(str);
                    for (int i = 0; i < newArray.length(); i++) {
                        JSONObject item = newArray.getJSONObject(i);
                        newAtList.add(buildAtInfo(String.valueOf(item.getLong("uin")), new String(new char[item.getInt("textLen")]), (short) item.getInt("startPos"), item.optLong("channelId")));
                    }
                } catch (Exception e) {

                }
                SendUtils.sendTextMsg(session, text, newAtList);
                return true;
            case "MessageForPic":
                SendUtils.sendPic(session, chatMsg);
                return true;
            case "MessageForPtt":
                String pptPath = MethodUtils.callMethod(chatMsg, "getLocalFilePath", String.class);
                SendUtils.sendVoice(session, pptPath);
                return true;
            case "MessageForReplyText":
                SendUtils.sendReplyMsg(session, chatMsg);
                return true;
            default:
                return false;
        }
    }

    public static Object buildAtInfo(String Useruin, String AtText, short StartPos, long ChannelID) {
        try {
            Object AtInfoObj = ConstructorUtils.newInstance(ClassUtils.getClass("com.tencent.mobileqq.data.AtTroopMemberInfo"));
            if (Useruin.isEmpty()) return null;
            if (Useruin.equals("0")) {
                FieIdUtils.setField(AtInfoObj, "flag", byte.class, (byte) 1);
                FieIdUtils.setField(AtInfoObj, "startPos", StartPos);
                FieIdUtils.setField(AtInfoObj, "textLen", (short) AtText.length());
            } else {
                if (ChannelID != 0) {
                    FieIdUtils.setField(AtInfoObj, "flag", byte.class, (byte) 0);
                    FieIdUtils.setField(AtInfoObj, "isResvAttr", boolean.class, true);
                }
                FieIdUtils.setField(AtInfoObj, "uin", Long.parseLong(Useruin));
                FieIdUtils.setField(AtInfoObj, "startPos", StartPos);
                FieIdUtils.setField(AtInfoObj, "textLen", (short) AtText.length());
            }
            return AtInfoObj;
        } catch (Exception e) {
            LogUtils.addRunLog(TAG, e);
            return null;
        }
    }

    @SuppressLint("ResourceType")
    public static void createIcon(RelativeLayout messageLayout, Object message) throws Exception {
        boolean isSendFromLocal;

        int istroop = FieIdUtils.getField(message, "istroop", int.class);
        if (istroop == 1 || istroop == 0) {
            String UserUin = FieIdUtils.getField(message, "senderuin", String.class);
            isSendFromLocal = UserUin.equals(QQEnvUtils.getCurrentUin());
        } else {
            isSendFromLocal = MethodUtils.callNoParamsMethod(message, "isSendFromLocal", boolean.class);
        }
        Context context = messageLayout.getContext();
        CommonTool.InjectResourcesToContext(context);
        String msgName = message.getClass().getSimpleName();
        if (msgTypeAndActionList.containsKey(msgName)) {
            ImageButton imageButton = messageLayout.findViewById(258787);
            if (imageButton == null) {
                imageButton = new ImageButton(context);
                imageButton.setImageDrawable(MainRepetitionIcon.icon);
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                imageButton.setAdjustViewBounds(true);
                imageButton.getBackground().setAlpha(0);
                imageButton.setMaxHeight(ScreenParamUtils.dpToPx(context, MainRepetitionIcon.iconSize));
                imageButton.setMaxWidth(ScreenParamUtils.dpToPx(context, MainRepetitionIcon.iconSize));
                imageButton.setId(258787);
                imageButton.setTag(message);

                imageButton.setOnClickListener(v -> {
                    if (MainRepetitionIcon.enableDoubleClicking) {
                        if (System.currentTimeMillis() - double_click_time > 300) {
                            double_click_time = System.currentTimeMillis();
                            return;
                        }
                    }
                    try {
                        sendContent(HostEnv.SessionInfo, v.getTag());
                    } catch (Exception e) {
                        CommonTool.Toast("复读错误");
                        LogUtils.addRunLog(TAG, e);
                    }
                });
                imageButton.setOnLongClickListener(v -> {
//                    ModifyMessage.show(v.getTag());
                    return true;
                });
                messageLayout.addView(imageButton, param);
            } else {
                if (imageButton.getVisibility() != View.VISIBLE)
                    imageButton.setVisibility(View.VISIBLE);
                imageButton.setTag(message);
            }

            //属性
            RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) imageButton.getLayoutParams();
            String attachName = supportMessageTypes.get(msgName);
            //子消息控件
            View attachView = findView(attachName, messageLayout);
            if (attachView != null) {
                final ImageButton imageButton1 = imageButton;
                imageButton1.post(() -> {
                    int top = attachView.getTop();
                    top += attachView.getHeight() / 2 - ScreenParamUtils.dpToPx(context, MainRepetitionIcon.VerticalOffsetValue);
                    //自己发送的消息

                    param.removeRule(RelativeLayout.ALIGN_LEFT);
                    param.removeRule(RelativeLayout.ALIGN_RIGHT);
                    param.removeRule(RelativeLayout.ALIGN_TOP);

                    if (isSendFromLocal) {
                        param.addRule(RelativeLayout.ALIGN_LEFT, attachView.getId());
                        if (MainRepetitionIcon.iconLocatedUpperRightCorner) {
                            param.addRule(RelativeLayout.ALIGN_TOP, attachView.getId());
                            param.leftMargin = ScreenParamUtils.dpToPx(context, -5);
                            param.topMargin = ScreenParamUtils.dpToPx(context, -5);
                        } else {
                            param.topMargin = top;
                            param.leftMargin = ScreenParamUtils.dpToPx(context, MainRepetitionIcon.HorizontalOffsetValue);
                        }
                    } else {
                        param.addRule(RelativeLayout.ALIGN_RIGHT, attachView.getId());
                        if (MainRepetitionIcon.iconLocatedUpperRightCorner) {
                            param.addRule(RelativeLayout.ALIGN_TOP, attachView.getId());
                            param.rightMargin = ScreenParamUtils.dpToPx(context, -5);
                            param.topMargin = ScreenParamUtils.dpToPx(context, -5);
                        } else {
                            param.topMargin = top;
                            param.rightMargin = ScreenParamUtils.dpToPx(context, MainRepetitionIcon.HorizontalOffsetValue);
                        }
                    }
                    imageButton1.setLayoutParams(param);
                });
            }
        }
    }

    public static View findView(String Name, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (vg.getChildAt(i).getClass().getSimpleName().contains(Name)) {
                return vg.getChildAt(i);
            }
        }
        return null;
    }

    private interface RepeatAction {
        void Repeat(Object session, Object chatMsg);
    }
}
