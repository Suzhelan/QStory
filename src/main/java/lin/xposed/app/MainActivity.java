package lin.xposed.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.R;
import lin.xposed.Utils.UpdateTool;


public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static Button updateButton;

    public static Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            updateView uV = (updateView) msg.obj;
            uV.start();
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main_app_layout);
        makeStatusBarTransparent(this);
        try {
            HostEnv.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
        RegisterJoinGroupLayout();
        updateIconAndVersion();
        initUpdateView();
        CheckBox box = findViewById(R.id.hideIcon);
        PackageManager pm = getPackageManager();
        ComponentName commonIcon = new ComponentName(this, "lin.xposed.app.MainActivity");
        ComponentName Hide = new ComponentName(this, "lin.xposed.app.MainActivity.hide");
        box.setChecked(pm.getComponentEnabledSetting(Hide) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
        box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                pm.setComponentEnabledSetting(commonIcon, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(Hide, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            } else {
                pm.setComponentEnabledSetting(commonIcon, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(Hide, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        });

    }


    public static void makeStatusBarTransparent(Activity activity) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int option = window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        window.getDecorView().setSystemUiVisibility(option);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    /**
     * 根据包名获取App的Icon
     *
     * @param pkgName 包名
     */
    public static Drawable getAppIcon(Context context, String pkgName) {
        try {
            if (null != pkgName) {
                PackageManager pm = context.getPackageManager();
                ApplicationInfo info = pm.getApplicationInfo(pkgName, 0);
                return info.loadIcon(pm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initUpdateView() {
        RelativeLayout updateLayout = findViewById(R.id.main_update);
        updateButton = findViewById(R.id.startUpdate);
        updateLayout.setOnClickListener(v -> {
            try {
                CommonTool.Toasts("向服务器查询...");
                UpdateTool.update(MainActivity.this);
            } catch (Exception e) {
                CommonTool.Toasts("Error " + e);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateIconAndVersion() {
        ImageView qq_icon = findViewById(R.id.qq_icon);
        qq_icon.setImageDrawable(getAppIcon(this, HostEnv.packageName));
        TextView qq_Version = findViewById(R.id.QQ_Version);
        qq_Version.setText(qq_Version.getText().toString() + " " + HostEnv.QQVersionName);
        ImageView module_icon = findViewById(R.id.module_icon);
        module_icon.setImageDrawable(getAppIcon(this, this.getString(R.string.module_packageName)));
        TextView module_version = findViewById(R.id.module_Version);
        module_version.setText(module_version.getText().toString() + " " + HostEnv.moduleVersionName);
    }

    public interface updateView {
        void start();
    }

    private void RegisterJoinGroupLayout() {
        LinearLayout layout = findViewById(R.id.main_add_tg_1);
        layout.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content = Uri.parse("https://t.me/WhenFlowersAreInBloom");
            intent.setData(content);
            this.startActivity(intent);
        });
        LinearLayout layout2 = findViewById(R.id.main_add_tg_2);
        layout2.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content = Uri.parse("https://t.me/AnQChat");
            intent.setData(content);
            this.startActivity(intent);
        });
    }

    private boolean isActivation() {

        return false;
    }

}