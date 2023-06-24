package lin.xposed.Initialize.ProxyTool;

public class ProxyUtil {
    public static boolean isModuleProxyActivity(String actClassName) {
        return actClassName.startsWith("lin.xposed");
    }
}
