package DexFinder;

import android.annotation.SuppressLint;
import android.provider.Settings;
import lin.xposed.HostEnv;
import lin.xposed.Utils.DataUtils;
import lin.xposed.Utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SoLoader {
    @SuppressLint("UnsafeDynamicallyLoadedCode")
    public static void loadByName(String name) {
        String cachePath = HostEnv.context.getCacheDir() + "/" + DataUtils.getStrMD5(Settings.Secure.ANDROID_ID + "_").substring(0, 8) + "/";
        String tempName = String.valueOf(name.hashCode());
        FileUtils.deleteFile(new File(cachePath + tempName));
        outputLibToCache(cachePath + tempName, name);
        System.load(cachePath + tempName);
    }

    //输出自带的so到qq目录 data/user/qq/cache/MD5/string-哈希码
    private static void outputLibToCache(String cachePath, String name) {
        String apkPath = HostEnv.modulePath;
        try {
            ZipInputStream zInp = new ZipInputStream(new FileInputStream(apkPath));
            ZipEntry entry;
            while ((entry = zInp.getNextEntry()) != null) {
                if (android.os.Process.is64Bit() && entry.getName().startsWith("lib/arm64-v8a/" + name)) {
                    FileUtils.WriteToFile(cachePath, DataUtils.readAllBytes(zInp));
                    break;
                } else if (!android.os.Process.is64Bit() && entry.getName().startsWith("lib/armeabi-v7a/" + name)) {
                    FileUtils.WriteToFile(cachePath, DataUtils.readAllBytes(zInp));
                    break;
                }
            }
        } catch (Exception ignored) {

        }

    }
}
