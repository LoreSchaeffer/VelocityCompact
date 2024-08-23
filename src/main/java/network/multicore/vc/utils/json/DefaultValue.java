package network.multicore.vc.utils.json;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DefaultValue {

    String s() default "";

    int i() default 0;

    long l() default 0L;

    double d() default 0.0;

    boolean b() default false;

    String[] a() default {};
}
