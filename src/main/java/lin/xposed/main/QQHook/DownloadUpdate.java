package lin.xposed.main.QQHook;

import ConfigTool.GlobalConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.content.Context;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.io.File;

public class DownloadUpdate {
    private static String oldPath;

    private static boolean checkMkDir(String Path) {
        File f = new File(Path);
        f.mkdirs();
        return f.exists() && f.isDirectory();
    }

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.name = "下载文件位置更改";
        ui.info = "点击设置路径 重启生效";
        ui.onClick = v -> showSaveDialog();
        ui.groupType = ClassifyMenu.base;
        return ui;
    }

    public void showSaveDialog() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_edittext, null);
        EditText editText = layout.findViewById(R.id.base_editText);
        String downloadPath = GlobalConfig.getString("DownloadPath");
        editText.setText(TextUtils.isEmpty(downloadPath) ? HostEnv.context.getExternalCacheDir().getParent() + "/Tencent/QQfile_recv/" : downloadPath);
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
                    GlobalConfig.putString("DownloadPath", editText.getText().toString().replace("\n", ""));
                    if (checkMkDir(GlobalConfig.getString("DownloadPath"))) {
                        CommonTool.Toast("已保存 重启生效");
                    } else {
                        CommonTool.Toast("可能对此没有读写权限");
                    }
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
            GlobalConfig.putString("DownloadPath", editText.getText().toString().trim());
            if (checkMkDir(GlobalConfig.getString("DownloadPath"))) {
                CommonTool.Toast("已保存 重启生效");
            } else {
                CommonTool.Toast("可能对此没有读写权限");
            }
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

    public void getMethod(MethodContainer container) {
        container.addMethod("hook", MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.vfs.VFSAssistantUtils"), "getSDKPrivatePath", String.class, new Class[]{
                String.class
        }));
        container.addMethod("hook_2",
                MethodUtils.findMethod(ClassUtils.getClass("com.tencent.guild.api.msg.impl.GuildMsgApiImpl"), "getNTKernelExtDataPath", String.class, new Class[0]));
    }

    @XPOperate(ID = "hook_2", period = XPOperate.After)
    public HookAction hook_1() {
        return param -> {
            param.setResult(oldPath);
        };
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction hook_2() {
        return param -> {

            String Path = (String) param.args[0];
            String Result = (String) param.getResult();
            if (Result.contains("/Tencent/QQfile_recv/")) {
                if (new File(Result).exists() && new File(Result).isFile())
                    return;//如果下载的文件已经存在则不替换,防止与QQ文件数据库出错而导致无法下载的问题

                String End = Path.substring(Path.lastIndexOf("/Tencent/QQfile_recv/") + "/Tencent/QQfile_recv/".length());

                oldPath = Result.substring(0, Result.lastIndexOf("/Tencent/QQfile_recv/") + "/Tencent/QQfile_recv/".length());

                String Start = GlobalConfig.getString("DownloadPath");

                //如果没有设置过且开启此功能就默认定向到
                if (TextUtils.isEmpty(Start))
                    Start = Environment.getExternalStorageDirectory() + "/Download/QQ下载/";

                if (!Start.endsWith("/")) Start = Start + "/";
                param.setResult(Start + End);
            }
        };
    }


}
