package HookItem.ChatHook;

import ConfigTool.GlobalConfig;
import HookItem.loadHook.UIInfo;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;

public class PictureDisplaysText {
    public static String PicText;//只在初始化和保存时更新减少这可有可无的性能开销

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "图片外显";
        ui.info = "点击设置";
        ui.groupType = "功能";
        ui.onClick = view -> showSavePicTextDialog();
        PicText = GlobalConfig.getString("PicText");
        return ui;
    }

    public void showSavePicTextDialog() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_edittext, null);
        EditText editText = layout.findViewById(R.id.base_editText);
        editText.setText(GlobalConfig.getString("PicText"));
        editText.setHint("为空则不生效");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    GlobalConfig.putString("PicText", editText.getText().toString());
                    PicText = GlobalConfig.getString("PicText");
                    CommonTool.Toast("已保存");
                    dialog.dismiss();
                }
            }
        });
        Button close = layout.findViewById(R.id.button2_close);
        close.setOnClickListener(view -> {
            dialog.dismiss();
        });
        Button yesSave = layout.findViewById(R.id.button2_yes);
        yesSave.setOnClickListener(v -> {
            GlobalConfig.putString("PicText", editText.getText().toString());
            PicText = GlobalConfig.getString("PicText");
            CommonTool.Toast("已保存");
            dialog.dismiss();
        });
        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.7, 0.4);
        dialog.setOnDismissListener(dialog1 -> {
            MenuLayout.isShow = false;
            MenuLayout.newDialog(MenuLayout.thisGroupName);
        });

        dialog.show();
        MenuLayout.dialog.dismiss();
    }
}
