package lin.xposed.main.QQHook;

import ConfigTool.ListConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.util.Map;

public class DPI_SET {

    public static final Map<String, Integer> DATA =
            (Map<String, Integer>) ListConfig.getMap("DPI_SET");
    public static int SetDPI = 0;

    static {
        if (SetDPI == 0) {
            if (DATA.containsKey("DPI_SET")) {
                SetDPI = DATA.get("DPI_SET");
            }
        }
    }

    public void Start() {
        float scale = 1.0f / 160;
        if (SetDPI != 0) {
            try {


               /* XposedHelpers.findAndHookMethod(Resources.class, "updateConfiguration",
                        Configuration.class, DisplayMetrics.class,
                        ClassUtils.getClass("android.content.res.CompatibilityInfo"),
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                int setDpi = SetDPI;
                                if (setDpi > 1500) setDpi = 1500;

                                if (setDpi != 0) {
                                    DisplayMetrics metrics = (DisplayMetrics) param.args[1];
                                    if (metrics == null) return;
                                    metrics.densityDpi = setDpi;
                                    metrics.density = setDpi * scale;


                                    Configuration conf = (Configuration) param.args[0];
                                    conf.densityDpi = setDpi;
                                    //metrics.scaledDensity = setDpi;
                                }
                            }
                        });*/

                XposedHelpers.findAndHookMethod(Resources.class, "updateConfiguration",
                        Configuration.class, DisplayMetrics.class, ClassUtils.getClass("android.content.res.CompatibilityInfo"),
                        new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                super.beforeHookedMethod(param);
                                int setDpi = SetDPI;
                                if (setDpi > 1500) setDpi = 1500;

                                if (setDpi != 0) {
                                    DisplayMetrics metrics = (DisplayMetrics) param.args[1];
                                    if (metrics == null) return;
                                    metrics.densityDpi = setDpi;
                                    metrics.density = setDpi * scale;


                                    Configuration conf = (Configuration) param.args[0];
                                    conf.densityDpi = setDpi;
                                    //metrics.scaledDensity = setDpi;
                                }
                            }
                        });


            } catch (Throwable e) {

            }

        }
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = "功能";
        ui.name = "修改DPI";
        ui.info = "点击设置 重启生效";
        ui.onClick = v -> showSavePicTextDialog();
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("hook",
                MethodUtils.findUnknownReturnMethod(Resources.class,
                        "getDisplayMetrics",
                        new Class[0]));

//        Start();
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction action() {
        return param -> {
            float scale = 1.0f / 160;
            if (SetDPI != 0) {
                int setDpi = SetDPI;
                if (setDpi > 1500) setDpi = 1500;
                DisplayMetrics metrics = (DisplayMetrics) param.getResult();
                if (metrics == null) return;
                metrics.densityDpi = setDpi;
                metrics.density = setDpi * scale;
            }
//            Start();
        };
    }

    @SuppressLint("SetTextI18n")
    public void showSavePicTextDialog() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        @SuppressLint("InflateParams")
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.common_edittext, null);
        TextView title = layout.findViewById(R.id.common_edittext_title);
        title.setText("使用前请截图 此功能非常的不安全 可能会导致QQ变得不正常 如果出现异常请删除 \n" +
                PathInit.paths[1] + "DPI_SET 文件即可恢复");
        EditText editText = layout.findViewById(R.id.base_editText);
        if (SetDPI != 0) editText.setText(String.valueOf(SetDPI));
        else editText.setText(String.valueOf(context.getResources().getDisplayMetrics().densityDpi));
        editText.setHint("为空则坏掉");
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (s.toString().contains("\n")) {
                        DATA.put("DPI_SET", Integer.parseInt(editText.getText().toString()));
                        SetDPI = DATA.get("DPI_SET");
                        ListConfig.setMapToFile("DPI_SET", DATA);
                        CommonTool.Toast("已保存");
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    CommonTool.Toast(e);
                }
            }
        });
        Button close = layout.findViewById(R.id.button2_close);
        close.setOnClickListener(view -> dialog.dismiss());
        Button yesSave = layout.findViewById(R.id.button2_yes);
        yesSave.setOnClickListener(v -> {
            try {
                DATA.put("DPI_SET", Integer.parseInt(editText.getText().toString()));
                SetDPI = DATA.get("DPI_SET");
                ListConfig.setMapToFile("DPI_SET", DATA);
                CommonTool.Toast("已保存");
                dialog.dismiss();
            } catch (NumberFormatException e) {
                CommonTool.Toast(e);
            }
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
