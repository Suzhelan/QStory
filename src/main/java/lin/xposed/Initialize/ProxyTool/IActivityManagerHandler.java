package lin.xposed.Initialize.ProxyTool;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import lin.xposed.HostEnv;
import lin.xposed.Initialize.ProxyActivityManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * 活动管理器处理器 接口 被调用处理器
 *
 * 因为是被反射注册的 所以重写invoke方法
 */
public class IActivityManagerHandler implements InvocationHandler {
    //原对象 用于
    private final Object mOrigin;

    public IActivityManagerHandler(Object origin) {
        mOrigin = origin;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //只处理startActivity方法
        if ("startActivity".equals(method.getName())) {
            int index = -1;
            for (int i = 0; i < args.length; i++) {
                //查找Intent索引
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                //获取原本的Intent
                Intent raw = (Intent) args[index];
                ComponentName component = raw.getComponent();
                Context hostApp = HostEnv.context;
                if (hostApp != null && component != null
                        //判断是宿主
                        && hostApp.getPackageName().equals(component.getPackageName())
                        //判断是模块的Activity
                        && ProxyUtil.isModuleProxyActivity(component.getClassName())) {
                    //自己的包装
                    Intent wrapper = new Intent();
                    //要代理的宿主的活动类名
                    wrapper.setClassName(component.getPackageName(), ProxyActivityManager.PROXY_CLASS_NAME);
                    //把我们自己的标记和原Intent包装进去
                    wrapper.putExtra(ProxyActivityManager.FLAG, raw);
                    //替换原本的Intent
                    args[index] = wrapper;
                }
            }
        }
        try {
            return method.invoke(mOrigin, args);
        } catch (InvocationTargetException ite) {
            throw ite.getTargetException();
        }
    }
}
