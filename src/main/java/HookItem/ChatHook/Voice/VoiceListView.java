package HookItem.ChatHook.Voice;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.QQUtils.SendUtils;
import lin.xposed.R;
import lin.xposed.Utils.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class VoiceListView {
    private static final AtomicLong updateTime = new AtomicLong();

    private static final AtomicBoolean isRootDirectory = new AtomicBoolean();

    //定义父目录,防止多层目录迷失自我
    private static String Parent;

    @SuppressLint("SetTextI18n")
    public static View buildView(Dialog dialog, Context context, String path) {
        isRootDirectory.set(path.equals(new File(PathInit.paths[2]).getAbsolutePath()) || path.equals(PathInit.paths[2]));
        File file = new File(path);
        Parent = file.getParent();
        BasicBackground background = new BasicBackground();
        background.setCornerRadius(15);
        background.setAlpha(0.4);
        background.setPadding(30, 15, 15, 5);
        background.setStroke(2, Color.parseColor(BasicBackground.COLOR_Lines));

        BasicBackground fileBackground = new BasicBackground();
        fileBackground.setCornerRadius(15);
        fileBackground.setAlpha(0.3);
        fileBackground.setPadding(30, 15, 15, 5);
        fileBackground.setStroke(2, Color.parseColor(BasicBackground.COLOR_Lines));

        CommonTool.InjectResourcesToContext(context);
        @SuppressLint("InflateParams") ScrollView scrollView = (ScrollView) LayoutInflater.from(context).inflate(R.layout.send_voice_layout, null);
        LinearLayout layout = scrollView.findViewById(R.id.send_voice_layout);
        layout.post(() -> {
            TextView tvs = new TextView(context);
            tvs.setText("上一级:" + Parent);
            tvs.setTextColor(Color.parseColor("#000000"));
            tvs.setTextSize(10);
            tvs.setOnClickListener(v -> {
                if (Parent.equals("/storage/emulated")) {
                    CommonTool.Toast("已是所能探索到的极限");
                    return;
                }
                dialog.setContentView(buildView(dialog, context, Parent));
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(30, 10, 15, 5);
            layout.addView(tvs, params);
            boolean isFirst = true;
            List<File> list = Arrays.asList(file.listFiles());
            // 按文件夹先显示的顺序：
            list.sort((o1, o2) -> {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            });
            for (File f : list) {
                if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
                    continue;
                }
                TextView tv = new TextView(context);
                tv.setText(f.getName());
                tv.setTextColor(Color.parseColor("#80000000"));
                tv.setPadding(30, 15, 15, 15);
                tv.setTextSize(20);
                tv.setOnLongClickListener(v -> {
                    tryDeleteVoiceFile(f, dialog, context, path);
                    return true;
                });
                if (f.isDirectory()) tv.setBackground(background);
                if (f.isFile()) tv.setBackground(fileBackground);
                tv.setOnClickListener(view -> {
                    if (f.isFile()) {
                        SendUtils.sendVoice(HostEnv.SessionInfo, f.getAbsolutePath());
                    } else {
                        dialog.setOnCancelListener(dialogs -> {
                            if (isRootDirectory.get()) return;
                            if (Parent.equals("/storage/emulated")) return;
                            dialog.setContentView(buildView(dialog, context, Parent));
                            dialog.show();
                        });
                        dialog.setContentView(buildView(dialog, context, f.getAbsolutePath()));
                    }
                });
                layout.addView(tv, getParams(isFirst));
                isFirst = false;
            }
            if (isFirst) {
                TextView tv = new TextView(context);
                tv.setText("当前目录无文件");
                tv.setTextColor(Color.parseColor("#80000000"));
                tv.setTextSize(20);
                tv.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params1.setMargins(30, 30, 15, 5);
                layout.addView(tv, params1);
            }
        });
        updateTime.set(file.lastModified());
        return scrollView;
    }

    private static void tryDeleteVoiceFile(File file, Dialog dialogs, Context contexts, String path) {
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);

        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.save_voice_layout, null, false);
        LinearLayout linearLayout = layout.findViewById(R.id.save_voice_root_layout);
        linearLayout.setBackground(new BasicBackground());
        TextView title = layout.findViewById(R.id.save_voice_title);
        title.setText("删除此文件");
        title.setTextColor(CommonTool.getColors(context, R.color.蔷薇色));
        EditText editText = layout.findViewById(R.id.voice_name);
        editText.setText(file.getName());
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
                    if (new File(file.getParentFile() + "/" + names).exists()) {
                        dialog.dismiss();
                        VoiceTools.repeatFileName(file.getAbsolutePath(), names);
                        return;
                    }
                    if (file.renameTo(new File(file.getParentFile() + "/" + names))) {
                        dialog.dismiss();
                    } else {
                        editText.setText(names);
                        CommonTool.Toast("重命名失败 可能已经有重名文件或格式错误");
                    }
                }
            }
        });
        Button yesSave = layout.findViewById(R.id.yes_save_voice);
        yesSave.setText("删除");
        yesSave.setOnClickListener(v -> {
            FileUtils.deleteFile(file);
            dialog.dismiss();
        });
        Button noSave = layout.findViewById(R.id.save_voice_close);
        noSave.setText("重命名");
        noSave.setOnClickListener(view -> {
            String names = editText.getText().toString();
            if (file.renameTo(new File(file.getParentFile() + "/" + names))) {
                dialog.dismiss();
            } else {
                CommonTool.Toast("重命名失败 可能已经有重名文件");
            }
        });
        dialog.setContentView(layout);
        dialog.setOnDismissListener(dialog1 -> dialogs.setContentView(buildView(dialogs, contexts, path)));

        dialog.setDialogWindowAttr(0.6, 0.25);
        dialog.show();
    }

    private static void 排序(int type) {
        if (type == 1) {
        }
    }

    private static ViewGroup.LayoutParams getParams(boolean isFirst) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, isFirst ? 15 : 10, 10, 5);
        return params;
    }
}
