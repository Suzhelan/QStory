package lin.xposed.Initialize.ProxyTool;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import lin.xposed.ReflectUtils.ClassUtils;

import java.util.Objects;

public class BaseProxyActivity extends AppCompatActivity {

    private ClassLoader baseClassLoader = null;

    @Override
    public ClassLoader getClassLoader() {
        if (baseClassLoader == null) {
            baseClassLoader = new SavedInstanceStatePatchedClassReferencer(
                    BaseProxyActivity.class.getClassLoader());
        }
        return baseClassLoader;
    }

    public static class SavedInstanceStatePatchedClassReferencer extends ClassLoader {
        private static final ClassLoader mBootstrap = Context.class.getClassLoader();
        private final ClassLoader mBaseReferencer;
        private final ClassLoader mHostReferencer;

        public SavedInstanceStatePatchedClassReferencer(ClassLoader referencer) {
            super(mBootstrap);
            mBaseReferencer = Objects.requireNonNull(referencer);
            mHostReferencer = ClassUtils.getHostLoader();
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            try {
                return mBootstrap.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
            if (mHostReferencer != null) {
                try {
                    //开始重载
                    if ("androidx.lifecycle.ReportFragment".equals(name)) {
                        return mHostReferencer.loadClass(name);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
            return mBaseReferencer.loadClass(name);
        }
    }
}
