package com.orgspeedcloud.speedcloud.core.aop.operationlog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

/**
 * 操作日志切面类
 * @author Chen
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    private final static String METHOD_GET = "GET";
    ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<>();
    ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Pointcut(value = "@annotation(com.orgspeedcloud.speedcloud.core.aop.operationlog.OperationLog)")
    public void logPointcut(){}

    @Around(value = "logPointcut()")
    public Object doAroundLog(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        requestThreadLocal.set(request);
        startTime.set(System.currentTimeMillis());
        log.info("---------------------------->");
        log.info("request start: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        processRequestHeader();
        processRequestParams(joinPoint);
        Object result = null;
        result = joinPoint.proceed();
        if (result != null) {
            //响应结果
            log.info("request result: " + JSONObject.toJSONString(result));
        }
        log.info("request spent time: " + (System.currentTimeMillis() - startTime.get()));
        startTime.remove();
        return result;
    }
    private void processRequestParams(ProceedingJoinPoint point) {
        String params = null;
        HttpServletRequest request = requestThreadLocal.get();
        String url = request.getRequestURI();
        String method = request.getMethod();
        //如果是get请求
        if (METHOD_GET.equalsIgnoreCase(method)) {
            params = request.getQueryString();
        } else if (!checkFileUpload()) {
            Object[] args = point.getArgs();
            params = JSONArray.toJSONString(args);
        } else {
            params = "";
        }
        log.info("request params: url:{},method:{},params:{}", url, method, params);
    }

    /**
     * 判断是否是文件上传
     *
     * @return
     */
    private boolean checkFileUpload() {
        return ServletFileUpload.isMultipartContent(requestThreadLocal.get());
    }

    /**
     * 处理请求头信息
     */
    private void processRequestHeader() {
        JSONObject jsonObject = new JSONObject();
        HttpServletRequest request = requestThreadLocal.get();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            jsonObject.put(key, value);
        }
        log.info("request header: " + jsonObject.toString());
    }
}
