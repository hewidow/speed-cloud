package com.orgspeedcloud.speedcloud.core.VO;

import lombok.Data;

/**
 * @author Chen
 */
@Data
public class DownloadVO {
    private Integer nodeId;
    private String fullPath;
    private String fileId;
    private String fileMd5;
    private String nodeName;
    private Integer parentId;
    private Boolean isDirectory;
    private Boolean isBanned;
}
