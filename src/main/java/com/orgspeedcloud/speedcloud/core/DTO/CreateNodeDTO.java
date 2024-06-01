package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

/**
 * @author Chen
 */
@Data
public class CreateNodeDTO {
    /**
     * 是否是文件夹
     */
    private Boolean isDirectory;
    /**
     * 父节点ID
     */
    private Integer parentId;
    /**
     * 节点名
     */
    private String nodeName;
    /**
     * 如果是文件,在file表中对应的文件ID
     */
    private Integer fileId;
}
