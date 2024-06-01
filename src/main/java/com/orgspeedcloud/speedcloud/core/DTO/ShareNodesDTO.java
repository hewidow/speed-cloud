package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

import java.util.List;

/**
 * 分享节点DTO
 * @author Chen
 */
@Data
public class ShareNodesDTO {
    private List<Integer> srcNodeIds;
    private Integer type;
}
