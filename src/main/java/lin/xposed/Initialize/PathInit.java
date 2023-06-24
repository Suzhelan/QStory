package lin.xposed.Initialize;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.ReflectUtils.PostMain;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;


public class PathInit {
    public static final AtomicBoolean StoragePermissions = new AtomicBoolean();
    public static final String storage = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String path = storage + "/QStory/";
    public static final String[] paths = {"日志", "数据", "语音", "表情(没实现,导入也没用)"};
    private static final int REQUEST_EXTERNAL_STORAGE = 2376;//请求权限标识码
    private static final String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",//外部读
            "android.permission.WRITE_EXTERNAL_STORAGE"//外部写
    };
    public static AtomicBoolean IsInitPath = new AtomicBoolean();

    public static void isFileComplete() {
        if (!IsInitPath.getAndSet(true)) {
            File file = new File(path);
            if (!file.exists())
                file.mkdirs();
            File nomedia = new File(path + ".nomedia");
            if (!nomedia.exists()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException e) {

                }
            }
            //文件夹完整性
            for (int i = 0; i < paths.length; i++) {
                File f = new File(path + paths[i]);
                if (!f.exists()) {
                    f.mkdirs();
                }
                paths[i] = f.getAbsolutePath() + "/";//把文件夹名的数组更新成绝对路径
            }
        }
    }

    //申请权限
    public static boolean verifyStoragePermissions(Context context) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(context,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            //没有写的权限，去申请写的权限，会弹出对话框 注意申请权限主线程不会等待权限申请结果 最好注册回调检查或者开一条线程循环判断申请权限结果 然后再检查文件完整性
            if (permission == PackageManager.PERMISSION_GRANTED) {
                StoragePermissions.set(true);
            }
            return permission == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void dialog() {
        Activity act = CommonTool.getActivity();

        if (act == null) return;
        hookPermissionResults(act.getClass());
        mDialog dialog = new mDialog(act);

        LinearLayout layout = new LinearLayout(act);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        BasicBackground background = new BasicBackground();
        background.setColor(CommonTool.getColors(act, R.color.蔷薇色));
        background.setAlpha(1d);
        layout.setBackground(background);
        layout.setPadding(20, 20, 20, 10);

        TextView textView = new TextView(act);
        textView.setTextSize(20);
        textView.setText("QQ没有读写权限导致无法初始化\n接下来将会进行申请读写权限\n授权成功后请强行停止(重启)QQ\n如没有弹出授权弹窗请手动到权限管理授权");
        textView.setTextColor(Color.parseColor("#000000"));
        layout.addView(textView);

        Button button = new Button(act);
        button.setText("确定");
        button.setTextSize(20);
        button.setBackgroundResource(R.drawable.base_shape);
        button.setOnClickListener(view -> {
            ActivityCompat.requestPermissions(act, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            dialog.dismiss();
        });
        layout.addView(button);

        dialog.setContentView(layout);
        dialog.setDialogWindowAttr(0.7f, 0.5f);
        dialog.show();
    }

    private static void hookPermissionResults(Class<?> clz) {
        Method m = MethodUtils.findUnknownReturnMethod(clz.getName(), "onRequestPermissionsResult", new Class[]{int.class, String[].class, int[].class});
        XposedBridge.hookMethod(m, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                int requestCode = (int) param.args[0];
                String[] permissions = (String[]) param.args[1];
                int[] grantResults = (int[]) param.args[2];

                if (requestCode == REQUEST_EXTERNAL_STORAGE) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] != -1) {
                            //权限设置成功
                            CommonTool.Toast("授权成功 请重启QQ 你不重启我帮你重启");
                            PostMain.postMain(CommonTool::killAppProcess, 4000);
                        } else {
                            //没成功
                            CommonTool.Toast("授权可能没有成功");
                        }
                    }
                }
            }
        });
    }
}
