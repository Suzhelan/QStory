package HookItem.ChatHook.RecallMessage;

import ConfigTool.ListConfig;
import ConfigTool.SimpleConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.LoadItemInfo.Template.ItemAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ScreenParamUtils;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.QQUtils.API.FinalApiBuilder;
import lin.xposed.QQUtils.API.MsgApi_revokeMsg;
import lin.xposed.QQUtils.Common;
import lin.xposed.QQUtils.MessageUtils;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestItemHook extends ItemAction {

    private static final String TAG = "RevokeMsg";
    boolean retainMyMessage = new SimpleConfig(TAG).getBoolean("isRetainMyMessage");

    @Override
    public UIInfo getBaseInfo() {
        if (this.UIInfo != null) return this.UIInfo;
        HookItem.loadHook.UIInfo UIInfo = new UIInfo();
        UIInfo.name = "防撤回";
        UIInfo.info = "点击可开关是否保留自己撤回的消息";
        UIInfo.groupType = ClassifyMenu.base;
        UIInfo.onClick = v -> show();
        return UIInfo;
    }

    public void show() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        RelativeLayout item = (RelativeLayout) inflater.inflate(R.layout.main_item_layout, null);
        TextView textView = item.findViewById(R.id.item_name);
        textView.setText("撤回时保留自己的消息");
        Switch sw = item.findViewById(R.id.item_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleConfig simpleConfig = new SimpleConfig(TAG);
                simpleConfig.putBoolean("isRetainMyMessage", isChecked);
            }
        });
        sw.setChecked(retainMyMessage);
        item.removeView(item.findViewById(R.id.item_info));
        dialog.setOnDismissListener(dialog1 -> {
            retainMyMessage = new SimpleConfig(TAG).getBoolean("isRetainMyMessage");
            MenuLayout.isShow = false;
            MenuLayout.newDialog(MenuLayout.thisGroupName);
        });
        dialog.setContentView(item);
        dialog.setDialogWindowAttr(0.7, 0.05);
        dialog.show();
        MenuLayout.dialog.dismiss();
    }

    @Override
    public void findMethod(MethodContainer container) {

        container.addMethod("hook_1", MethodUtils.findMethod("com.tencent.imcore.message.QQMessageFacade", null, void.class, new Class[]{
                ArrayList.class, boolean.class
        }));

        container.addMethod("hook_3", MethodUtils.findMethod("com.tencent.imcore.message.BaseMessageManager", null, void.class, new Class[]{
                ArrayList.class
        }));
        Common.AIOMessageListAdapter_getView(container);
        container.addMethod(MethodFindBuilder.newFindMethodByName("hook_4_before", "tips_exp", m -> m.getDeclaringClass().equals(ClassUtils.getClass("com.tencent.mobileqq.activity.aio.helper.AIORevokeMsgHelper"))));
        container.addMethod(MethodFindBuilder.newFinderWhichMethodInvokingLinked("hook_4", "hook_4_before", m -> ((Method) m).getParameterCount() == 1 && m.getDeclaringClass().equals(ClassUtils.getClass("com.tencent.mobileqq.activity.aio.helper.AIORevokeMsgHelper"))));

    }

    @XPOperate(ID = "onAIOGetView", period = XPOperate.After)
    public HookAction hook_2() {
        return param -> {
            Object mGetView = param.getResult();
            RelativeLayout mLayout;
            if (mGetView instanceof RelativeLayout) mLayout = (RelativeLayout) mGetView;
            else return;
            List<Object> MessageRecoreList = FieIdUtils.getFirstField(param.thisObject, List.class);
            if (MessageRecoreList == null) return;
            Object ChatMsg = MessageRecoreList.get((int) param.args[0]);


            String ExtStr = FieIdUtils.getField(ChatMsg, "extStr", String.class);
            if (TextUtils.isEmpty(ExtStr)) return;
            @SuppressLint("ResourceType")
            View view = mLayout.findViewById(753953);
            List<String> list = ListConfig.getList("RevokeMsgList");
            long msq = FieIdUtils.getField(ChatMsg, "shmsgseq", long.class);
            long time = FieIdUtils.getField(ChatMsg, "time", long.class);
            if (list.contains(msq + ":" + time)) {
                if (view == null) {
                    Context context = mLayout.getContext();
                    CommonTool.InjectResourcesToContext(context);
                    view = getRevokeView(mLayout);
                }
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                }
            } else if (view != null && view.getVisibility() != View.GONE) {
                view.setVisibility(View.GONE);
            }
        };
    }

    @SuppressLint("ResourceType")
    private View getRevokeView(RelativeLayout rootView) {
        Context context = rootView.getContext();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = ScreenParamUtils.dpToPx(context, 18);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        BasicBackground background = new BasicBackground();
        background.setColor(CommonTool.getColors(context, R.color.银鼠));
        background.setAlpha(0.7);
        background.setStroke(0, Color.BLACK);
        background.setPadding(20, 15, 20, 15);
        TextView textView = new TextView(context);
        textView.setText("该消息已被撤回");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(15);
        textView.setTextColor(CommonTool.getColors(context, R.color.生成色));
        textView.setBackground(background);
        textView.setId(753953);
        rootView.addView(textView, params);
        return textView;
    }

    @XPOperate(ID = "hook_1")
    @Override
    public HookAction getAction() {
        return param -> {
            ArrayList<Object> msgList = (ArrayList<Object>) param.args[0];
            if (msgList == null || msgList.isEmpty()) return;
            String GroupUin = (String) Table_RevokeInfo_Field.GroupUin().get(msgList.get(0));
            String OpUin = (String) Table_RevokeInfo_Field.OpUin().get(msgList.get(0));
            int istroop = (int) Table_RevokeInfo_Field.IsTroop().get(msgList.get(0));
            long shmsgseq = (long) Table_RevokeInfo_Field.shmsgseq().get(msgList.get(0));
            String FriendUin;
            if (istroop == 1) {
                FriendUin = GroupUin;
            } else if (istroop == 0) {
                if (OpUin.equals(QQEnvUtils.getCurrentUin())) {
                    FriendUin = GroupUin;
                } else {
                    FriendUin = OpUin;
                }
            } else {
                if (OpUin.equals(QQEnvUtils.getCurrentUin())) {
                    FriendUin = GroupUin;
                } else {
                    FriendUin = OpUin;
                }
            }

            Object RawMsg = MessageUtils.FindMessageByTime(FriendUin, istroop, shmsgseq);
            if (RawMsg != null) {
                long msq = FieIdUtils.getField(RawMsg, "shmsgseq", long.class);
                long time = FieIdUtils.getField(RawMsg, "time", long.class);
                List<String> list = ListConfig.getList("RevokeMsgList");
                if (!list.contains(msq + ":" + time + ":")) {
                    list.add(msq + ":" + time);
                }
                if (list.size() > 1000) list.remove(0);
                ListConfig.setListToFile("RevokeMsgList", list);
            }
            param.setResult(null);
        };
    }

    @XPOperate(ID = "hook_3")
    public HookAction getAction2() {
        return param -> {
            if (!retainMyMessage) return;
            ArrayList msgList = (ArrayList) param.args[0];
            if (msgList == null || msgList.isEmpty()) return;
            String GroupUin = (String) Table_RevokeInfo_Field.GroupUin().get(msgList.get(0));
            String OpUin = (String) Table_RevokeInfo_Field.OpUin().get(msgList.get(0));
            String sender = (String) Table_RevokeInfo_Field.Sender().get(msgList.get(0));
            int istroop = (int) Table_RevokeInfo_Field.IsTroop().get(msgList.get(0));
            long shmsgseq = (long) Table_RevokeInfo_Field.shmsgseq().get(msgList.get(0));
            String FriendUin;
            if (istroop == 1 || istroop == 0) {
                FriendUin = GroupUin;
            } else {
                FriendUin = sender;
            }
            Object mRawmsg = MessageUtils.FindMessageByTime(FriendUin, istroop, shmsgseq);

            if (mRawmsg != null) {
                if (OpUin.equals(QQEnvUtils.getCurrentUin())) {
                    if (istroop == 1 || (istroop == 0 && !mRawmsg.getClass().getName().contains("MessageForTroopFile"))
                            || (istroop == 1000 && !mRawmsg.getClass().getName().contains("MessageForTroopFile"))) {
                        param.setResult(null);
                    }
                }
            }
        };
    }

    @XPOperate(ID = "hook_4")
    public HookAction hook_4() {
        return param -> {
            param.setResult(null);
            FinalApiBuilder.builder(MsgApi_revokeMsg.class, param.args[0]);
        };
    }

    public static class Table_RevokeInfo_Field {
        public static Class RevokeMsgInfo() {
            return ClassUtils.getClass("com.tencent.mobileqq.revokemsg.RevokeMsgInfo");
        }

        public static Field GroupUin() {
            Field f = FieIdUtils.findField(RevokeMsgInfo(), "g", String.class);
            if (f != null) f.setAccessible(true);
            return f;
        }

        public static Field OpUin() {
            Field f = FieIdUtils.findField(RevokeMsgInfo(), "h", String.class);
            if (f != null) f.setAccessible(true);
            return f;
        }

        public static Field Sender() {
            Field f = FieIdUtils.findField(RevokeMsgInfo(), "n", String.class);
            if (f != null) f.setAccessible(true);
            return f;
        }

        public static Field IsTroop() {
            Field f = FieIdUtils.findField(RevokeMsgInfo(), "e", int.class);
            if (f != null) f.setAccessible(true);
            return f;
        }

        public static Field shmsgseq() {
            Field f = FieIdUtils.findField(RevokeMsgInfo(), "f", long.class);
            if (f != null) f.setAccessible(true);
            return f;
        }
    }
}
