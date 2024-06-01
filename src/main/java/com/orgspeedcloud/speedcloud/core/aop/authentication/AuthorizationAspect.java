package com.orgspeedcloud.speedcloud.core.aop.authentication;

import com.orgspeedcloud.speedcloud.core.DTO.ResponseDTO;
import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.util.RedisUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 鉴权切面
 * @author Chen
 */
@Aspect
@Component
public class AuthorizationAspect {
    private static final String USER_DETAIL_NAME = "user";

    /**
     * 切入点,切入注解
     */
    @Pointcut(value = "@annotation(com.orgspeedcloud.speedcloud.core.aop.authentication.RequireRoles)")
    public void authorizationPointCut(){}

    @Before(value = "authorizationPointCut()")
    public void doAuthorization(JoinPoint joinPoint){
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取需要的角色名称
        RequireRoles requireRoles = method.getAnnotation(RequireRoles.class);
        String[] roles = requireRoles.roles();

        String[] parameterNames = signature.getParameterNames();
        int argIndex = ArrayUtils.indexOf(parameterNames, USER_DETAIL_NAME);
        UserDetail user = (UserDetail)joinPoint.getArgs()[argIndex];

        String roleName = user.getRoleName();

        boolean match = false;
        for(String role : roles){
            if(role.equals(roleName)){
                match = true;
                break;
            }
        }
        if(!match){
            throw new AuthorizationException();
        }
    }
}
