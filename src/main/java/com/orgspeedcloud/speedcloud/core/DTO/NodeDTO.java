package com.orgspeedcloud.speedcloud.core.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 节点DTO,向前端隐藏了某些特殊字段,添加了文件大小字段
 * @author Chen
 */
@Data
public class NodeDTO {
    private Integer nodeId;
    private String nodeName;
    private Boolean isDirectory;
    private Integer parentId;
    private Integer fileId;
    private Long fileSize;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deleteTime;
}
