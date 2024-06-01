package com.orgspeedcloud.speedcloud.core.aop.operationlog;

/**
 * 操作日志切点
 * @author Chen
 */
public @interface OperationLog {
    String value() default "";
}
