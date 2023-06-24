package HookItem.ChatHook.Voice;

import ConfigTool.GlobalConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.MethodUtils;

import java.util.HashMap;
import java.util.Map;

public class VoiceRedPacketFlag {
    public static int VoiceFlag;//只在初始化和保存时更新减少这可有可无的性能开销
    public static Map<String, Integer> Flag = new HashMap<>();

    static {
        Flag.put("SSS", 1);
        Flag.put("", 0);
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "为语音添加语音红包标志";
        ui.info = "懒得说";
        ui.groupType = "功能";
        ui.onClick = view -> showSavePicTextDialog();
        VoiceFlag = GlobalConfig.getInt("VoiceFlag");
        return ui;
    }

    public void getAllMethod(MethodContainer container) {
        container.addMethod("hook", MethodUtils.findNoParamsMethod("com.tencent.mobileqq.data.MessageForPtt", "getSummaryMsg", String.class));

    }

    //    @XPOperate(ID = "hook",period = XPOperate.Before)
    public HookAction Hook() {
        return param -> {
            /*Object MessageRecord = param.thisObject;
            int sendType = FieIdUtils.getField(MessageRecord, "istroop", int.class);
            if (sendType == 1 || sendType == 0) {
                String sendUin = FieIdUtils.getField(MessageRecord, "senderuin", String.class);
                if (sendUin.equals(QQEnvUtils.getCurrentUin())) {
                    MessageProcessingder.setVoiceRedPacketFlag(MessageRecord);
                }
                param.setResult("msg");
            }*/
        };
    }

    public void showSavePicTextDialog() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        CommonTool.InjectResourcesToContext(context);
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_edittext, null);
        EditText editText = layout.findViewById(R.id.base_editText);
        for (String i : Flag.keySet()) {
            if (GlobalConfig.getInt("VoiceFlag") == Flag.get(i)) {
                editText.setText(i);
            }
        }

        editText.setHint("为空则不生效");
        Button close = layout.findViewById(R.id.button2_close);
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        Button yesSave = layout.findViewById(R.id.button2_yes);
        yesSave.setOnClickListener(v -> {
            if (Flag.containsKey(editText.getText().toString().trim())) {
                int flag = Flag.get(editText.getText().toString().trim());
                GlobalConfig.putInt("VoiceFlag", flag);
                VoiceFlag = GlobalConfig.getInt("VoiceFlag");
                CommonTool.Toast("已保存");
                dialog.dismiss();
                return;
            }
            CommonTool.Toast("该标志无效");
        });
        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.7, 0.2);
        dialog.setOnDismissListener(dialog1 -> {
            MenuLayout.isShow = false;
            MenuLayout.newDialog(MenuLayout.thisGroupName);
        });

        dialog.show();
        MenuLayout.dialog.dismiss();
    }
}
