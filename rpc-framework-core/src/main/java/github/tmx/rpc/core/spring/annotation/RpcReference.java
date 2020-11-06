package github.tmx.rpc.core.spring.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: TangMinXuan
 * @created: 2020/10/21 09:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
@Component
public @interface RpcReference {

    String group() default "test";

    String version() default "v1.0.0";
}
