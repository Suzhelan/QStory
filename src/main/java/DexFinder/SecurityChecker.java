package DexFinder;

import android.content.SharedPreferences;
import lin.xposed.HostEnv;

public class SecurityChecker {
    public static int checkLoaderType() {
        SharedPreferences share = HostEnv.context.getSharedPreferences("SecurityLoad", 0);
        return share.getInt("type", 0);
    }

    public static void saveLoaderType(int type) {
        SharedPreferences share = HostEnv.context.getSharedPreferences("SecurityLoad", 0);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt("type", type);
        editor.commit();
    }

    public static boolean isLoading() {
        SharedPreferences share = HostEnv.context.getSharedPreferences("SecurityLoad", 0);
        return share.getBoolean("loading", false);
    }

    public static void savePreload() {
        SharedPreferences share = HostEnv.context.getSharedPreferences("SecurityLoad", 0);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("loading", true);
        editor.commit();
    }

    public static void finishPreload() {
        SharedPreferences share = HostEnv.context.getSharedPreferences("SecurityLoad", 0);
        SharedPreferences.Editor editor = share.edit();
        editor.remove("loading");
        editor.commit();
    }
}
