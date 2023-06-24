package ConfigTool;

import lin.xposed.Initialize.PathInit;
import lin.xposed.Utils.LogUtils;
import lin.xposed.Utils.MFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ListConfig {
    public static List<String> getList(String FileName) {
        try {
            return (List<String>) MFileUtils.readFileObject(new File(PathInit.paths[1] + FileName));
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static void setListToFile(String filePath, List<String> list) {
        try {
            MFileUtils.writeObjectToFile(new File(PathInit.paths[1] + filePath), list);
        } catch (IOException e) {
            LogUtils.addError(e);
        }
    }

    public static Map<?, ?> getMap(String FileName) {
        try {
            return (Map<?, ?>) MFileUtils.readFileObject(new File(PathInit.paths[1] + FileName));
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    public static void setMapToFile(String filePath, Map<?, ?> map) {
        try {
            MFileUtils.writeObjectToFile(new File(PathInit.paths[1] + filePath), map);
        } catch (IOException e) {
            LogUtils.addError(e);
        }
    }

    public static Set<String> getSet(String FileName) {
        try {
            return (Set<String>) MFileUtils.readFileObject(new File(PathInit.paths[1] + FileName));
        } catch (Exception ex) {
            return new HashSet<>();
        }
    }

    public static void setSetToFile(String filePath, Set<String> set) {
        try {
            MFileUtils.writeObjectToFile(new File(PathInit.paths[1] + filePath), set);
        } catch (IOException e) {
            LogUtils.addError(e);
        }
    }


}
