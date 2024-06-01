package com.orgspeedcloud.speedcloud.core.controller;

import com.orgspeedcloud.speedcloud.core.DTO.*;
import com.orgspeedcloud.speedcloud.core.aop.authentication.UserInformation;
import com.orgspeedcloud.speedcloud.core.aop.operationlog.OperationLog;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.service.UploadService;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author DZB
 */
@RestController
@CrossOrigin
public class UploadController {
    @Resource
    private UploadService uploadService;

    @OperationLog
    @RequestMapping("/checkFile")
    public ResponseDTO check(@RequestBody CheckFileDTO fileDTO, @UserInformation UserDetail user) {
        CloudFile file = uploadService.checkFile(fileDTO, user);
        return ResponseDTO.success(file);
    }

    @OperationLog
    @RequestMapping("/checkAgain")
    public ResponseDTO checkAgain(@RequestBody CheckAgainDTO checkAgainDTO) {
        Integer[] result = uploadService.checkFileAgain(checkAgainDTO);
        return ResponseDTO.success(result);
    }

    @OperationLog
    @RequestMapping("/upload")
    public ResponseDTO upload(UploadDTO uploadDTO, @UserInformation UserDetail user) {
        return ResponseDTO.success(uploadService.uploadFile(uploadDTO, user.getUserId(), user.getUsername()));
    }

    @OperationLog
    @RequestMapping("/getMd5")
    public String upload(MultipartFile file) {
        try {
            return DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

