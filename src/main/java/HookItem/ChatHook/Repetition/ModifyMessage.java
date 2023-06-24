package HookItem.ChatHook.Repetition;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.Utils.LogUtils;

public class ModifyMessage {
    @SuppressLint("SetTextI18n")
    public static void show(Object message) {
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        @SuppressLint("InflateParams") LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.modify_message, null);
        TextView textView = layout.findViewById(R.id.MessageContent);
        EditText content = layout.findViewById(R.id.Msg_Content);
        EditText sendQQ = layout.findViewById(R.id.sendMsgUin);
        String sendUin = null;
        try {
            sendUin = FieIdUtils.getField(message, "senderuin", String.class);
        } catch (Exception e) {
            sendUin = "未能获取到发送者";
        }
        sendQQ.setText(sendUin);

        String simpleName = message.getClass().getSimpleName();
        textView.setText(simpleName);
        try {
            switch (simpleName) {
                case "MessageForReplyText":
                case "MessageForText":
                case "MessageForLongTextMsg":
                case "MessageForFoldMsg":
                    content.setText(FieIdUtils.getField(message, "msg", String.class));
                    break;
                case "MessageForPic":
                    textView.setText("图片MD5");
                    content.setText(FieIdUtils.getField(message, "md5", String.class));
                    break;
                default:
                    CommonTool.Toast("消息类型不支持 长按也没用");
                    return;
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    if (simpleName.equals("MessageForPic")) {
                        FieIdUtils.setField(message, "md5", content.getText().toString());
                    } else {
                        FieIdUtils.setField(message, "msg", content.getText().toString());
                    }
                    FieIdUtils.setField(message, "senderuin", sendQQ.getText().toString().trim());
                } catch (Exception e) {
                    CommonTool.Toast(e);
                    LogUtils.addError(e);
                }
            }
        });
        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.8, 0.5);
        dialog.show();
    }
}
