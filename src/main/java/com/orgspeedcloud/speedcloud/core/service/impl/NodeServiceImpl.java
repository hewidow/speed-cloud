package com.orgspeedcloud.speedcloud.core.service.impl;

import com.orgspeedcloud.speedcloud.core.DTO.NodeDTO;
import com.orgspeedcloud.speedcloud.core.VO.NodeVo;
import com.orgspeedcloud.speedcloud.core.VO.ShareNodesVO;
import com.orgspeedcloud.speedcloud.core.entity.Node;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.NodeMapper;
import com.orgspeedcloud.speedcloud.core.mapper.UserMapper;
import com.orgspeedcloud.speedcloud.core.service.NodeService;
import com.orgspeedcloud.speedcloud.util.CheckCodeUtils;
import com.orgspeedcloud.speedcloud.util.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 节点服务实现类
 *
 * @author Chen
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class NodeServiceImpl implements NodeService {

    public static final String NODE_DELIMITER = ",";
    public static final String DIRECTORY_DELIMITER = "/";
    public static final String ROOT_NODE_IDS = "rootNodeIds";
    public static final String ALL_SHARE_NODES = "allShareNodes";
    public static final String CHECK_CODE = "checkCode";
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String INIT_FULL_PATH = "/1/";
    @Resource
    private NodeMapper nodeMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisUtils redisUtils;

    private static final String SHARE_UNIQUE_PREFIX = "share:";
    private static final int OVER_TIME = 3 * 3600 * 24;

    @Override
    public void createNode(Integer userId, Integer parentId, Boolean isDirectory, Integer fileId, String nodeName) {
        int nodeId = nodeMapper.queryMaxNodeId(userId) + 1;
        Node parentNode = nodeMapper.queryNode(userId, parentId);
        // 计算出新节点的完整路径
        String fullPath = parentNode.getFullPath() + nodeId + DIRECTORY_DELIMITER;
        Node newNode = new Node(userId, nodeId, isDirectory, parentId, fullPath, nodeName, fileId, false, null, new Date(), false);
        nodeMapper.insertNode(newNode);
    }

    @Override
    public void initUser(Integer userId, String username) {
        Node newNode = new Node(userId, 1, true, 0, INIT_FULL_PATH, username, null, false, null, new Date(), false);
        nodeMapper.insertNode(newNode);
    }

    @Override
    public void renameNode(Integer userId, Integer nodeId, String newName) {
        Node node = new Node();
        node.setUserId(userId);
        node.setNodeId(nodeId);
        node.setNodeName(newName);
        nodeMapper.updateNode(node);
    }

    @Override
    public List<NodeDTO> queryChildren(Integer userId, Integer directoryId) {
        return nodeMapper.queryChildNodes(userId, directoryId);
    }

    @Override
    public void deleteNode(Integer userId, Integer nodeId) {
        Node targetNode = nodeMapper.queryNode(userId, nodeId);
        List<Node> childrenNodes = nodeMapper.queryAllChildrenNoDelete(userId, nodeId, targetNode.getFullPath());
        Date deleteTime = new Date();
        targetNode.setDeleteRoot(true);
        targetNode.setIsDelete(true);
        targetNode.setDeleteTime(deleteTime);
        for (Node node : childrenNodes) {
            node.setIsDelete(true);
            node.setDeleteTime(deleteTime);
            node.setDeleteRoot(false);
        }
        childrenNodes.add(targetNode);
        nodeMapper.updateNodes(childrenNodes);
    }

    @Override
    public void moveNodes(Integer userId, Integer srcNodeId, Integer dstNodeId) {
        Node dstNode = nodeMapper.queryNode(userId, dstNodeId);
        if (dstNode == null || dstNode.getIsDelete() || !dstNode.getIsDirectory()) {
            throw new RuntimeException("目标文件夹不存在或非文件夹");
        }
        Node srcNode = nodeMapper.queryNode(userId, srcNodeId);
        List<Node> childrenNodes = nodeMapper.queryAllChildren(userId, srcNodeId, srcNode.getFullPath());
        if (childrenNodes.contains(dstNode)) {
            throw new RuntimeException("不能将父文件夹移动到子文件夹");
        }
        // 原始路径,后续使用新路径替换原始路径
        String originPath = srcNode.getFullPath();
        // 修改移动节点的parenId,其余节点只需要修改fullPath即可
        srcNode.setParentId(dstNode.getNodeId());
        srcNode.setFullPath(dstNode.getFullPath() + srcNodeId + DIRECTORY_DELIMITER);
        // 新路径前缀
        String newPrefix = srcNode.getFullPath();
        for (Node node : childrenNodes) {
            node.setFullPath(node.getFullPath().replace(originPath, newPrefix));
        }
        // 将源节点加入List一次性批量更新
        childrenNodes.add(srcNode);
        nodeMapper.updateNodes(childrenNodes);
    }

    @Override
    public void copyNodes(Integer srcUserId, Integer dstUserId, Integer srcNodeId, Integer dstNodeId) {
        Node dstNode = nodeMapper.queryNode(dstUserId, dstNodeId);
        if (dstNode == null || dstNode.getIsDelete() || !dstNode.getIsDirectory()) {
            throw new RuntimeException("目标文件夹不存在或非文件夹");
        }

        NodeVo srcNode = nodeMapper.queryNodeVo(srcUserId, srcNodeId);
        List<NodeVo> childrenNodes = nodeMapper.queryAllChildrenNodeVo(srcUserId,srcNodeId,srcNode.getFullPath());
        Long totalFileSize = 0L;
        totalFileSize += srcNode.getFileSize() == null ? 0 : srcNode.getFileSize();
        for(NodeVo nodeVo : childrenNodes){
            totalFileSize += nodeVo.getFileSize() == null ? 0 : nodeVo.getFileSize();
        }
        UserDetail userDetail = userMapper.queryByUserId(dstUserId);
        if(totalFileSize > userDetail.getAvailableSize()){
            throw new RuntimeException("用户空间不足");
        }

        // 查询到新用户的最大NodeId,为将来的生成新NodeId做准备
        Integer newNodeId = nodeMapper.queryMaxNodeId(dstUserId);

        srcNode.setUserId(dstUserId);
        srcNode.setNodeId(++newNodeId);
        srcNode.setParentId(dstNodeId);
        srcNode.setFullPath(dstNode.getFullPath() + srcNode.getNodeId() + DIRECTORY_DELIMITER);
        srcNode.setCreateTime(new Date());


        // 相同父节点的节点归为同一组,从小到大排列
        TreeMap<Integer, List<Node>> parentMap = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        // 建立<NodeID,Node>的映射,便于查找
        HashMap<Integer, Node> nodeMap = new HashMap<>(16);
        for (Node node : childrenNodes) {
            List<Node> nodes = parentMap.getOrDefault(node.getParentId(), new LinkedList<>());
            nodes.add(node);
            parentMap.put(node.getParentId(), nodes);
            nodeMap.put(node.getNodeId(), node);

            node.setUserId(dstUserId);
            node.setCreateTime(new Date());
        }
        nodeMap.put(srcNodeId, srcNode);
        LinkedList<Integer> queue = new LinkedList<>();
        queue.push(srcNodeId);
        while (!queue.isEmpty()) {
            Integer parentId = queue.poll();
            Node parentNode = nodeMap.get(parentId);
            List<Node> nodes = parentMap.get(parentId);
            if (nodes != null) {
                for (Node node : nodes) {
                    queue.push(node.getNodeId());
                    node.setParentId(parentNode.getNodeId());
                    node.setNodeId(++newNodeId);
                    node.setFullPath(parentNode.getFullPath() + newNodeId + DIRECTORY_DELIMITER);
                }
            }
        }
        childrenNodes.add(srcNode);
        nodeMapper.insertNodes(childrenNodes);
        userMapper.decreaseUserAvailable(dstUserId, totalFileSize);
    }

    @Override
    public List<NodeDTO> queryRecycleNodes(Integer userId) {
        return nodeMapper.queryDeletedNodes(userId);
    }

    @Override
    public void recoverNodes(Integer userId, Integer nodeId) {
        Node deleteNode = nodeMapper.queryNode(userId, nodeId);
        Integer parentId = deleteNode.getParentId();
        Node parentNode = nodeMapper.queryNode(userId, parentId);
        List<Node> children = nodeMapper.queryAllChildren(userId, nodeId, deleteNode.getFullPath());
        if (parentNode.getIsDelete()) {
            moveNodes(userId, nodeId, 1);
        }
        children.add(deleteNode);
        for (Node node : children) {
            node.setDeleteRoot(null);
            node.setDeleteTime(null);
            node.setIsDelete(false);
        }
        nodeMapper.updateNodes(children);
    }

    @Override
    public void deleteNodeFinal(Integer userId, String fullPath) {
        nodeMapper.deleteNodes(userId, fullPath);
    }

    @Override
    public void deleteNodeFinal(Integer userId, Integer nodeId) {
        NodeVo rootNode = nodeMapper.queryNodeVo(userId, nodeId);
        List<NodeVo> childrenNodes = nodeMapper.queryAllChildrenNodeVo(userId, nodeId, rootNode.getFullPath());
        Long totalFileSize = 0L;
        totalFileSize += rootNode.getFileSize() == null ? 0 : rootNode.getFileSize();
        for(NodeVo node : childrenNodes){
            totalFileSize += node.getFileSize() == null ? 0 : node.getFileSize();
        }
        nodeMapper.deleteNodes(userId, rootNode.getFullPath());
        userMapper.decreaseUserAvailable(userId, -totalFileSize);
    }

    @Override
    public ShareNodesVO shareNodes(UserDetail user, List<Integer> shareNodeIds) {
        // 生成唯一标识
        String uniqueId = SHARE_UNIQUE_PREFIX + System.currentTimeMillis() + user.getUserId();
        // 生成验证码
        String checkCode = CheckCodeUtils.createCode(4);
        StringBuilder sb = new StringBuilder();
        // 将所有分享的节点ID追加到sb后
        for(Integer nodeId : shareNodeIds){
            sb.append(nodeId).append(NODE_DELIMITER);
        }
        String nodeIds = sb.substring(0, sb.length() - 1);
        redisUtils.hset(uniqueId, ROOT_NODE_IDS, nodeIds);
        // 通过用户ID和分享节点的ID，一次性查询出所有分享的节点信息，这一步主要是为了将所有能访问到的节点存入，防止恶意访问未分享的文件
        List<Node> rootNodes = nodeMapper.queryNodeByIds(user.getUserId(), shareNodeIds);
        for(Node node : rootNodes){
            List<Node> children = nodeMapper.queryAllChildrenNoDelete(user.getUserId(), node.getNodeId(), node.getFullPath());
            for(Node child : children){
                sb.append(child.getNodeId()).append(",");
            }
        }
        redisUtils.hset(uniqueId, ALL_SHARE_NODES, sb.substring(0, sb.length() - 1));
        redisUtils.hset(uniqueId, CHECK_CODE,checkCode);
        redisUtils.hset(uniqueId, USER_ID, user.getUserId().toString());
        redisUtils.hset(uniqueId, USERNAME, user.getUsername());

        redisUtils.expire(uniqueId, OVER_TIME, TimeUnit.SECONDS);
        return new ShareNodesVO(checkCode,uniqueId.substring(SHARE_UNIQUE_PREFIX.length()));
    }

    @Override
    public String travelShare(String uniqueId) {
        String hashKey = SHARE_UNIQUE_PREFIX + uniqueId;
        return redisUtils.hget(hashKey, USERNAME);
    }

    @Override
    public Boolean checkShare(String checkCode, String uniqueId) {
        String hashKey = SHARE_UNIQUE_PREFIX + uniqueId;
        String target = redisUtils.hget(hashKey, CHECK_CODE);
        return target.equals(checkCode);
    }

    @Override
    public List<NodeDTO> viewRoot(String uniqueId) {
        String hashKey = SHARE_UNIQUE_PREFIX + uniqueId;
        checkShareTime(hashKey);
        Integer userId = Integer.parseInt(redisUtils.hget(hashKey, USER_ID));
        String shareNodeIds = redisUtils.hget(hashKey, ROOT_NODE_IDS);
        List<Integer> nodeIdList = toIntegerList(shareNodeIds);
        return nodeMapper.queryNodeDtoByIds(userId, nodeIdList);
    }

    @Override
    public List<NodeDTO> queryShareChildren(String uniqueId, Integer nodeId) {
        String hashKey = SHARE_UNIQUE_PREFIX + uniqueId;
        checkShareTime(hashKey);
        String allShareRoots = redisUtils.hget(hashKey, ALL_SHARE_NODES);
        Integer userId = Integer.parseInt(redisUtils.hget(hashKey, USER_ID));
        List<Integer> allShareIds = toIntegerList(allShareRoots);
        if(!allShareIds.contains(nodeId)){
            throw new RuntimeException("没有分享这个节点");
        }
        return nodeMapper.queryChildNodes(userId, nodeId);
    }

    @Override
    public void copy(Integer dstUserId, String uniqueId, Integer dstNodeId, List<Integer> srcNodeIds) {
        String hashKey = SHARE_UNIQUE_PREFIX + uniqueId;
        checkShareTime(hashKey);
        String strUserId = redisUtils.hget(hashKey, USER_ID);
        Integer srcUserId = Integer.parseInt(strUserId);
        for(Integer srcNodeId : srcNodeIds){
            copyNodes(srcUserId, dstUserId,srcNodeId, dstNodeId);
        }
    }

    private List<Integer> toIntegerList(String s){
        String[] split = s.split(NODE_DELIMITER);
        ArrayList<Integer> list = new ArrayList<>();
        for(String str : split){
            list.add(Integer.parseInt(str));
        }
        return list;
    }

    private void checkShareTime(String hashKey){
        if(!redisUtils.exists(hashKey)){
            throw new RuntimeException("分享过期了");
        }
    }
}
