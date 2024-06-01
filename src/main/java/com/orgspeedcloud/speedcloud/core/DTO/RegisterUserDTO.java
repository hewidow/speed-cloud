package com.orgspeedcloud.speedcloud.core.DTO;


import com.orgspeedcloud.speedcloud.core.entity.User;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 注册用户扩展类
 * @author Chen
 */
@Data
public class RegisterUserDTO extends User {
    @NotBlank(message = "验证码不能为空")
    private String checkCode;
}
