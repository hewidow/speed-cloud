package com.orgspeedcloud.speedcloud.util;

import org.springframework.util.DigestUtils;

/**
 * 安全工具类,主要用于密码相关
 * @author Chen
 */
public class SecurityUtils {
    private final static String SALT_VALUE = "SpeedCloud604.";
    /**
     * 生成用户密码
     * @param password 用户密码明文
     * @return 用户密码密文
     */
    public static String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex((password + SALT_VALUE).getBytes());
    }
}
