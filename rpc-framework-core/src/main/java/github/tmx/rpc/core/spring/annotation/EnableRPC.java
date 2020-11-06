package github.tmx.rpc.core.spring.annotation;

import github.tmx.rpc.core.spring.RpcBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author: TangMinXuan
 * @created: 2020/10/20 11:48
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RpcBeanRegistrar.class)
public @interface EnableRPC {

    String[] basePackage() default {};

}
