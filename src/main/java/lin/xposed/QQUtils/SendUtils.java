package lin.xposed.QQUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.PathInit;
import lin.xposed.QQUtils.API.Builder_Pic;
import lin.xposed.QQUtils.API.FinalApiBuilder;
import lin.xposed.QQUtils.API.MsgApi_sendReply;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.DataUtils;
import lin.xposed.Utils.FileUtils;
import lin.xposed.Utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SendUtils {

    static String TAG = "QQSendUtils";

    public static void sendTextMsg(Object session, String text, ArrayList atList) {
        Method send = MethodUtils.findMethod("com.tencent.mobileqq.activity.ChatActivityFacade", null, void.class, new Class[]{
                ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"),
                Context.class,
                ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"),
                String.class,
                ArrayList.class
        });
        try {
            send.invoke(null, HostEnv.AppInterface, HostEnv.context, session, text, atList);
        } catch (Exception e) {
            LogUtils.addRunLog(TAG, e);
        }
    }

    //通过路径发送图片
    public static void sendByPicPath(Object session, String path) {
        sendPic(HostEnv.SessionInfo, MsgBuilder.builderPic(HostEnv.SessionInfo, MsgBuilder.checkAndGetCastPic(path)));
    }

    //发送对象,图片对象
    public static void sendPic(Object session, Object pic) {
        Method m = MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.activity.ChatActivityFacade"), null, void.class, new Class[]{
                ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"),
                ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"),
                ClassUtils.getClass("com.tencent.mobileqq.data.MessageForPic"),
                int.class});
        if (m == null) LogUtils.addError("sendPic method null");
        try {
            m.invoke(null, HostEnv.AppInterface, session, pic, 0);
        } catch (Exception e) {
            LogUtils.addRunLog(TAG, e);
        }
    }

    public static void sendReplyMsg(Object session, Object msg) {
        FinalApiBuilder.builder(MsgApi_sendReply.class, session, msg);
    }

    public static void sendVoice(Object _Session, String path) {
        try {
            if (!path.contains("com.tencent.mobileqq/Tencent/MobileQQ/" + QQEnvUtils.getCurrentUin())) {
                String newPath = Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.mobileqq/Tencent/MobileQQ/" + QQEnvUtils.getCurrentUin() + "/ptt/" + new File(path).getName();
                FileUtils.copy(path, newPath);
                path = newPath;
            }
            Method CallMethod =
                    MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.activity.ChatActivityFacade"), null, long.class, new Class[]{ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"), ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"), String.class});
            CallMethod.invoke(null, HostEnv.AppInterface, _Session, path);
        } catch (Exception e) {
            LogUtils.addRunLog(TAG, e);
        }
    }

    public static class MsgBuilder {


        //构建要发送的图片消息
        public static Object builderPic(Object session, String path) {
            return FinalApiBuilder.builder(Builder_Pic.class, session, path);
        }

        //如果图片太大压缩后再返回压缩后的图片路径
        private static String checkAndGetCastPic(String Path) {
            File f = new File(Path);
            if (f.exists() && f.length() > 128) {
                try {
                    byte[] buffer = new byte[4];
                    FileInputStream ins = new FileInputStream(f);
                    ins.read(buffer);
                    ins.close();
                    if (buffer[0] == 'R' && buffer[1] == 'I' && buffer[2] == 'F' && buffer[3] == 'F') {
                        Bitmap bitmap = BitmapFactory.decodeFile(Path);
                        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bOut);
                        if (bOut.size() > 128) {
                            String CachePath = PathInit.path + "/Cache/Img_" + DataUtils.getFileMD5(f);
                            FileUtils.WriteToFile(CachePath, bOut.toByteArray());
                            return CachePath;
                        }
                    }
                } catch (Exception e) {

                }

            }
            return Path;
        }
    }
}
