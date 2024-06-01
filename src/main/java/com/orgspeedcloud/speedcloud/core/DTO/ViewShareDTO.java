package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Chen
 */
@Data
public class ViewShareDTO {
    @NotBlank(message = "uniqueId不能为空")
    private String uniqueId;
    private Integer nodeId;
    private String checkCode;
}
