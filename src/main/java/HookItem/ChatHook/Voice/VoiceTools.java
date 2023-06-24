package HookItem.ChatHook.Voice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.Utils.FileUtils;
import lin.xposed.Utils.LogUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VoiceTools {
    private static mDialog dialog;

    public static void createSaveVoiceDialog(Context context, String path) {
        CommonTool.InjectResourcesToContext(context);
        try {
            dialog = new mDialog(context);
            View v = getSaveVoiceView(context, path);
            dialog.setContentView(v);
            dialog.setDialogWindowAttr(0.7, 0.25);
            dialog.show();
        } catch (Exception e) {
            LogUtils.addError("创建保存语音面板 已抛出异常", e);
        }
    }

    private static View getSaveVoiceView(Context context, String path) {
        @SuppressLint("InflateParams")
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.save_voice_layout, null, false);
        LinearLayout linearLayout = layout.findViewById(R.id.save_voice_root_layout);
        linearLayout.setBackground(new BasicBackground());
        EditText editText = layout.findViewById(R.id.voice_name);
        editText.setText("");
        //监听实时更改dialog大小 防止出现对话框内容多占满整块布局
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n") && !s.toString().equals("")) {
                    saveVoice(path, s.toString().replace("\n", ""));
                }
            }
        });
        Button yesSave = layout.findViewById(R.id.yes_save_voice);
        yesSave.setOnClickListener(v -> {
            String name = editText.getText().toString();
            saveVoice(path, name);
        });
        Button noSave = layout.findViewById(R.id.save_voice_close);
        noSave.setOnClickListener(view -> {
            //关闭dialog
            dialog.dismiss();
        });
        return layout;
    }

    private static void saveVoice(String path, String name) {
        if (!new File(path).exists()) {
            CommonTool.Toast("语音可能尚未加载完毕");
            return;
        } else if (name.trim().equals("")) {
            name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        }
        //防止重复的文件名
        else if (new File(PathInit.paths[2] + name).exists()) {
            repeatFileName(path, name);
            return;
        }

        final String newName = name;
        new Thread(() -> {
            FileUtils.copy(path, PathInit.paths[2] + newName);
            CommonTool.Toast("语音已保存到" + PathInit.paths[2] + newName);
            //关闭dialog 复制文件在线程里做 避免阻塞UI
        }).start();
        dialog.dismiss();
    }

    @SuppressLint("SetTextI18n")
    public static void repeatFileName(String path, String name) {
        Context context = CommonTool.getActivity();
        mDialog dialog1 = new mDialog(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.save_voice_layout, null, false);
        LinearLayout linearLayout = layout.findViewById(R.id.save_voice_root_layout);
        linearLayout.setBackground(new BasicBackground());
        TextView title = layout.findViewById(R.id.save_voice_title);
        title.setText("文件名重复 目录已存在 " + name);
        title.setTextColor(CommonTool.getColors(context, R.color.蔷薇色));
        EditText editText = layout.findViewById(R.id.voice_name);
        editText.setText(name);
        Button yesSave = layout.findViewById(R.id.yes_save_voice);
        yesSave.setText("覆盖此目标文件");
        yesSave.setOnClickListener(v -> {
            FileUtils.copy(path, PathInit.paths[2] + name);
            dialog1.dismiss();
            CommonTool.Toast("已覆盖该文件");
        });
        Button noSave = layout.findViewById(R.id.save_voice_close);
        noSave.setText("保留两个文件");
        noSave.setOnClickListener(view -> {
            extracted(path, name);
            dialog1.dismiss();
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n") && !s.toString().equals("")) {
                    String names = editText.getText().toString().replace("\n", "");
                    extracted(path, names);
                    dialog1.dismiss();
                    return;
                }
                yesSave.setText("保存");
                noSave.setText("思考");
                noSave.setOnClickListener(v -> dialog1.dismiss());
            }
        });
        dialog1.setContentView(layout);
        dialog1.setDialogWindowAttr(0.7, 0.25);
        dialog1.show();
        dialog.dismiss();
    }

    private static void extracted(String path, String name) {
        String numbering = PathInit.paths[2] + name + "(1)";
        while (new File((numbering)).exists()) {
            //获取编号值
            String num = numbering.substring(numbering.lastIndexOf("(") + 1, numbering.lastIndexOf(")"));
            //提升编号索引
            int i = Integer.parseInt(num);
            i++;
            numbering = PathInit.paths[2] + name + "(" + i + ")";
        }
        FileUtils.copy(path, numbering);
        CommonTool.Toast("已生成新的语音文件" + numbering);
    }
}
