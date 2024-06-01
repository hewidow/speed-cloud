package com.orgspeedcloud.speedcloud.core.service;

import com.orgspeedcloud.speedcloud.core.DTO.PageDTO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;

import java.util.List;

/**
 * @author Chen
 */
public interface CloudFileService {
    /**
     * 分页查询文件
     * @param pageDTO 分页信息DTO
     * @return 文件列表
     */
    PageDTO queryFilePage(PageDTO pageDTO);
}

