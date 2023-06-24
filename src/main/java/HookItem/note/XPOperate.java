package HookItem.note;

import de.robv.android.xposed.XC_MethodHook;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XPOperate {
    int Before = 1;
    int After = 1 << 1;
    int Replace = 3;

    String ID();

    int period() default Before;

    //优先级
    int hook_period() default XC_MethodHook.PRIORITY_DEFAULT;
}
