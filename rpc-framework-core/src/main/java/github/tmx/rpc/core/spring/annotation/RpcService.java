package github.tmx.rpc.core.spring.annotation;

import java.lang.annotation.*;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 15:14
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface RpcService {

    String group() default "test";

    String version() default "v1.0.0";
}
