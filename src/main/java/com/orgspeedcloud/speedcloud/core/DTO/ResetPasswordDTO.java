package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

/**
 * 重置密码入参
 * @author Chen
 */
@Data
public class ResetPasswordDTO {
    private String email;
    private String password;
    private String checkCode;
}
