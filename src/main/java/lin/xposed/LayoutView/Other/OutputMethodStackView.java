package lin.xposed.LayoutView.Other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HookUtils.XPBridge;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

public class OutputMethodStackView {
    @SuppressLint("SetTextI18n")
    public static void startShow() {
        MenuLayout.isShow = true;
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(new BasicBackground().setAlpha(1d));

        @SuppressLint("InflateParams")
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_edittext, null);
        TextView textView = layout.findViewById(R.id.common_edittext_title);
        textView.setText("通过方法运行时对象动态反射属性和调用栈\n会输出到模块的日志目录");
        EditText ClassEditText = layout.findViewById(R.id.base_editText);
        ClassEditText.setHint("反射的类 内部类用$相隔");
        ClassEditText.setText("");
        @SuppressLint("InflateParams") LinearLayout layout2 = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.base_edit_text, null);
        EditText methodInfo = layout2.findViewById(R.id.base_editText);
        methodInfo.setText("");
        methodInfo.setHint("方法格式 : 修饰符(不必要) 返回参数(非必要) 方法名称(方法参数,方法参数);");
        layout.addView(layout2, 2);
        Button close = layout.findViewById(R.id.button2_close);
        close.setOnClickListener(view -> dialog.dismiss());
        Button yesSave = layout.findViewById(R.id.button2_yes);
        yesSave.setText("开始");
        yesSave.setOnClickListener(view -> {
            Method m = parseMethod(ClassEditText.getText().toString(), methodInfo.getText().toString());
            if (m == null) {
                CommonTool.Toast("没有查找到方法");
                return;
            }
            CommonTool.Toast("查找到方法 开始在重启QQ前一直记录");
            XPBridge.hookAfter(m, OutputHookStack::OutputMethodStack);
            dialog.dismiss();

        });
        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.9, 0.9);
        dialog.setOnDismissListener(dialog1 -> {
            MenuLayout.isShow = false;
            MenuLayout.newDialog(MenuLayout.thisGroupName);
        });
        dialog.show();
        MenuLayout.dialog.dismiss();
    }

    //解析方法参数并查找方法
    private static Method parseMethod(String className, String info) {
        // 提取获取到方法参数列表 避免被下一行的空格分割 因为我写的输出属性列表方法参数列表会有空格
        try {
            String methodParams = info.substring(info.indexOf("(") + 1, info.indexOf(")"));
            String[] paramsList = info.split(" ");
            if (paramsList.length < 2) return null;
            for (int i = 0; i < paramsList.length; i++) {
                //判断到 > 方法名( < 这段内容
                String params = paramsList[i];
                //命中匹配字段 a(
                if (params.trim().matches("^[A-z]+\\(.*")) {
                    Class<?> Return = null;
                    if (i != 0)
                        Return = paramsList[i - 1].matches("void") ? null : ClassUtils.getClass(paramsList[i - 1].trim());
                    //截取名称
                    String name = params.substring(0, params.indexOf("("));
                    //截取方法参数段并去除空格
                    String[] methodParamsList = methodParams.replace(" ", "").split(",");
                    //即使没有有效参数split出来的也很可能是length==1
                    Class<?>[] classes = new Class[methodParamsList[0].equals("") ? 0 : methodParamsList.length];
                    for (int i2 = 0; i2 < classes.length; i2++) {
                        Class<?> type = ClassUtils.getClass(methodParamsList[i2]);
                        if (type == null) {
                            CommonTool.Toast("参数列表类没有找到 : " + methodParamsList[i2]);
                            return null;
                        }
                        classes[i2] = type;
                    }
                    if (Return == null) {
                        LogUtils.addRunLog("调试 : 查找无返回方法 class : " + className + "\nname : " +
                                name + "\n" +
                                "params : " + Arrays.toString(methodParamsList));
                        return MethodUtils.findUnknownReturnMethod(className, name, classes);
                    } else {
                        LogUtils.addRunLog("调试 : 查找有返回方法 class : " + className + "\nname : " +
                                name + "\n" + Return.getName() + "\n" +
                                "params : " + Arrays.toString(methodParamsList));
                        return MethodUtils.findMethod(className, name, Return, classes);
                    }
                }
            }
            CommonTool.Toast("查找到类名 但没有查找到方法");
            return null;
        } catch (Exception e) {
            CommonTool.Toast("方式格式错误" + e);
            return null;
        }
    }


}
