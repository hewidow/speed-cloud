package com.orgspeedcloud.speedcloud.core.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * 目录实体类
 * @author Chen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {

    /**
     * 用户ID
     */
    private Integer userId;
    /**
     * 节点ID
     */
    private Integer nodeId;
    /**
     * 是否是文件夹
     * 0为文件 1为文件夹
     */
    private Boolean isDirectory;
    /**
     * 父节点ID
     */
    private Integer parentId;
    /**
     * 完整路径
     */
    private String fullPath;
    /**
     * 节点名
     */
    private String nodeName;
    /**
     * 如果是文件,在file表中对应的文件ID
     */
    private Integer fileId;
    /**
     * 是否删除
     */
    private Boolean isDelete;
    /**
     * 删除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 删除指示是否是最外层节点
     */
    private Boolean deleteRoot;

}
