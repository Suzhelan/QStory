package HookItem.ChatHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ScreenParamUtils;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ChatPicDisplaySendUin {
    private Bundle bundle;

    private String fieldName = "";

    public static String toHexEncoding(int color) {
        String R, G, B;
        StringBuilder sb = new StringBuilder();
        R = Integer.toHexString(Color.red(color));
        G = Integer.toHexString(Color.green(color));
        B = Integer.toHexString(Color.blue(color));
        //判断获取到的R,G,B值的长度 如果长度等于1 给R,G,B值的前边添0
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append("0x");
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    @Nullable
    private static Method getResume(Class<?> AIOGalleryActivityClass) {
        return MethodUtils.findMethod(AIOGalleryActivityClass, "onResume", void.class, new Class[0]);
    }

    private static boolean checkQQ(String qq) {
        if (TextUtils.isEmpty(qq)) {
            return false;
        }
        int length = qq.length();
        if (length < 5 || length > 10) {
            return false;
        }
        if (qq.startsWith("0")) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char a = qq.charAt(i);
            if (a < '0' || a > '9') {
                return false;
            }
        }
        return true;
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "聊天界面查看图片显示发送者";
        ui.groupType = "功能";
        return ui;
    }

    public void getMethod(MethodContainer container) {

        container.addMethod(MethodFindBuilder.newFindMethodByName("hook_3", "enterGallery error, invalidate session info!", m -> true));

        Class<?> AIOGalleryActivityClass = ClassUtils.getClass("com.tencent.mobileqq.richmediabrowser.AIOGalleryActivity");

        container.addMethod("hook_2",
                getResume(AIOGalleryActivityClass));

        container.addMethod("hook",
                MethodUtils.findMethod(AIOGalleryActivityClass, "onCreate", void.class, new Class[]{Bundle.class}));
    }

    @XPOperate(ID = "hook_3")
    public HookAction action_3() {
        return param -> {
//            LogUtils.addRunLog(cache);

            //静态方法不能保存参数对象 此钩子已弃用


//            OutputMethodStack.OutputObjectField(param.args[3]);
        };
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction action() {
        return param -> {
            Intent intent = MethodUtils.callNoParamsMethod(param.thisObject, "getIntent", Intent.class);
            bundle = intent.getExtras();

        };
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    @XPOperate(ID = "hook_2", period = XPOperate.After)
    public HookAction action_2() {
        return param -> {
            if (bundle == null) {
                LogUtils.addRunLog("PicUI bundle==null");
                return;
            }
            List<View> views = CommonTool.getAllChildViews((Activity) param.thisObject);
            Context context = (Context) param.thisObject;
            CommonTool.InjectResourcesToContext(context);
            RelativeLayout root = null;
            for (int i = 0; i < views.size(); i++) {
                View view = views.get(i);
                if (root == null) {
                    if (view instanceof RelativeLayout && i >= 3) {
                        root = (RelativeLayout) view;
                    }
                }
                if (view.getClass().equals(View.class)) {
                    view.setBackground(new BasicBackground());
                    break;
                }
            }
            @SuppressLint("InflateParams")
            View top = LayoutInflater.from(context).inflate(R.layout.pic_top_layout, null, false);

            String groupUin;
            String sendName = null;
            String sendUin;
            TextView sendUinView = top.findViewById(R.id.pic_send_uin);
            TextView isGroup = top.findViewById(R.id.type);
            isGroup.setOnClickListener(v -> CommonTool.Toast("可能点旁边的字会有什么反应呢"));
            TextView source = top.findViewById(R.id.pic_group_uin);
            //私聊时此值为聊天对象Uin
            String keyTroopGroupName = bundle.getString("key_troop_group_name");
            int type = bundle.getInt("uintype");
            if (type == 1) {
                groupUin = bundle.getString("extra.GROUP_UIN");
                isGroup.setText("群聊消息 ");
                source.setText("来自群组 : " + keyTroopGroupName + "(" +
                        groupUin + ")");
                source.setOnClickListener(v -> context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin="
                                + groupUin + "&card_type=group&source=qrcode"))));
            } else {
                groupUin = null;
                if (type == 0) {
                    source.setText("来自私聊与 " + keyTroopGroupName + " 的对话");
                    source.setOnClickListener(v -> {
                        String urlQQ = "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin="
                                + keyTroopGroupName;
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlQQ)));
                    });
                }
            }

            Object picData = bundle.getParcelable("extra.EXTRA_CURRENT_IMAGE");
            //查找被混淆的字段 发送者UIN
            Class<?> picDataClz = picData.getClass();
            for (Field field : picDataClz.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().equals(String.class)) {
                    String isSendUin = (String) field.get(picData);
                    //不同场景查找方式不同
                    if (groupUin != null) {
                        if (checkQQ(isSendUin) && !isSendUin.equals(groupUin)) {
                            fieldName = field.getName();
                        }
                    } else if (checkQQ(isSendUin) && !isSendUin.equals(keyTroopGroupName)) {
                        fieldName = field.getName();
                    }
                }
            }
            try {
                sendUin = FieIdUtils.getField(picData, fieldName, String.class);
            } catch (Exception e) {
                return;
            }

            sendUinView.setText("发送者 : " + sendUin);

            String finalSendUin = sendUin;
            sendUinView.setOnClickListener(v -> {
                String urlQQ = "mqq://card/show_pslcard?src_type=internal&source=sharecard&version=1&uin="
                        + finalSendUin;
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlQQ)));
            });

            int statusBarHeight1 = -1;
            //获取status_bar_height资源的ID
            @SuppressLint("InternalInsetResource")
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
            }

            top.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (top.getAlpha() >= 50) {
                        top.setAlpha(0);
                    } else {
                        top.setAlpha(255);
                    }
                }
            });
            top.setPadding(0, statusBarHeight1, 0, ScreenParamUtils.dpToPx(context, 10));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            root.addView(top.getRootView(), params);
        };
    }

}
