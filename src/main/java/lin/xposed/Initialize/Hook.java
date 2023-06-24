package lin.xposed.Initialize;


import com.github.kyuubiran.ezxhelper.EzXHelper;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import lin.xposed.HostEnv;
import lin.xposed.ReflectUtils.ClassUtils;

//入口初始化
public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static StartupParam thisstartupParam;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        /*
         * 框架并不是只加载并优先挂qq的
         * 框架可能会先加载两个系统应用扩展和管理程序（即使没有勾选）
         * 然后就可能造成 load别的应用的PackageParam或者多次load宿主应用
         */
        if (!loadPackageParam.isFirstApplication) return;
        if (loadPackageParam.packageName.equals(HostEnv.packageName)) {
            /*
             * 初始化EzXHelper库要求最先且必须初始化
             * 初始化Zygote（qq使用模块的res资源用的）
             */
            EzXHelper.initHandleLoadPackage(loadPackageParam);
            EzXHelper.initZygote(thisstartupParam);

            //类加载器
            ClassUtils.setHostClassLoader(  loadPackageParam.classLoader);
            ClassUtils.setModuleLoader( this.getClass().getClassLoader());

            //data路径
            HostEnv.DataPath = loadPackageParam.appInfo.dataDir;
            //apk路径
            HostEnv.apkPath = loadPackageParam.appInfo.sourceDir;


            InitItemHook.startHookQQ();
        }
    }

    /*
     * 实现跨进程res资源共享
     */
    @Override
    public void initZygote(StartupParam startupParam) {
        HostEnv.modulePath = startupParam.modulePath;
        thisstartupParam = startupParam;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) {
    }
}
