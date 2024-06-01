package com.orgspeedcloud.speedcloud.core.service;

import com.orgspeedcloud.speedcloud.core.DTO.CheckAgainDTO;
import com.orgspeedcloud.speedcloud.core.DTO.CheckFileDTO;
import com.orgspeedcloud.speedcloud.core.DTO.UploadDTO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;

/**
 * @author DZB
 */
public interface UploadService {
    /**
     * 校验是否可以秒传
     * @param fileDTO 入参
     * @param user 用户信息
     * @return CloudFile
     */
    CloudFile checkFile(CheckFileDTO fileDTO, UserDetail user);

    /**
     * 校验哪些分片需要穿
     * @param checkAgainDTO 入参
     * @return Integer[]
     */
    Integer[] checkFileAgain(CheckAgainDTO checkAgainDTO);

    /**
     * 上传分片
     * @param uploadDTO 入参
     * @param userId 用户id
     * @param username 用户名字
     * @return boolean
     */
    boolean uploadFile(UploadDTO uploadDTO, int userId, String username);
}
