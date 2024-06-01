package com.orgspeedcloud.speedcloud.core.aop.authentication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 为接口做权限验证,添加此注解到接口上,并写上需要的权限等级,示例如下:
 *         RequireRoles(roles = {"admin","superAdmin"})
 *         public String hello()
 *
 *  ps: 使用该注解必须搭配 @UserInformation,否则切入失败
 *
 * @author Chen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequireRoles {
    String[] roles() default {};
}
