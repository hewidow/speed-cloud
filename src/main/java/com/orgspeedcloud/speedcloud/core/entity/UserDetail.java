package com.orgspeedcloud.speedcloud.core.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 与前端交互的用户信息,隐藏了password字段
 * @author Chen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetail {
    private Integer userId;
    private String username;
    private String email;
    private String roleName;
    private Long totalSize;
    private Long availableSize;
    private Boolean banned;
    private Integer theme;
}
