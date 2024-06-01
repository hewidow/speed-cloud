package com.orgspeedcloud.speedcloud.core.service;

import com.orgspeedcloud.speedcloud.core.DTO.CheckCodeDTO;
import com.orgspeedcloud.speedcloud.core.DTO.PageDTO;
import com.orgspeedcloud.speedcloud.core.VO.LoginUserDetailVO;
import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;

import java.util.List;

/**
 * @author Chen
 */
public interface UserService {
    /**
     * 登录功能
     * @param user 传入的用户信息实例
     * @return 登录成功的Token与用户信息
     */
     LoginUserDetailVO login(User user);

    /**
     * 注册功能
     * @param user 需要注册的用户信息
     */
    void register(User user);

    /**
     * 生成并发送验证码
     * @param checkCodeDTO 验证码发送对象入参
     */
    void sendCheckCode(CheckCodeDTO checkCodeDTO);

    /**
     * 根据邮箱,重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     * @param checkCode 验证码
     */
    void resetPassword(String email, String newPassword, String checkCode);

    /**
     * 获取角色列表
     * @return 用户列表
     */
    List<String> getRoleList();

    /**
     * 根据分页参数获取用户列表
     * @param pageDTO 分页对象
     * @return 分页对象
     */
    PageDTO queryUserPage(PageDTO pageDTO);

    /**
     * 修改用户信息
     * @param user 修改后用户
     */
    void updateUser(UserDetail user);

    /**
     * 用户登出
     * @param user 用户信息
     */
    void logOut(UserDetail user);
}
