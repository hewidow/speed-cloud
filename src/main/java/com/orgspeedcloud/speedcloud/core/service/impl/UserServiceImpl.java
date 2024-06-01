package com.orgspeedcloud.speedcloud.core.service.impl;

import com.orgspeedcloud.speedcloud.core.DTO.CheckCodeDTO;
import com.orgspeedcloud.speedcloud.core.DTO.PageDTO;
import com.orgspeedcloud.speedcloud.core.DTO.RegisterUserDTO;
import com.orgspeedcloud.speedcloud.core.VO.LoginUserDetailVO;
import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.UserMapper;
import com.orgspeedcloud.speedcloud.core.service.NodeService;
import com.orgspeedcloud.speedcloud.core.service.UserService;
import com.orgspeedcloud.speedcloud.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Chen
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class UserServiceImpl implements UserService {
    private final static String REGISTER_EMAIL_PREFIX = "register-email:";
    private final static String RESET_EMAIL_PREFIX = "reset-email:";
    private final static Integer CHECK_CODE_LENGTH = 6;
    private final static String EMAIL_SUBJECT = "速云邮件验证码";
    private final static String CHECK_CODE_TEMPLATE_NAME = "check-code.ftl";
    private final static String CHECK_CODE = "checkCode";
    private final static String OPERATION = "operation";
    private final static String OPERATION_REGISTER = "注册账号";
    private final static String OPERATION_RESET = "重置密码";
    private final static String LOGIN_TOKEN_PREFIX = "login-user-token:";
    private final static String LOGIN_ID_PREFIX = "login-user-id:";

    @Resource
    private UserMapper userMapper;
    @Resource
    private NodeService nodeService;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private MailUtils mailUtils;
    @Resource
    private FreeMarkerUtils freeMarkerUtils;

    @Override
    public LoginUserDetailVO login(User user) {
        // 查询用户是否存在
        User query = userMapper.queryByUsername(user.getUsername());
        LoginUserDetailVO loginUserDetailVO = new LoginUserDetailVO();

        // 不存在该用户 或 该用户被封禁
        if (query == null || query.getBanned()) {
            return loginUserDetailVO;
        }
        // 密码正确
        if (query.getPassword().equals(SecurityUtils.encodePassword(user.getPassword()))) {
            // 密码正确,进入生成token逻辑,有老Token重复登录需要清除,否则直接添加
            String idKey = LOGIN_ID_PREFIX + query.getUserId();
            String oldToken = (String) redisUtils.get(idKey);

            // 如果能拿到用户ID的,说明是重复登录,需要把原先保存的Token删除
            if (oldToken != null) {
                redisUtils.delete(LOGIN_TOKEN_PREFIX + oldToken);
            }
            String token = UUID.randomUUID().toString();
            String tokenKey = LOGIN_TOKEN_PREFIX + token;
            redisUtils.setnx(tokenKey, query.getUserId().toString(), 24 * 3600);
            redisUtils.setnx(idKey, token, 24 * 3600);
            loginUserDetailVO.setToken(token);
            loginUserDetailVO.setUserDetail(new UserDetail(query.getUserId(), query.getUsername(), query.getEmail(), query.getRoleName(), query.getTotalSize(), query.getAvailableSize(), query.getBanned(), query.getTheme()));
        }
        return loginUserDetailVO;
    }

    @Override
    public void register(User user) {
        RegisterUserDTO userDTO = (RegisterUserDTO) user;
        // 验证码检查
        checkRightCode(userDTO.getEmail(), userDTO.getCheckCode(), 0);
        // 加密密码
        String encodePassword = SecurityUtils.encodePassword(user.getPassword());
        user.setPassword(encodePassword);
        User checkDuplicated = userMapper.queryByEmail(user.getEmail());
        if (checkDuplicated != null) {
            throw new RuntimeException("请勿重复注册");
        }
        // 插入用户记录
        userMapper.insert(user);
        redisUtils.delete(REGISTER_EMAIL_PREFIX + user.getEmail());
        User query = userMapper.queryByEmail(user.getEmail());
        nodeService.initUser(query.getUserId(), query.getUsername());
    }


    @Override
    public void sendCheckCode(CheckCodeDTO checkCodeDTO) {
        String email = checkCodeDTO.getEmail();
        User user = userMapper.queryByEmail(email);
        if (checkCodeDTO.getType() == 0) {
            // 验证用户是否已经注册
            if (user != null) {
                throw new RuntimeException("该邮箱已被注册");
            }
        } else {
            if (user == null) {
                throw new RuntimeException("不存在该用户");
            }
        }
        // 生成验证码
        String checkCode = CheckCodeUtils.createCode(CHECK_CODE_LENGTH);
        // 准备模板数据
        HashMap<String, Object> templateModel = new HashMap<>(16);
        String redisKey;
        if (checkCodeDTO.getType() == 0) {
            redisKey = REGISTER_EMAIL_PREFIX + email;
            templateModel.put(OPERATION, OPERATION_REGISTER);
        } else {
            redisKey = RESET_EMAIL_PREFIX + email;
            templateModel.put(OPERATION, OPERATION_RESET);
        }
        templateModel.put(CHECK_CODE, checkCode);
        String templateHtml = freeMarkerUtils.makeTemplate(CHECK_CODE_TEMPLATE_NAME, templateModel);
        // 发送邮件
        mailUtils.sendEmail(email, EMAIL_SUBJECT, templateHtml, true);
        // 设置Redis过期时间
        redisUtils.setnx(redisKey, checkCode, 10 * 60);
    }

    @Override
    public void resetPassword(String email, String newPassword, String checkCode) {
        checkRightCode(email, checkCode, 1);
        String newEncodePassword = SecurityUtils.encodePassword(newPassword);
        userMapper.updatePasswordByEmail(email, newEncodePassword);
    }

    @Override
    public List<String> getRoleList() {
        ArrayList<String> roleList = new ArrayList<String>();
        roleList.add("root");
        roleList.add("normal");
        return roleList;
    }

    @Override
    public PageDTO queryUserPage(PageDTO pageDTO) {
        Integer pageSize = pageDTO.getPageSize();
        Integer pageNumber = pageDTO.getPageNumber();
        Integer recordCount = userMapper.queryRecordCount();
        List<UserDetail> userList = null;
        if (pageNumber * pageSize > recordCount) {
            userList = userMapper.queryUserDetailPage(Math.max(recordCount - pageSize,0), pageSize);
        } else {
            userList = userMapper.queryUserDetailPage((pageNumber - 1) * pageSize, pageSize);
        }
        pageDTO.setData(userList);
        pageDTO.setTotalRecordNumber(recordCount);
        pageDTO.setTotalPageNumber(recordCount % pageSize > 0 ? recordCount / pageSize + 1 : recordCount / pageSize);
        return pageDTO;
    }

    @Override
    public void updateUser(UserDetail user) {
        userMapper.updateUserById(user);
    }

    @Override
    public void logOut(UserDetail user) {
        Integer userId = user.getUserId();
        String token = (String)redisUtils.get(LOGIN_ID_PREFIX + userId);
        redisUtils.delete(LOGIN_ID_PREFIX + userId);
        redisUtils.delete(LOGIN_TOKEN_PREFIX + token);
    }


    /**
     * 校验验证码是否正确
     *
     * @param email     邮箱
     * @param checkCode 验证码
     * @param type      验证码类型
     */
    private void checkRightCode(String email, String checkCode, Integer type) {
        String redisKey;
        if (type == 0) {
            redisKey = REGISTER_EMAIL_PREFIX + email;
        } else {
            redisKey = RESET_EMAIL_PREFIX + email;
        }
        String redisCode = (String) redisUtils.get(redisKey);
        if (redisCode == null) {
            throw new RuntimeException("验证码超时");
        }
        if (!redisCode.equals(checkCode)) {
            throw new RuntimeException("验证码错误");
        }
    }


}
