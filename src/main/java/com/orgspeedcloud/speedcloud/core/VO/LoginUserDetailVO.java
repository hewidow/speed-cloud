package com.orgspeedcloud.speedcloud.core.VO;

import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import lombok.Data;

/**
 * 用于登录接口在服务层和控制层之间传递Token和UserDetail
 * @author Chen
 */
@Data
public class LoginUserDetailVO {
    private String token;
    private UserDetail userDetail;
}
