package com.orgspeedcloud.speedcloud.core.DTO;


import lombok.Data;

/**
 * 验证码入参对象
 * @author Chen
 */
@Data
public class CheckCodeDTO {
    /**
     * 收件人邮箱
     */
    private String email;
    /**
     * 发送验证码的类型,0:注册 1:忘记密码
     */
    private Integer type;
}
