package com.orgspeedcloud.speedcloud.core.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 节点移动DTO
 * @author Chen
 */
@Data
public class MoveNodesDTO {
    /**
     * 源节点ID
     */
    @NotNull(message = "源节点ID不允许为空")
    private List<Integer> srcNodeId;
    /**
     * 目的节点ID
     *
     * ps: 此目的节点应是文件夹
     */
    @NotNull(message = "目标节点ID不允许为空")
    private Integer dstNodeId;


    private String uniqueId;
}
