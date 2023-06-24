package lin.xposed.LayoutView.Other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Field;

public class EnvInfo {

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n", "ResourceAsColor"})
    public static void createDialog() {
        Context context = CommonTool.getActivity();
        mDialog dialog = new mDialog(context);
        dialog.setBackground(context.getDrawable(R.drawable.main_background));
        ScrollView rootView = new ScrollView(context);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(20, 20, 20, 0);
        BasicBackground base = new BasicBackground();
        base.setAlpha(1d);
        try {
            Field[] fields = ClassUtils.loadClass(ClassUtils.getModuleLoader(),"lin.xposed.HostEnv").getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName();
                String type = field.getType().getSimpleName();
                String v = String.valueOf(field.get(null));
                TextView textView = new TextView(context);
                textView.setTextSize(14);
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setBackground(base);
                textView.setText(type + " " + name + " = " + v);
                layout.addView(textView, getParams());
            }
        } catch (Exception e) {
            LogUtils.addError(e);
        }
        rootView.addView(layout);
        dialog.setContentView(rootView);
        dialog.show();
    }

    private static ViewGroup.LayoutParams getParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(25, 15, 25, 15);
        return params;
    }
}
