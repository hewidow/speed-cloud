package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

/**
 * @author DZB
 */
@Data
public class CheckFileDTO {
    private String md5;
    private String fullPath;
    private String nodeName;
    private Long size;
}
