package com.orgspeedcloud.speedcloud.core.controller;

import com.orgspeedcloud.speedcloud.core.DTO.PageDTO;
import com.orgspeedcloud.speedcloud.core.DTO.ResponseDTO;
import com.orgspeedcloud.speedcloud.core.aop.authentication.RequireRoles;
import com.orgspeedcloud.speedcloud.core.aop.authentication.UserInformation;
import com.orgspeedcloud.speedcloud.core.aop.operationlog.OperationLog;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.service.CloudFileService;
import com.orgspeedcloud.speedcloud.core.service.DownloadService;
import org.apache.coyote.Response;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;

/**
 * @author DZB
 */
@RestController
@CrossOrigin
public class DownloadController {
    @Resource
    private DownloadService downloadService;
    @Resource
    private CloudFileService cloudFileService;

    @OperationLog
    @GetMapping("/download")
    public void download(@RequestParam("nodeId") Integer nodeId, @RequestParam("token") String token, @RequestParam("online") Integer online,HttpServletResponse response) {
        UserDetail userDetail = downloadService.authentication(token);
        downloadService.download(nodeId, userDetail.getUserId(), response, online != 0);
    }

    @OperationLog
    @GetMapping("/rootView")
    public void rootOnlinePlay(@RequestParam("fileId") Integer fileId,@RequestParam("token") String token,HttpServletResponse response){
        UserDetail userDetail = downloadService.authentication(token);
        downloadService.rootDownload(fileId,response);
    }

    @OperationLog
    @GetMapping("/playVideo")
    public ResponseDTO playVideo(@RequestParam("nodeId")Integer nodeId,@UserInformation UserDetail user){
        String url = downloadService.playVideo(user.getUserId(),nodeId);
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("url", url);
        return ResponseDTO.success(map);
    }

    @OperationLog
    @RequireRoles(roles = {"root"})
    @GetMapping("/rootPlayVideo")
    public ResponseDTO rootPlayVideo(@RequestParam("fileId")Integer fileId,@UserInformation UserDetail user){
        String url = downloadService.playVideo(fileId);
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("url", url);
        return ResponseDTO.success(map);
    }

    @OperationLog
    @RequireRoles(roles = {"root"})
    @PostMapping("/updateFile")
    public ResponseDTO updateRole(@RequestBody CloudFile cloudFile, @UserInformation UserDetail user){
        downloadService.updateCloudFile(cloudFile);
        return ResponseDTO.success(null);
    }


    @OperationLog
    @RequireRoles(roles = {"root"})
    @PostMapping("/fileList")
    public ResponseDTO queryFileList(@RequestBody PageDTO pageDTO,@UserInformation UserDetail user){
        PageDTO returnDTO = cloudFileService.queryFilePage(pageDTO);
        return ResponseDTO.success(returnDTO);
    }
}
