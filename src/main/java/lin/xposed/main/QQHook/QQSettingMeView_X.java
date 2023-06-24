package lin.xposed.main.QQHook;

import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.XPOperate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;

import java.lang.reflect.Field;

public class QQSettingMeView_X {

    public UIInfo getUi() {
        UIInfo ui = new UIInfo();
        ui.groupType = "净化";
        ui.name = "移除主页侧滑栏右上角返回图标";
        ui.info = "重启生效";
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod("hook",
                MethodUtils.findMethod("com.tencent.mobileqq.activity.QQSettingMe",
                        null, ClassUtils.getClass("com.tencent.mobileqq.activity.BaseQQSettingMeView"),
                        new Class[]{boolean.class}));
    }


    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction action() {
        return param -> {
            //获取返回对象
            Object BaseSettingMeView = param.getResult();
            //获取返回对象的父类
            Class<?> au = BaseSettingMeView.getClass().getSuperclass().getSuperclass();

            Field viewGroupField = null;
            for (Field field : au.getDeclaredFields()) {
                field.setAccessible(true);
                //判断类型符合ViewGroup
                if (field.getType() == ViewGroup.class) {
                    viewGroupField = field;
                    break;
                }
            }
//            ViewGroup viewGroup = FieIdUtils.getFirstField(BaseSettingMeView, au, ViewGroup.class);
            ViewGroup viewGroup = (ViewGroup) viewGroupField.get(BaseSettingMeView);
//            viewGroup.setVisibility(View.GONE);

            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof FrameLayout) {
                    FrameLayout backLayout = (FrameLayout) view;
                    if (backLayout.getChildCount() == 1) {
                        if (backLayout.getChildAt(0) instanceof ImageView) {
                            viewGroup.removeView(backLayout);
                            break;
                        }
                    }
                }
            }

        };

    }
}
