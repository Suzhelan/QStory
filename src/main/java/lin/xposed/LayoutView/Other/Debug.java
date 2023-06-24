package lin.xposed.LayoutView.Other;

import ConfigTool.GlobalConfig;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.Initialize.PathInit;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mButton;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;
import lin.xposed.Utils.MFileUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class Debug {
    private static final ArrayList<item> demoList = new ArrayList<>();

    static {
        demoList.add(new item("结束所有进程", v -> CommonTool.killAppProcess()));
        demoList.add(new item("清除适配信息", v -> {
            GlobalConfig.putString("Relative version", "");
            CommonTool.Toast("适配信息已清理,重启QQ会重新初始化");
        }));

//            demoList.add(new item("环境信息", v -> EnvInfo.createDialog()));
        demoList.add(new item("类名反射静态和结构信息", v -> {
            getClassInfo();
        }));
        demoList.add(new item("方法动态反射属性与堆栈", v -> OutputMethodStackView.startShow()));
        demoList.add(new item("OutBaseEnvInfo", view -> {
            new Thread(() -> {
                String info = LogUtils.OutputDeviceInfo(true).toString();
                MFileUtils.writeTextToFile(PathInit.paths[0] + "sendEnvInfo.txt", info, false);
                CommonTool.Toast("ok");
            }).start();
        }));
    }

    private static void getClassInfo() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_edittext, null);
        TextView textView = layout.findViewById(R.id.common_edittext_title);
        textView.setText("获取类信息");
        EditText editText = layout.findViewById(R.id.base_editText);
        editText.setHint("反射的类名 内部类用$相隔");
        editText.setText("");
        Button close = layout.findViewById(R.id.button2_close);
        close.setOnClickListener(view -> dialog.dismiss());
        Button yesSave = layout.findViewById(R.id.button2_yes);
        yesSave.setText("确认");
        yesSave.setOnClickListener(view -> {
            if (outClassInfo(editText.getText().toString().trim())) dialog.dismiss();
        });
        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.7, 0.3);
        dialog.setOnDismissListener(dialog1 -> {
            MenuLayout.isShow = false;
            MenuLayout.newDialog(MenuLayout.thisGroupName);
        });
        dialog.show();
        MenuLayout.dialog.dismiss();
    }

    private static boolean outClassInfo(String className) {
        Class<?> clz = ClassUtils.getClass(className);
        if (clz == null) {
            CommonTool.Toast("未查找到类 " + className + " 请确认输入无误或没有遭到随版本更新的动态混淆");
            return false;
        }
        StringBuffer stringBuffer = new StringBuffer("类名 :\n");
        getClassInfo(stringBuffer, clz);
        stringBuffer.append("\n内部类 : \n");
        if (clz.getDeclaredClasses().length == 0) stringBuffer.append("没有o");
        for (Class<?> clazzs : clz.getDeclaredClasses()) {
            getClassInfo(stringBuffer, clazzs);
        }

        MFileUtils.writeTextToFile(PathInit.paths[0] + className + ".java", stringBuffer.toString(), false);
        CommonTool.Toast("已将类信息输出到模块的Log目录");
        return true;
    }

    private static void getClassInfo(StringBuffer stringBuffer, Class<?> clazzs) {
        String str = "没有o";
        stringBuffer.append(Modifier.toString(clazzs.getModifiers())).append(" ");
        stringBuffer.append(clazzs.getName());
        if (clazzs.getSuperclass() != null) {
            stringBuffer.append(" extends ").append(clazzs.getSuperclass().getName());
        }
        if (clazzs.getInterfaces().length != 0) {
            stringBuffer.append(" implements ");
            for (Class<?> clazz : clazzs.getInterfaces()) {
                stringBuffer.append(clazz.getName()).append(" , ");
            }
            if (stringBuffer.toString().endsWith(" , ")) {
                stringBuffer.delete(stringBuffer.length() - 3, stringBuffer.length());
            }
        }
        stringBuffer.append("{\n\n属性 :\n");
        if (clazzs.getDeclaredFields().length == 0) stringBuffer.append(str);
        for (Field f : clazzs.getDeclaredFields()) {
            f.setAccessible(true);
            stringBuffer.append("   ");
            stringBuffer.append(Modifier.toString(f.getModifiers())).append(" ");
            String type = f.getType().getName().startsWith("java.lang.") ? f.getType().getSimpleName() : f.getType().getName();
            stringBuffer.append(type).append(" ").append(f.getName());
            try {
                if (Modifier.toString(f.getModifiers()).contains("static")) {
                    stringBuffer.append(" = ");
                    stringBuffer.append(f.get(null));
                }
            } catch (IllegalAccessException e) {

            }
            stringBuffer.append(";");
            stringBuffer.append("\n");
        }
        stringBuffer.append("\n构造方法 :\n");
        if (clazzs.getDeclaredConstructors().length == 0) stringBuffer.append(str);
        for (Constructor<?> c : clazzs.getDeclaredConstructors()) {
            c.setAccessible(true);
            stringBuffer.append("   ");
            stringBuffer.append(Modifier.toString(c.getModifiers())).append(" ");
            stringBuffer.append(clazzs.getSimpleName());
            stringBuffer.append("(");
            //参数类型
            for (Class<?> paramsType : c.getParameterTypes()) {
                stringBuffer.append(paramsType.getName()).append(" , ");
            }
            if (stringBuffer.toString().endsWith(" , ")) {
                stringBuffer.delete(stringBuffer.length() - 3, stringBuffer.length());
            }
            stringBuffer.append(");\n");
        }
        stringBuffer.append("\n方法 :\n");
        if (clazzs.getDeclaredMethods().length == 0) stringBuffer.append(str);
        for (Method m : clazzs.getDeclaredMethods()) {
            m.setAccessible(true);
            stringBuffer.append("   ");
            stringBuffer.append(MethodUtils.GetMethodInfo(m).Signature);
            stringBuffer.append("\n");
        }
        stringBuffer.append("\n}");
    }

    @SuppressLint("SetTextI18n")
    public static void addDebugView(ViewGroup view) {

        Context context = view.getContext();
        for (item d : demoList) {
            mButton button = new mButton(context);
            button.setText(d.text);
            button.setOnClickListener(v -> d.click.on(v));
            view.addView(button.layout, getParams());
        }

    }

    private static ViewGroup.LayoutParams getParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 15, 15, 15);
        return params;
    }

    private static class item {
        public String text;
        public onClick click;

        public item(String text, onClick click) {
            this.click = click;
            this.text = text;
        }

        public interface onClick {
            void on(View view);
        }
    }
}
