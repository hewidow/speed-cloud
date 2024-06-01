package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author DZB
 */
@Data
public class UploadDTO {
    private String partMd5;
    private MultipartFile file;
    private Integer index;
    private Integer num;
    private Long size;
    private String fullMd5;
    private String fullPath;
    private String nodeName;
}
