package com.orgspeedcloud.speedcloud.core.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author Chen
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareNodesVO {
    private String checkCode;
    private String uniqueId;
}
