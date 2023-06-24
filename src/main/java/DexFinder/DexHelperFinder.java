package DexFinder;

import lin.xposed.Utils.LogUtils;
import me.iacn.biliroaming.utils.DexHelper;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class DexHelperFinder implements IDexFinder {
    private DexHelper helper;

    @Override
    public void init(String apkPath, ClassLoader loader) {
        if (SecurityChecker.checkLoaderType() == 2) return;
        SoLoader.loadByName("libdexfinder.so");
        SoLoader.loadByName("libdex_builder.so");
        helper = new DexHelper(loader);
    }

    @Override
    public Method[] findMethodByString(String str) {
        if (SecurityChecker.checkLoaderType() == 2) return new Method[0];
        if (str == null) return new Method[0];
        long[] dexIndexes = helper.findMethodUsingString(str, false, -1, (short) -1, null, -1,
                null, null, null, false);
        ArrayList<Method> retArr = new ArrayList<>();
        for (long index : dexIndexes) {
            Member m = helper.decodeMethodIndex(index);
            if (m instanceof Method) {
                retArr.add((Method) m);
            }
        }
        return retArr.toArray(new Method[0]);
    }

    @Override
    public Method[] findMethodBeInvoked(Method beInvoked) {
        if (SecurityChecker.checkLoaderType() == 2) return new Method[0];
        if (beInvoked == null) return new Method[0];
        long[] dexIndexes = helper.findMethodInvoked(helper.encodeMethodIndex(beInvoked), -1, (short) -1, null, -1, null,
                null, null, false);
        ArrayList<Method> retArr = new ArrayList<>();
        for (long index : dexIndexes) {
            try {
                Member m = helper.decodeMethodIndex(index);
                if (m instanceof Method) {
                    retArr.add((Method) m);
                }
            } catch (Exception e) {
                LogUtils.addError(e);
            }
        }
        return retArr.toArray(new Method[0]);
    }

    @Override
    public Method[] findMethodInvoking(Method beInvoked) {
        if (SecurityChecker.checkLoaderType() == 2) return new Method[0];
        if (beInvoked == null) return new Method[0];
        long[] dexIndexes = helper.findMethodInvoking(helper.encodeMethodIndex(beInvoked), -1, (short) -1, null, -1, null,
                null, null, false);
        ArrayList<Method> retArr = new ArrayList<>();
        for (long index : dexIndexes) {
            Member m = helper.decodeMethodIndex(index);
            if (m instanceof Method) {
                retArr.add((Method) m);
            }
        }
        return retArr.toArray(new Method[0]);
    }
}
