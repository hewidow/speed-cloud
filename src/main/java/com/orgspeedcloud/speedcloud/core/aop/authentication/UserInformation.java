package com.orgspeedcloud.speedcloud.core.aop.authentication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于给控制层注入用户信息,给需要登录才能访问的接口使用
 * @author Chen
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UserInformation {
}
