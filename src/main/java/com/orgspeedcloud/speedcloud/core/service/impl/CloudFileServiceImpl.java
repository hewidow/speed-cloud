package com.orgspeedcloud.speedcloud.core.service.impl;

import com.orgspeedcloud.speedcloud.core.DTO.PageDTO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.CloudFileMapper;
import com.orgspeedcloud.speedcloud.core.service.CloudFileService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Chen
 */
@Service
public class CloudFileServiceImpl implements CloudFileService {
    @Resource
    private CloudFileMapper cloudFileMapper;
    @Override
    public PageDTO queryFilePage(PageDTO pageDTO) {
        Integer pageSize = pageDTO.getPageSize();
        Integer pageNumber = pageDTO.getPageNumber();
        Integer recordCount = cloudFileMapper.queryRecordCount();
        List<CloudFile> fileList;
        if (pageNumber * pageSize > recordCount) {
            fileList = cloudFileMapper.queryFilePage(Math.max(recordCount - pageSize,0), pageSize);
        } else {
            fileList = cloudFileMapper.queryFilePage((pageNumber - 1) * pageSize, pageSize);
        }
        pageDTO.setData(fileList);
        pageDTO.setTotalRecordNumber(recordCount);
        pageDTO.setTotalPageNumber(recordCount % pageSize > 0 ? recordCount / pageSize + 1 : recordCount / pageSize);
        return pageDTO;
    }
}
