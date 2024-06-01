package com.orgspeedcloud.speedcloud.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.orgspeedcloud.speedcloud.core.DTO.NodeDTO;
import com.orgspeedcloud.speedcloud.core.VO.NodeVo;
import com.orgspeedcloud.speedcloud.core.entity.Node;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


/**
 * (node)
 * 数据库目录数据库操作类
 *
 * @author Chen
 */
@Mapper
public interface NodeMapper extends BaseMapper<Node> {

    /**
     * 往node表中插入一条记录
     *
     * @param node 要插入的节点
     * @return 影响的条数
     */
    Integer insertNode(@Param("node") Node node);

    /**
     * 往node表中批量插入节点
     *
     * @param nodes 要插入的节点列表
     * @return 影响条数
     */
    Integer insertNodes(@Param("nodes") List<? extends Node> nodes);

    /**
     * 获取该用户当前最大NodeId
     *
     * @param userId 用户ID
     * @return 最大节点ID
     */
    Integer queryMaxNodeId(@Param("userId") Integer userId);

    /**
     * 查询用户某个节点下的直接节点
     *
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 节点列表
     */
    List<NodeDTO> queryChildNodes(@Param("userId") Integer userId, @Param("nodeId") Integer nodeId);

    /**
     * 查询某个节点下的所有子孙节点
     *
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param fullPath 完整路径
     * @return 子孙节点列表
     */
    List<Node> queryAllChildren(@Param("userId") Integer userId, @Param("nodeId") Integer nodeId, @Param("fullPath") String fullPath);

    /**
     * 查询某个节点下的所有未删除的子孙节点
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param fullPath 完整路径
     * @return 子孙节点列表
     */
    List<Node> queryAllChildrenNoDelete(@Param("userId") Integer userId, @Param("nodeId") Integer nodeId, @Param("fullPath") String fullPath);

    /**
     * 查询某个节点
     *
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 节点信息
     */
    Node queryNode(@Param("userId") Integer userId, @Param("nodeId") Integer nodeId);

    /**
     * 批量更新节点
     * @param nodes 待更新节点
     */
    void updateNodes(@Param("nodes") List<Node> nodes);


    /**
     * 更新节点
     * @param node 待更新节点
     */
    void updateNode(@Param("node") Node node);

    /**
     * 查询在回收站的节点
     * @param userId 用户ID
     * @return 节点信息
     */
    List<NodeDTO> queryDeletedNodes(@Param("userId") Integer userId);

    /**
     * 根据节点信息,动态查询节点,动态SQL
     * @param node 节点条件
     * @return 节点
     */
    List<Node> queryNodesByNode(Node node);

    /**
     * 删除节点
     * @param userId 用户ID
     * @param fullPath 完整路径
     */
    void deleteNodes(@Param("userId") Integer userId,@Param("fullPath") String fullPath);

    /**
     * 根据节点ID去查询,给前端返回信息
     * @param userId 用户ID
     * @param nodeIds 节点ID列表
     * @return 节点信息
     */
    List<NodeDTO> queryNodeDtoByIds(@Param("userId") Integer userId, @Param("nodeIds") List<Integer> nodeIds);


    /**
     * 根据节点ID去查询
     * @param userId 用户ID
     * @param nodeIds 节点ID列表
     * @return 节点信息
     */
    List<Node> queryNodeByIds(@Param("userId") Integer userId, @Param("nodeIds") List<Integer> nodeIds);

    /**
     * 根据用户ID和节点ID去查询节点信息，包括FileSize
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @return 包括文件大小
     */
    NodeVo queryNodeVo(@Param("userId") Integer userId,@Param("nodeId")Integer nodeId);

    /**
     * 根据完整路径和用户ID查询出所有子孙节点
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param fullPath 完整路径
     * @return 子孙结点列表
     */
    List<NodeVo> queryAllChildrenNodeVo(@Param("userId") Integer userId,@Param("nodeId")Integer nodeId,@Param("fullPath") String fullPath);
}
