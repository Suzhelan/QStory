package HookItem.ChatHook.Repetition;

import ConfigTool.SimpleConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.QQUtils.Common;
import lin.xposed.R;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.io.*;
import java.util.List;

public class MainRepetitionIcon {
    public static final String TAG = "主复读";
    private static final String iconPath = PathInit.path + "+1.png";
    private static final SimpleConfig config = new SimpleConfig(TAG);
    public static boolean iconLocatedUpperRightCorner = config.getBoolean("图标位于右上角");
    public static boolean enableDoubleClicking = config.getBoolean("是否双击复读");
    public static int iconSize = config.getInt("图标大小", 80);
    public static int VerticalOffsetValue = config.getInt("垂直偏移值", 20);
    public static int HorizontalOffsetValue = config.getInt("水平偏移值", -45);
    public static Drawable icon;

    //Bitmap.CompressFormat.PNG Bitmap.CompressFormat.JPEG
    public static void drawableToFile(Drawable drawable, String filePath, Bitmap.CompressFormat format) {
        if (drawable == null)
            return;
        try {
            File file = new File(filePath);

            if (file.exists())
                file.delete();

            if (!file.exists())
                file.createNewFile();

            FileOutputStream out = null;
            out = new FileOutputStream(file);
            ((BitmapDrawable) drawable).getBitmap().compress(format, 100, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将本地文件转换为 Drawable
     */
    private static Drawable iconDrawable(String file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        Drawable drawable = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            drawable = new BitmapDrawable(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return drawable;
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "消息复读";
        ui.info = "点击可设置一些参数";
        ui.groupType = ClassifyMenu.base;
        ui.onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog();
            }
        };
        InitIcon();
        return ui;
    }

    private void createDialog() {
        Context context = CommonTool.getActivity();
        Dialog dialog = new Dialog(context, R.style.dialog);
        LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.repetition_set_params_layout, null, false);
        //查找复选框并设置
        CheckBox checkBox_1 = rootLayout.findViewById(R.id.iconLocatedUpperRightCorner);
        checkBox_1.setChecked(iconLocatedUpperRightCorner);
        checkBox_1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            //使用线程 避免闲的吃饱用户高速开关导致UI卡顿
            new Thread(() -> {
                iconLocatedUpperRightCorner = isChecked;
                config.putBoolean("图标位于右上角", isChecked);
            }).start();
        });
        CheckBox checkBox_2 = rootLayout.findViewById(R.id.enableDoubleClicking);
        checkBox_2.setChecked(enableDoubleClicking);
        checkBox_2.setOnCheckedChangeListener((buttonView, isChecked) -> new Thread(() -> {
            enableDoubleClicking = isChecked;
            config.putBoolean("是否双击复读", isChecked);
        }).start());
        EditText size = rootLayout.findViewById(R.id.icon_size);
        size.setText(String.valueOf(iconSize));
        EditText VerticalView = rootLayout.findViewById(R.id.VerticalOffsetValue);
        VerticalView.setText(String.valueOf(VerticalOffsetValue));
        EditText Horizontal = rootLayout.findViewById(R.id.HorizontalOffsetValue);
        Horizontal.setText(String.valueOf(HorizontalOffsetValue));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                try {
                    iconSize = Integer.parseInt(size.getText().toString().trim());
                    config.putInt("图标大小", iconSize);
                } catch (Exception e) {
                    CommonTool.Toast("图标大小输入错误 可能不是合法的数字\n\n" + e);
                }
                try {
                    VerticalOffsetValue = Integer.parseInt(VerticalView.getText().toString().trim());
                    config.putInt("垂直偏移值", VerticalOffsetValue);
                } catch (Exception e) {
                    CommonTool.Toast("垂直偏移值输入错误 可能不是合法的数字\n\n" + e);
                }
                try {
                    HorizontalOffsetValue = Integer.parseInt(Horizontal.getText().toString().trim());
                    config.putInt("水平偏移值", HorizontalOffsetValue);
                } catch (Exception e) {
                    CommonTool.Toast("水平偏移值输入错误 可能不是合法的数字\n\n" + e);
                }
                MenuLayout.isShow = false;
                MenuLayout.newDialog(MenuLayout.thisGroupName);
            }
        });
        dialog.setContentView(rootLayout);
        dialog.show();
        MenuLayout.dialog.dismiss();
        MenuLayout.isShow = true;
    }

    public void InitIcon() {
        new Thread(() ->
        {
            File iconFile = new File(iconPath);
            if (iconFile.exists()) {
                icon = iconDrawable(iconPath);
            } else {
                drawableToFile(HostEnv.context.getDrawable(R.drawable.repeat), iconPath, Bitmap.CompressFormat.PNG);
                CommonTool.Toast("复读图标已初始化完成");
            }
        }).start();
    }

    public void getMethod(MethodContainer container) {
        Common.AIOMessageListAdapter_getView(container);
        container.addMethod("hide_def_icon", MethodUtils.findMethod("com.tencent.mobileqq.data.ChatMessage", "isFollowMessage", boolean.class, new Class[0]));
    }

    @XPOperate(ID = "onAIOGetView", period = XPOperate.After)
    public HookAction hook() {
        return param -> {
            Object mGetView = param.getResult();
            RelativeLayout messageLayout;
            if (mGetView instanceof RelativeLayout) messageLayout = (RelativeLayout) mGetView;
            else return;
            List<Object> MessageRecoreList = FieIdUtils.getFirstField(param.thisObject, List.class);
            if (MessageRecoreList == null) return;
            Object ChatMsg = MessageRecoreList.get((int) param.args[0]);
            Context context = messageLayout.getContext();
            if (context.getClass().getName().contains("MultiForwardActivity")) return;
            RepetitionTools.createIcon(messageLayout, ChatMsg);
        };
    }

    @XPOperate(ID = "hide_def_icon")
    public HookAction hook_1() {
        return param -> {
            param.setResult(false);
        };
    }

}
