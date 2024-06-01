package com.orgspeedcloud.speedcloud.core.controller;

import com.orgspeedcloud.speedcloud.core.DTO.*;
import com.orgspeedcloud.speedcloud.core.VO.ShareNodesVO;
import com.orgspeedcloud.speedcloud.core.aop.authentication.UserInformation;
import com.orgspeedcloud.speedcloud.core.aop.operationlog.OperationLog;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.service.NodeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 用户目录改变控制器
 *
 * @author Chen
 */
@RestController
@CrossOrigin
public class NodeController {
    @Resource
    private NodeService nodeService;

    @OperationLog
    @GetMapping("/queryChildren")
    public ResponseDTO queryChildNodes(@RequestParam("nodeId") Integer nodeId, @UserInformation UserDetail user) {
        List<NodeDTO> nodes = nodeService.queryChildren(user.getUserId(), nodeId);
        return ResponseDTO.success(nodes);
    }

    @OperationLog
    @PostMapping("/createNode")
    public ResponseDTO createDirectory(@RequestBody CreateNodeDTO createNodeDTO, @UserInformation UserDetail user) {
        nodeService.createNode(user.getUserId(), createNodeDTO.getParentId(), true, null, createNodeDTO.getNodeName());
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/deleteNode")
    public ResponseDTO deleteNode(@RequestBody List<Integer> nodeIds, @UserInformation UserDetail user) {
        HashSet<Integer> set = new HashSet<>(nodeIds);
        for (Integer nodeId : set) {
            nodeService.deleteNode(user.getUserId(), nodeId);
        }
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/renameNode")
    public ResponseDTO renameNode(@RequestBody RenameNodeDTO renameNodeDTO, @UserInformation UserDetail user) {
        nodeService.renameNode(user.getUserId(), renameNodeDTO.getNodeId(), renameNodeDTO.getNewName());
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/moveNode")
    public ResponseDTO moveNodes(@RequestBody @Valid MoveNodesDTO moveNodesDTO, @UserInformation UserDetail user) {
        if (moveNodesDTO.getSrcNodeId().contains(moveNodesDTO.getDstNodeId())) {
            throw new RuntimeException("不能移动到该文件夹");
        }
        for (Integer nodeId : moveNodesDTO.getSrcNodeId()) {
            nodeService.moveNodes(user.getUserId(), nodeId, moveNodesDTO.getDstNodeId());
        }
        return ResponseDTO.success(null);
    }

    @OperationLog
    @GetMapping("/recycle")
    public ResponseDTO queryRecycleNodes(@UserInformation UserDetail user) {
        List<NodeDTO> nodes = nodeService.queryRecycleNodes(user.getUserId());
        return ResponseDTO.success(nodes);
    }

    @OperationLog
    @PostMapping("/recovery")
    public ResponseDTO recoverNodes(@RequestBody List<Integer> nodeIds, @UserInformation UserDetail user) {
        for (Integer nodeId : nodeIds) {
            nodeService.recoverNodes(user.getUserId(), nodeId);
        }
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/deleteFinal")
    public ResponseDTO deleteFinal(@RequestBody List<Integer> nodeIds, @UserInformation UserDetail user) {
        for (Integer nodeId : nodeIds) {
            nodeService.deleteNodeFinal(user.getUserId(), nodeId);
        }
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/copyNode")
    public ResponseDTO copyNode(@RequestBody MoveNodesDTO moveNodesDTO, @UserInformation UserDetail user) {
        for (Integer nodeId : moveNodesDTO.getSrcNodeId()) {
            nodeService.copyNodes(user.getUserId(), user.getUserId(), nodeId, moveNodesDTO.getDstNodeId());
        }
        return ResponseDTO.success(null);
    }

    @OperationLog
    @PostMapping("/share")
    public ResponseDTO shareNodes(@RequestBody ShareNodesDTO shareNodesDTO, @UserInformation UserDetail user) {
        ShareNodesVO shareNodes = nodeService.shareNodes(user, shareNodesDTO.getSrcNodeIds());
        return ResponseDTO.success(shareNodes);
    }

    @OperationLog
    @PostMapping("/travel")
    public ResponseDTO travelShare(@RequestBody ViewShareDTO viewShareDTO) {
        String username = nodeService.travelShare(viewShareDTO.getUniqueId());
        if(username == null){
            throw new RuntimeException("查询不到此次分享");
        }
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("username", username);
        return ResponseDTO.success(map);
    }

    @OperationLog
    @PostMapping("/check")
    public ResponseDTO checkShare(@RequestBody ViewShareDTO viewShareDTO) {
        Boolean success = nodeService.checkShare(viewShareDTO.getCheckCode(), viewShareDTO.getUniqueId());
        if (success) {
            List<NodeDTO> nodeList = nodeService.viewRoot(viewShareDTO.getUniqueId());
            return ResponseDTO.success(nodeList);
        } else {
            throw new RuntimeException("验证码错误");
        }
    }

    @OperationLog
    @PostMapping("/queryChildren")
    public ResponseDTO queryShareChildren(@RequestBody ViewShareDTO nodeIdDto) {
        List<NodeDTO> nodeDTOS = nodeService.queryShareChildren(nodeIdDto.getUniqueId(), nodeIdDto.getNodeId());
        return ResponseDTO.success(nodeDTOS);
    }

    @OperationLog
    @PostMapping("/save")
    public ResponseDTO saveFile(@RequestBody MoveNodesDTO moveNodesDTO, @UserInformation UserDetail user) {
        nodeService.copy(user.getUserId(), moveNodesDTO.getUniqueId(), moveNodesDTO.getDstNodeId(), moveNodesDTO.getSrcNodeId());
        return ResponseDTO.success(null);
    }
}
