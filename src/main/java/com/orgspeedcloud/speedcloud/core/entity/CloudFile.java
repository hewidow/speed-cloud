package com.orgspeedcloud.speedcloud.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


/**
 * @author Chen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudFile {
    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    @NotBlank(message = "文件ID不能为空")
    private Integer fileId;
    /**
     * 文件大小
     */
    private Long fileSize;
    /**
     * 文件MD5
     */
    private String fileMd5;
    /**
     * 文件类型
     */
    private String fileType;
    /**
     * 文件封禁状态
     */
    private Boolean isBanned;
}
