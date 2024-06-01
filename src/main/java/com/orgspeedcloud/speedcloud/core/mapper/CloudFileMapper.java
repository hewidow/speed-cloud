package com.orgspeedcloud.speedcloud.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.orgspeedcloud.speedcloud.core.VO.DownloadVO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author Chen
 */
@Mapper
public interface CloudFileMapper extends BaseMapper<CloudFile> {

    /**
     * 根据用户ID和节点ID,查询文件的md5和文件名
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 文件MD5和文件名
     */
    DownloadVO queryFileMd5AndNodeName(@Param("userId") Integer userId, @Param("nodeId")Integer nodeId);

    /**
     * 根据用户ID和全路径,模糊查询该节点下的所有节点
     * @param userId 用户ID
     * @param fullPath 全路径
     * @return 所有子节点（包括自身）
     */
    List<DownloadVO> queryDownloadVOByUserIdAndFullPath(@Param("userId")Integer userId, @Param("fullPath")String fullPath);

    /**
     * 查询总条数
     * @return 总条数
     */
    Integer queryRecordCount();

    /**
     * 分页查询文件
     * @param offset 起始坐标
     * @param length 长度
     * @return 文件列表
     */
    List<CloudFile> queryFilePage(@Param("offset")Integer offset,@Param("length")Integer length);

    /**
     * 根据FileId查询文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    CloudFile queryFileById(@Param("fileId") Integer fileId);
}
