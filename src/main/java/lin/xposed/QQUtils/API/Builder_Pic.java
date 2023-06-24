package lin.xposed.QQUtils.API;

import HookItem.note.ApiMark;
import lin.xposed.HostEnv;
import lin.xposed.ReflectUtils.ClassUtils;
import lin.xposed.ReflectUtils.FieIdUtils;
import lin.xposed.ReflectUtils.MethodUtils;
import lin.xposed.Utils.DataUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;

//构建图片对象
public class Builder_Pic {
    @ApiMark
    public Object buider(Object session, String path) throws Exception {
        Method CallMethod = MethodUtils.findMethod(ClassUtils.getClass("com.tencent.mobileqq.activity.ChatActivityFacade"), null, ClassUtils.getClass("com.tencent.mobileqq.data.ChatMessage"), new Class[]{
                ClassUtils.getClass("com.tencent.mobileqq.app.QQAppInterface"),
                ClassUtils.getClass("com.tencent.mobileqq.activity.aio.SessionInfo"),
                String.class
        });
        Object PICMsg = CallMethod.invoke(null,
                HostEnv.AppInterface, session, path
        );
        FieIdUtils.setField(PICMsg, "md5", DataUtils.getFileMD5(new File(path)));
        FieIdUtils.setField(PICMsg, "uuid", DataUtils.getFileMD5(new File(path)) + ".jpg");
        FieIdUtils.setField(PICMsg, "localUUID", UUID.randomUUID().toString());
        MethodUtils.callNoParamsMethod(PICMsg, "prewrite", void.class);
        return PICMsg;
    }
}
