package lin.xposed.LayoutView;

import HookItem.LoadItemInfo.BaseMethodInfo;
import HookItem.loadHook.HookItemMainInfo;
import HookItem.loadHook.UIInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.Other.Debug;
import lin.xposed.LayoutView.Other.emmmmm;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.SettingsLoader;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

public class MenuLayout {
    @SuppressLint("StaticFieldLeak")
    public static mDialog dialog;
    public static boolean isShow;
    public static String thisGroupName;

    public static void newDialog(String group) {
        Activity context = CommonTool.getActivity();
        dialog = new mDialog(context);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.main_background);
        dialog.setContentView(newView(group, context));
        //监听dialog关闭后返回打开分类的弹窗并把设置备份到本地
        dialog.setOnDismissListener(thisDialog -> {
            if (isShow) return;
            BaseDialog.create(context);
            saveSettings();
        });
        dialog.setDialogWindowAttr(0.7, 0.5);
        dialog.show();
    }


    private static View newView(String groupName, Context context) {
        thisGroupName = groupName;
        //通过某个类确定想要生成的View,map结构 <groupName,List<Info>>
        ScrollView rootView = new ScrollView(context);
        rootView.setVerticalScrollBarEnabled(false);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 0);
        //非功能列表内的 就得一个一个自己判断

        boolean isHookItem = false;
        for (HookItemMainInfo.XPItemInfo info : HookItemMainInfo.itemInstance.values()) {
            //在HookItem列表中寻找符合groupName的UI
            UIInfo uiInfo = info.ui;
            if (uiInfo == null || !uiInfo.groupType.equals(groupName)) {
                continue;//不符合传进来的分类就略过 防止没有提供ui的item添加进来
            }
            isHookItem = true;
            @SuppressLint("InflateParams")
            RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.main_item_layout, null, false);

            LinearLayout itemTextLayout = itemLayout.findViewById(R.id.item_text_layout);
            {
                TextView name = itemTextLayout.findViewById(R.id.item_name);
                name.setText(uiInfo.name);
                if (!info.yesMethod) name.setTextColor(CommonTool.getColors(context, R.color.蔷薇色));
                //该功能是否包含了更详细的信息 如果有就设置 没有就移除布局 itemName会自动居中
                if (uiInfo.info != null) {
                    TextView itemInfo = itemTextLayout.findViewById(R.id.item_info);
                    itemInfo.setText(uiInfo.info);
                    Paint testPaint = itemInfo.getPaint();
                    String text = itemInfo.getText().toString();
                    int textWidth = itemInfo.getMeasuredWidth();
                    if (textWidth > 0) {
                        int availableWidth = textWidth - itemInfo.getPaddingLeft() -
                                itemInfo.getPaddingRight();
                        float trySize = itemInfo.getTextSize();
                        testPaint.setTextSize(trySize);
                        while ((testPaint.measureText(text) > availableWidth)) {
                            trySize -= 2;
                            itemInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, trySize);   //这里必须使用px，因为testPaint.measureText(text)和availableWidth的单位都是px
                        }
                        itemInfo.setTextSize(trySize);
                    }
                } else {
                    itemTextLayout.removeView(itemTextLayout.findViewById(R.id.item_info));
                }
                if (uiInfo.onClick != null) itemLayout.setOnClickListener(uiInfo.onClick);
            }
            //长按开关项目下的所有钩子 返回true说明不需要其他回调处理
            if (HostEnv.isDebug) {
                itemLayout.setOnLongClickListener(view -> {
                    deBugItemDialog(info);
                    return true;
                });
            }
            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch switchView = itemLayout.findViewById(R.id.item_switch);//开关
            switchView.setChecked(info.Enabled);
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> info.Enabled = isChecked);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(15, 15, 15, 0);

            layout.addView(itemLayout, params);
        }

        if (!isHookItem) {
            switch (groupName) {
                case "调试方面": {
                    Debug.addDebugView(layout);
                }
                break;
                case "模块设置": {

                }
                break;
                case "关于与更新":
                    emmmmm.addEmmmView(layout);
                    break;
            }
            rootView.addView(layout);
            return rootView;
        }
        rootView.addView(layout);
        return rootView;
    }

    @SuppressLint("SetTextI18n")
    private static void deBugItemDialog(HookItemMainInfo.XPItemInfo info) {
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        ScrollView rootView = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 0);
        for (Map.Entry<String, BaseMethodInfo> baseInfo : info.NeedMethodInfo.entrySet()) {
            @SuppressLint("InflateParams")
            RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.main_item_layout, null, false);
            LinearLayout itemTextLayout = itemLayout.findViewById(R.id.item_text_layout);
            BaseMethodInfo baseMethodInfo = baseInfo.getValue();
            {
                TextView name = itemTextLayout.findViewById(R.id.item_name);
                name.setText(baseInfo.getKey());
                if (info.scanResult.get(baseInfo.getKey()) != null) {
                    TextView itemInfo = itemTextLayout.findViewById(R.id.item_info);
                    itemInfo.setText(MethodUtils.GetMethodInfoText((Method) Objects.requireNonNull(info.scanResult.get(baseInfo.getKey()))));
                } else {
                    TextView itemInfo = itemTextLayout.findViewById(R.id.item_info);
                    itemInfo.setText("没有查找到方法 该功能失效");
                }
            }

            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch switchView = itemLayout.findViewById(R.id.item_switch);
            switchView.setChecked(baseMethodInfo.TheHookOpens);
            switchView.setOnCheckedChangeListener((buttonView, isChecked) -> baseMethodInfo.TheHookOpens = isChecked);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(15, 15, 15, 0);
            itemTextLayout.getLayoutParams().height = params.height;
            layout.addView(itemLayout, params);
        }
        rootView.addView(layout);
        dialog.setOnDismissListener(thisDialog -> saveSettings());
        dialog.setContentView(rootView);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.main_background);
        dialog.show();
    }

    private static void saveSettings() {

        new Thread(SettingsLoader::saveSettings).start();
    }

    public interface OnAction {
        void Action(View view);
    }
}
