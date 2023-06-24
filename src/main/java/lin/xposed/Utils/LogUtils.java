package lin.xposed.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.QQUtils.QQEnvUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

/**
 * 其实有待优化 当初设计没考虑这么多 现在也懒得优化了
 */
public class LogUtils {

    private static String RunLogFilePath;
    private static String ErrorLogFilePath;

    public static String getTime() {
        PathInit.isFileComplete();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy年MM月dd日"),
                df2 = new SimpleDateFormat("E", Locale.CHINA),
                df3 = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        String TimeMsg1 = df1.format(calendar.getTime()),
                TimeMsg2 = df2.format(calendar.getTime()),
                TimeMsg3 = df3.format(calendar.getTime());
        if (TimeMsg1.contains("年0"))//去掉多余的 0
            TimeMsg1 = TimeMsg1.replace("年0", "年");
        if (TimeMsg1.contains("月0"))
            TimeMsg1 = TimeMsg1.replace("月0", "月");
        if (TimeMsg2.contains("周"))
            TimeMsg2 = TimeMsg2.replace("周", "星期");//转换为星期
        return TimeMsg1 + TimeMsg2 + TimeMsg3;
    }

    public static void addRunLog(final Object obj) {
        try {
            new Thread(() -> {
                String text = "";
                if (obj instanceof Throwable) {
                    text = "\n" + Log.getStackTraceString((Throwable) obj);
                }
                MFileUtils.writeTextToFile(PathInit.paths[0] + "Run logs.txt",
                        getTime() +
                                "\n" +
                                obj +
                                text +
                                "\n\n",
                        true);
            }).start();
        } catch (Exception e) {
            addError(e);
        }
    }

    public static void addRunLog(String TAG, Object content) {
        new Thread(() -> {
            MFileUtils.writeTextToFile(PathInit.paths[0] + TAG + ".txt",
                    getTime() + "\n" + content.toString() + "\n\n",
                    true);
        }).start();
    }

    public static void addRunLog(String TAG, Throwable content) {
        new Thread(() -> {
            MFileUtils.writeTextToFile(PathInit.paths[0] + TAG + ".txt",
                    getTime() + "\n" + Log.getStackTraceString(content) + "\n\n",
                    true);
        }).start();
    }

    public static void addRunLog(Object obj, Throwable throwable) {
        new Thread(() -> {
            String Stack = Log.getStackTraceString(throwable);
            MFileUtils.writeTextToFile(PathInit.paths[0] + "Run logs.txt",
                    getTime() + "\n" + obj.toString() + " StackTrace: " + Stack + "\n",
                    true);
        }).start();
    }

    public static void addError(Object obj) {
        new Thread(() -> {
            String text = "";
            if (obj instanceof Throwable) {
                text = "\n" + Log.getStackTraceString((Throwable) obj);
            }
            MFileUtils.writeTextToFile(PathInit.paths[0] + "Error logs.txt",
                    getTime() +
                            "\n" +
                            obj +
                            text +
                            "\n\n",
                    true);
        }).start();
    }

    public static void addError(Object obj, Throwable throwable) {
        new Thread(() -> {
            String Stack = Log.getStackTraceString(throwable);
            MFileUtils.writeTextToFile(PathInit.paths[0] + "Error logs.txt",
                    getTime() + "\n" + obj + " StackTrace: " + Stack + "\n",
                    true);
        }).start();
    }


    //将所有日志封成压缩包发给后端
    public static void OutputBaseInfo() {

    }

    public static void sendLog(File file) {

    }

    public static StringBuffer OutputDeviceInfo(boolean isOutput) {
        Context context = CommonTool.getActivity();
        JSONArray jsonArray = new JSONArray();
        //收集不需要上下文的固定设备信息
        for (Method m : DeviceInfoUtils.class.getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.getParameterTypes().length != 0) continue;
            try {
                jsonArray.put(new JSONObject().put(m.getName().replace("get", ""), m.invoke(null)));
            } catch (Exception e) {
                LogUtils.addError(e);
            }
        }

        //收集hook的环境信息
        JSONArray j2 = new JSONArray();
        for (Field f : HostEnv.class.getDeclaredFields()) {
            f.setAccessible(true);
            try {
                Object field = f.get(null);
                if (field != null)
                    j2.put(new JSONObject().put(f.getName(), field.toString()));
            } catch (Exception e) {
                LogUtils.addError(e);
            }
        }
        try {
            j2.put(new JSONObject().put("RunTimeUin", QQEnvUtils.getCurrentUin()));
        } catch (JSONException e) {

        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Device", jsonArray);
            jsonObject.put("Env", j2);
        } catch (JSONException e) {

        }
        MFileUtils.writeTextToFile(PathInit.paths[0] + "sendEnvInfo.json", jsonObject.toString(), false);
        StringBuffer info = new StringBuffer();
        info.append("Device\n").append("--------------------------------------------------------------\n");
        try {
            JSONArray Device = (JSONArray) jsonObject.get("Device");
            for (int i = 0; i < Device.length(); i++) {
                JSONObject j = (JSONObject) Device.get(i);
                Iterator<String> iterator = j.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    info.append(key).append(" : ").append(j.get(key));
                }
                info.append("\n");
            }
        } catch (JSONException e) {
        }
        info.append("--------------------------------------------------------------\n");

        //收集环境信息
        info.append("Env\n").append("--------------------------------------------------------------\n");
        try {
            JSONArray Env = (JSONArray) jsonObject.get("Env");
            for (int i = 0; i < Env.length(); i++) {
                JSONObject j = (JSONObject) Env.get(i);
                Iterator<String> iterator = j.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    info.append(key).append(" : ").append(j.get(key));
                }
                info.append("\n");
            }
        } catch (JSONException e) {
        }
        info.append("--------------------------------------------------------------\n");
        return info;
    }

}
