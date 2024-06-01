package com.orgspeedcloud.speedcloud.core.aop.authentication;

import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.UserMapper;
import com.orgspeedcloud.speedcloud.util.RedisUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.annotation.Resource;

/**
 * 该类是一个参数处理器,用于解析入参时带有UserInformation注解的参数,注入访问该处理器的用户信息
 * 配合注解@UserInformation使用,当需要在控制器拿到用户信息时,可以在入参的时候添加如下格式:
 *
 *     public void hello(@UserInformation UserDetail user)
 *
 * 在入参的时候带上UserInformation,会自动根据请求进来的Token去解析出一个UserDetail对象供控制层使用
 * 并且配合切面,做登录/权限控制
 *
 * @author Chen
 */
public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    private final static String AUTHENTICATION_TOKEN = "token";
    private final static String USER_TOKEN_PREFIX = "login-user-token:";
    private final static String REQUEST_COUNT = "request-count:";
    public static final int MAX_REQUEST_COUNT = 50;
    public static final String CHECK_FILE = "/checkFile";
    public static final String CHECK_AGAIN = "/checkAgain";
    public static final String UPLOAD = "/upload";
    @Resource
    RedisUtils redisUtils;
    @Resource
    UserMapper userMapper;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(UserInformation.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        ServletWebRequest request = (ServletWebRequest) webRequest;
        String requestUrl = request.getRequest().getRequestURI();

        String token = webRequest.getHeader(AUTHENTICATION_TOKEN);
        String userId = (String) redisUtils.get(USER_TOKEN_PREFIX + token);
        if(token == null || token.trim().length() == 0 || userId == null){
            throw new AuthorizationException();
        }
        if(!CHECK_FILE.equals(requestUrl) && !CHECK_AGAIN.equals(requestUrl) && !UPLOAD.equals(requestUrl)){
            markRequestCount(Integer.parseInt(userId));
        }
        UserDetail user = userMapper.queryByUserId(Integer.parseInt(userId));
        if(user.getBanned()){
            throw new BannedException();
        }
        return user;
    }
    private void markRequestCount(Integer userId){
        String requestCountKey = REQUEST_COUNT + userId;
        Integer requestCount = (Integer) redisUtils.get(requestCountKey);
        if(requestCount == null){
            redisUtils.setnx(requestCountKey,1,10);
        }else{
            if(requestCount >= MAX_REQUEST_COUNT){
                UserDetail detail = new UserDetail();
                detail.setUserId(userId);
                detail.setBanned(true);
                userMapper.updateUserById(detail);
                throw new RuntimeException("你已违反SpeedCloud社区规则被封禁,请联系管理员");
            }else{
                redisUtils.increment(requestCountKey);
            }
        }
    }
}
