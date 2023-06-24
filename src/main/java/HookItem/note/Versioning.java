package HookItem.note;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)

public @interface Versioning {
    int targetVer() default -1;

    int max_targetVer() default -1;

}
