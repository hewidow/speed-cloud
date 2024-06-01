package com.orgspeedcloud.speedcloud.core.aop.authentication;

/**
 * 用户授权信息错误
 * @author Chen
 */
public class AuthorizationException extends RuntimeException{
    @Override
    public String getMessage() {
        return "您无权访问该接口";
    }
}
