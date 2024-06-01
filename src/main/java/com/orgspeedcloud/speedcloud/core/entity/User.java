package com.orgspeedcloud.speedcloud.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Chen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    @NotBlank(message = "用户名不能为空")
    private String username;
    private String password;
    private String email;
    private String roleName;
    private Long totalSize;
    private Long availableSize;
    private Boolean banned;
    private Integer theme;
}
