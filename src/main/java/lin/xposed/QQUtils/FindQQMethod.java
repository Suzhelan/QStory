package lin.xposed.QQUtils;

import android.view.View;

import java.lang.reflect.Method;

public class FindQQMethod {
    public static Method findMessageMenu(Class clz) {
        for (Method med : clz.getDeclaredMethods()) {
            if (med.getParameterTypes().length == 1) {
                if (med.getParameterTypes()[0] == View.class) {
                    Class ReturnClz = med.getReturnType();
                    if (ReturnClz.isArray()) {
                        return med;
                    }
                }
            }
        }
        return null;
    }
}
