package com.orgspeedcloud.speedcloud.core.mapper;

import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Chen
 */
@Mapper
public interface UserMapper {

    /**
     * 查询总条数
     * @return 总条数
     */
    Integer queryRecordCount();
    /**
     * 分页查询用户
     * @param offset 起始偏移量
     * @param length 页大小
     * @return User列表
     */
    List<UserDetail> queryUserDetailPage(@Param("offset") Integer offset, @Param("length") Integer length);

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户对象
     */
    User queryByUsername(@Param("username") String username);

    /**
     * 根据用户ID查询用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    UserDetail queryByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户邮箱查询用户信息
     * @param email 用户邮箱
     * @return 用户对象
     */
    User queryByEmail(@Param("email") String email);

    /**
     * 插入用户信息
     * @param user 需要插入的用户对象
     * @return 影响行数
     */
    int insert(@Param("user") User user);

    /**
     * 根据邮箱,修改用户密码
     * @param email 邮箱
     * @param newPassword 新密码
     */
    void updatePasswordByEmail(@Param("email")String email, @Param("newPassword")String newPassword);

    /**
     * 修改用户信息
     * @param user 修改后的用户信息
     */
    void updateUserById(@Param("user") UserDetail user);

    /**
     * 减少用户容量
     * @param userId 用户ID
     * @param size 减少大小
     */
    void decreaseUserAvailable(@Param("userId")Integer userId,@Param("size") Long size);
}
