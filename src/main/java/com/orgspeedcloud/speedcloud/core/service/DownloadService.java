package com.orgspeedcloud.speedcloud.core.service;

import com.orgspeedcloud.speedcloud.core.DTO.DownloadDTO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Dzb
 */
public interface DownloadService {
    /**
     * 下载
     * @param nodeId 文件信息
     * @param userId 用户信息
     * @param response 返回流
     * @param isOnline 是否在线
     *
     */
    void download(Integer nodeId, Integer userId,HttpServletResponse response,Boolean isOnline);

    /**
     * 管理员查看
     * @param fileId 文件ID
     * @param response 响应流
     */
    void rootDownload(Integer fileId,HttpServletResponse response);

    /**
     * 验证用户权限
     * @param token 用户Token
     * @return 用户信息
     */
    UserDetail authentication(String token);

    /**
     * 返回完整视频URL
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 完整播放URL
     */
    String playVideo(Integer userId,Integer nodeId);

    /**
     * 根据FileId预览视频
     * @param fileId 文件Id
     * @return URL
     */
    String playVideo(Integer fileId);

    /**
     * 修改文件信息
     * @param cloudFile 文件
     */
    void updateCloudFile(CloudFile cloudFile);
}
