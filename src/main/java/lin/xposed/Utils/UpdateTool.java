package lin.xposed.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.LayoutView.ShapeList.mDialog;
import lin.xposed.R;
import lin.xposed.app.MainActivity;
import org.json.JSONObject;

public class UpdateTool {


    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 10000) {
            //1000毫秒内按钮无效，这样可以控制快速点击，自己调整频率
            return true;
        }
        lastClickTime = time;
        return false;
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    public static void update(Context context) throws Exception {
        new Thread(() -> {
            try {
                if (isFastDoubleClick()) {
                    CommonTool.Toasts("这么快...会坏掉的");
                    return;
                }
                JSONObject info = DetectUpdates();
                int newVersion = info.getInt("newVersion");
                boolean isUpdate = info.getBoolean("isUpdate");
                String updateUrl = info.getString("url");
                String log = info.getString("log");
                if (isUpdate || HostEnv.moduleVersion < newVersion) {
                    /*
                     * handler post也可以不过我需要巩固一下用法知识
                     */
                    /*Handler handler = new Handler(Looper.getMainLooper()) {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                        }
                    };*/

                    View.OnClickListener onClickListener = v -> {
                        Intent intent1 = new Intent();
                        intent1.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://linl.top/getNewApk");
                        intent1.setData(content_url);
                        context.startActivity(intent1);
                    };
                    if (context.getClass().equals(MainActivity.class)) {
                        Message message = new Message();
                        message.obj = (MainActivity.updateView) () -> {
                            MainActivity.updateButton.setVisibility(View.VISIBLE);
                            MainActivity.updateButton.setOnClickListener(onClickListener);
                            RelativeLayout layout = (RelativeLayout) MainActivity.updateButton.getParent();
                            layout.setBackground(context.getDrawable(R.drawable.button_shape));
                        };
                        MainActivity.handler.sendMessage(message);
                        return;
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        @SuppressLint("InflateParams")
                        RelativeLayout root = (RelativeLayout) LayoutInflater.from(context).
                                inflate(R.layout.update_new_version, null, false);
                        TextView newVersionTextView = root.findViewById(R.id.newVersion);
                        newVersionTextView.setText("版本号 : " + newVersion);
                        Button button = root.findViewById(R.id.start_update);
                        button.setOnClickListener(onClickListener);
                        TextView updateLogTextView = root.findViewById(R.id.UpdateLog);
                        updateLogTextView.setText(log);
                        mDialog dialog = new mDialog(context);
                        dialog.setContentView(root);
                        dialog.show();
                    });
                } else {
                    CommonTool.Toasts("当前已是最新版");
                }
            } catch (Exception e) {
                CommonTool.Toasts("异常 \n" + e);
            }
        }).start();

    }


    private static JSONObject DetectUpdates() {
        String url = "https://linl.top/update";
        JSONObject results = null;
        // 开始时间
        long stime = System.currentTimeMillis();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version", HostEnv.moduleVersion);
            String result = HttpUtils.sendPost(url, jsonObject);
            results = new JSONObject(result);
            long etime = System.currentTimeMillis();
            // 计算执行时间
            CommonTool.Toasts("请求耗时 " + (etime - stime) + "ms");
        } catch (Exception e) {
            CommonTool.Toasts("服务器连接异常 " + e);
        }
        return results;
    }
}
