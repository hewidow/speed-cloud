package com.orgspeedcloud.speedcloud.core.controller;

import com.orgspeedcloud.speedcloud.core.DTO.*;
import com.orgspeedcloud.speedcloud.core.VO.LoginUserDetailVO;
import com.orgspeedcloud.speedcloud.core.aop.authentication.AuthorizationException;
import com.orgspeedcloud.speedcloud.core.aop.authentication.RequireRoles;
import com.orgspeedcloud.speedcloud.core.aop.authentication.UserInformation;
import com.orgspeedcloud.speedcloud.core.aop.operationlog.OperationLog;
import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.service.NodeService;
import com.orgspeedcloud.speedcloud.core.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

/**
 * @author Chen
 */
@RestController
@CrossOrigin
public class UserController {
    @Resource
    UserService userService;

    @OperationLog
    @PostMapping("/login")
    public ResponseDTO login(@RequestBody User user){
        LoginUserDetailVO login = userService.login(user);
        HashMap<String, Object> resultMap = new HashMap<>(16);
        String token = login.getToken();
        if(StringUtils.isNotBlank(token)){
            resultMap.put("login", true);
            resultMap.put("token", token);
            resultMap.put("userDetail", login.getUserDetail());
            return ResponseDTO.success(resultMap);
        }else{
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @OperationLog
    @PostMapping("/checkCode")
    public ResponseDTO sendCheckCode(@RequestBody CheckCodeDTO checkCodeDTO){
        userService.sendCheckCode(checkCodeDTO);
        return ResponseDTO.success(null);
    }


    @OperationLog
    @PostMapping("/register")
    public ResponseDTO register(@RequestBody RegisterUserDTO user){
        userService.register(user);
        return ResponseDTO.success(null);
    }
    @OperationLog
    @PostMapping("/reset")
    public ResponseDTO resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO){
        userService.resetPassword(resetPasswordDTO.getEmail(),resetPasswordDTO.getPassword(),resetPasswordDTO.getCheckCode());
        return ResponseDTO.success(null);
    }

    @OperationLog
    @RequireRoles(roles = {"root"})
    @GetMapping("/roles")
    public ResponseDTO getRoleList(@UserInformation UserDetail user){
        List<String> roleList = userService.getRoleList();
        return ResponseDTO.success(roleList);
    }

    @OperationLog
    @RequireRoles(roles = {"root"})
    @PostMapping("/users")
    public ResponseDTO getUserList(@RequestBody PageDTO pageDTO, @UserInformation UserDetail user){
        return ResponseDTO.success(userService.queryUserPage(pageDTO));
    }

    @OperationLog
    @RequireRoles(roles = {"root"})
    @PostMapping("/updateUser")
    public ResponseDTO updateRole(@RequestBody @Valid UserDetail updateUser, @UserInformation UserDetail user){
        userService.updateUser(updateUser);
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/updateMe")
    public ResponseDTO updateMe(@RequestBody @Valid UserDetail updateUser, @UserInformation UserDetail user){
        if(updateUser.getBanned() != null || updateUser.getRoleName() != null){
            throw new AuthorizationException();
        }
        updateUser.setUserId(user.getUserId());
        userService.updateUser(updateUser);
        return ResponseDTO.success(null);
    }
    @OperationLog
    @GetMapping("/logout")
    public ResponseDTO logOut(@UserInformation UserDetail user){
        userService.logOut(user);
        return ResponseDTO.success(null);
    }
    @OperationLog
    @GetMapping("/me")
    public ResponseDTO queryMe(@UserInformation UserDetail user){
        return ResponseDTO.success(user);
    }
}

