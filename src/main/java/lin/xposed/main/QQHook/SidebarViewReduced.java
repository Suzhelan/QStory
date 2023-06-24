package lin.xposed.main.QQHook;

import ConfigTool.ListConfig;
import ConfigTool.SimpleConfig;
import HookItem.LoadItemInfo.MethodContainer;
import HookItem.LoadItemInfo.MethodFindBuilder;
import HookItem.LoadItemInfo.Template.HookAction;
import HookItem.loadHook.UIInfo;
import HookItem.note.QQVersion;
import HookItem.note.XPOperate;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.ItemInfo.ClassifyMenu;
import lin.xposed.LayoutView.MenuLayout;
import lin.xposed.LayoutView.ShapeList.CheckBoxDialog;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.LogUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class SidebarViewReduced {
    private final static String TAG = "侧滑栏精简";
    private final String dataPath = this.getClass().getSimpleName();
    private final LinkedHashSet<String> allCacheTitle = new LinkedHashSet<>();
    private final Set<String> data = ListConfig.getSet(dataPath);
    private final SimpleConfig config = new SimpleConfig(TAG);
    private final ArrayList<Object> ResultArray = new ArrayList<>();
    private final String baseMenu = config.getString("titleInfoClass");
    private Class<?> titleInfoClass = baseMenu.equals("") ? null : ClassUtils.getClass(baseMenu);
    Class<?> SettingMeBizBean =
            ClassUtils.getClass("com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean");
    private LinearLayout menuLayout;
    private String titleFieldName = config.getString("titleFieldName");

    public UIInfo getUi() {
        HookItem.loadHook.UIInfo ui = new UIInfo();
        ui.name = "侧边栏精简";
        ui.info = "点击设置";
        ui.groupType = ClassifyMenu.streamline;
        ui.onClick = v -> {
            try {
                Context context = v.getContext();
//                    dialog.setTitle("需要精简的栏目",null);
                String[] viewNameList = allCacheTitle.toArray(new String[0]);
                if (viewNameList.length == 0) {
                    CommonTool.Toast("没有获取到侧边栏列表 可能是被启用了新版侧边栏\n请开启此功能后重启QQ再次点击此处即可正常设置精简侧边栏项目");
                    return;
                }
                boolean[] Checked = new boolean[viewNameList.length];
                CheckBoxDialog.OnCheckedChange[] changes = new CheckBoxDialog.OnCheckedChange[viewNameList.length];
                for (int i = 0; i < viewNameList.length; i++) {
                    String name = viewNameList[i];
                    changes[i] = (name1, Checked1) -> {
                        if (Checked1) {
                            data.add(name1);
                        } else {
                            data.remove(name1);
                        }
                    };
                    if (data.contains(name)) {
                        Checked[i] = true;
                    }
                }
                CheckBoxDialog dialog = new CheckBoxDialog(context);
                dialog.initView(viewNameList, Checked, changes);
                dialog.setOnDismissListener(dialog1 -> {
                    ListConfig.setSetToFile(dataPath, data);
                    CommonTool.Toast("已保存侧滑栏精简项目 重启生效");
                    MenuLayout.isShow = false;
                    MenuLayout.newDialog(MenuLayout.thisGroupName);
                });
                dialog.show();
                MenuLayout.dialog.dismiss();
                MenuLayout.isShow = true;

            } catch (Exception e) {
                LogUtils.addRunLog(TAG, e);
            }
        };
        return ui;
    }

    public void getMethod(MethodContainer container) {
        container.addMethod(MethodFindBuilder.newFindMethodByName("hook",
                "parse() group == null || group.length() == 0", m -> {
            for (Method m1 : m.getDeclaringClass().getDeclaredMethods()) {
                if (m1.getReturnType().isArray()) {
                    m1.setAccessible(true);
                    return m1;
                }
            }
            return null;
        }));

        String methodName = "e";
        if (HostEnv.QQVersion < QQVersion.QQ_8_9_35)
            methodName = "f";
        container.addMethod("hook_1", MethodUtils.findNoParamsMethod("com.tencent.mobileqq.activity.qqsettingme.utils.a", methodName, boolean.class));
    }

    @XPOperate(ID = "hook_1")
    public HookAction hook_1() {
        return param -> {
            //禁用新样式侧滑栏
            param.setResult(false);
        };
    }

    private void findTitleDeclaringClass(Object item) throws IllegalAccessException {
        if (titleInfoClass == null || titleFieldName.equals("")) {
            //获取到item Name(title) 的内部声明类和这个字段名
            for (Field f : item.getClass().getDeclaredFields()) {
                f.setAccessible(true);

                if (!f.getType().getName().startsWith("com.tencent.mobileqq.activity.qqsettingme.config.QQSettingMeBizBean")) {
                    continue;
                }
                Object runTimeObject = f.get(item);
                Class<?> clz = runTimeObject.getClass();
                int stringNum = 0;
                for (Field field : clz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getType().equals(String.class)) {
                        stringNum++;
                        if (((String) field.get(runTimeObject)).matches("[\\u4E00-\\u9FFF]+")) {
                            titleFieldName = field.getName();
                        }
                    }
                }
                if (stringNum == 2) {
                    titleInfoClass = clz;
                    config.putString("titleFieldName", titleFieldName);
                    config.putString("titleInfoClass", titleInfoClass.getName());
                    LogUtils.addRunLog(TAG, "已找到指定字段名称和类 侧滑栏初始化完成");
                    return;
                }
            }
            LogUtils.addRunLog(TAG, "侧滑栏的类和字段信息没有成功初始化");
            titleFieldName = null;
        }
    }

    @XPOperate(ID = "hook", period = XPOperate.After)
    public HookAction hook() {
        return param -> {
            Object ItemList = param.getResult();
            Object item = Array.get(ItemList, 1);
            findTitleDeclaringClass(item);
            for (int i = 1; i < Array.getLength(ItemList); i++) {
                Object item2 = Array.get(ItemList, i);
                Object runTimeObject = FieIdUtils.getFirstField(item2, titleInfoClass);
                String title = FieIdUtils.getField(runTimeObject, titleFieldName, String.class);
                allCacheTitle.add(title);
                if (!data.contains(title)) ResultArray.add(item2);
            }

            Object newItemList = Array.newInstance(ItemList.getClass().getComponentType(), ResultArray.size());
            for (int i = 0; i < Array.getLength(newItemList); i++) {
                Array.set(newItemList, i, ResultArray.get(i));
            }
            param.setResult(newItemList);
        };
    }


    private void gatherLayoutInfoAndRemoveView() {
        if (menuLayout != null) {
            //循环侧滑栏的Item列表
            itemFor:
            for (int i = 0; i < menuLayout.getChildCount(); i++) {
                View item = menuLayout.getChildAt(i);
                menuLayout.removeView(item);
                //循环这个Item信息
                if (item.getClass().equals(LinearLayout.class)) {
                    LinearLayout layout = (LinearLayout) item;
                    for (int j = 0; j < layout.getChildCount(); j++) {
                        View v = layout.getChildAt(i);
                        //获取文字显示信息
                        if (v instanceof TextView) {
                            String itemName = ((TextView) v).getText().toString();
//                            viewList.put(itemName, item);
                            if (data.contains(itemName)) {
                                menuLayout.removeView(item);
                            }
                            continue itemFor;
                        }
                    }
                }
            }
        }
    }

}
