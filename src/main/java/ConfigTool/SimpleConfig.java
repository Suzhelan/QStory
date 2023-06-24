package ConfigTool;

import android.content.SharedPreferences;
import lin.xposed.HostEnv;

public class SimpleConfig {
    private final String FileName;

    public SimpleConfig(String fileName) {
        this.FileName = fileName;
    }

    public void remove(String key) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        SharedPreferences.Editor editor = share.edit();
        editor.remove(key);
        editor.apply();
    }

    public String getString(String key) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        return share.getString(key, "");
    }

    public void putString(String key, String value) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        return share.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean Default) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        return share.getBoolean(key, Default);
    }

    public void putInt(String key, int i) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        SharedPreferences.Editor editor = share.edit();
        editor.putInt(key, i);
        editor.apply();
    }

    public int getInt(String key) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        return share.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        SharedPreferences share = HostEnv.context.getSharedPreferences(FileName, 0);
        return share.getInt(key, defaultValue);
    }
}
