package lin.xposed.LayoutView;

import HookItem.note.QQVersion;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;

import java.lang.reflect.Field;

public class BaseDialog {

    @SuppressLint("StaticFieldLeak")
    private static mDialog dialog;

    public static void create(Context activity) {
        if (dialog == null || dialog.isStop) {
            dialog = new mDialog(activity);
            dialog.setContentView(getBaseView(activity));
            dialog.setDialogWindowAttr(0.7, 0.5);
        }
        dialog.show();
    }


    @SuppressLint("SetTextI18n")
    private static View getBaseView(Context context) {
        //根布局实例化 参数（布局文件，父布局（如果有），是否添加到父布局（如果有）
        @SuppressLint("InflateParams")
        LinearLayout rootLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.base_main, null, false);

        for (String itemType : ClassifyMenu.mainClassify) {

            //item 根据需要的实例化数量
            @SuppressLint("InflateParams") LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.base_main_classify, null, false);
            MenuLayout.OnAction action = view -> {
                dialog.dismiss();
                MenuLayout.newDialog(itemType);
            };
            itemLayout.setOnClickListener(action::Action);
            //功能分类不需要图标 至少现在不需要
            ImageView icon = itemLayout.findViewById(R.id.base_item_icon);
            itemLayout.removeView(icon);

            TextView textView = itemLayout.findViewById(R.id.base_item_name);
            textView.setText(itemType);

            /*
             * XML Layout的margins在add的View时候会失效 需要重新设置一下
             */
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(25, 15, 25, 15);
            rootLayout.addView(itemLayout, params);
        }

        @SuppressLint("InflateParams") LinearLayout itemLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.base_main_classify, null, false);
        itemLayout.setBackground(null);
        ImageView icon = itemLayout.findViewById(R.id.base_item_icon);
        itemLayout.removeView(icon);
        TextView textView = itemLayout.findViewById(R.id.base_item_name);
        textView.setText("v" + HostEnv.moduleVersionName);
        textView.setTextColor(CommonTool.getColors(context, R.color.桔梗色));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(25, 20, 25, 15);

        rootLayout.addView(itemLayout, params);

        int min = 0, max = 0;
        String minName="?",maxName="?";
        //反射到最小和最大版本号
        for (Field field : QQVersion.class.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                int V = field.getInt(null);
                if (max < V) {
                    maxName = field.getName();
                    max = V;
                }
                if (min > V) {
                    minName = field.getName();
                    min = V;
                }
            } catch (IllegalAccessException e) {

            }
        }
        if (HostEnv.QQVersion <= max && HostEnv.QQVersion >= min) {
            return rootLayout;
        }
        @SuppressLint("InflateParams")
        LinearLayout itemLayout2 = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.base_main_classify, null, false);
        itemLayout2.setBackground(null);
        ImageView icon2 = itemLayout2.findViewById(R.id.base_item_icon);
        itemLayout2.removeView(icon2);
        TextView textView2 = itemLayout2.findViewById(R.id.base_item_name);
        textView2.setText("当前版本可能不在维护范围内 维护范围 "+minName+" - "+maxName);
        textView2.setTextColor(CommonTool.getColors(context, R.color.蔷薇色));
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.setMargins(25, 20, 25, 15);
        rootLayout.addView(itemLayout2, params2);
        return rootLayout;
    }
}
