package com.orgspeedcloud.speedcloud.core.service;

import com.orgspeedcloud.speedcloud.core.DTO.NodeDTO;
import com.orgspeedcloud.speedcloud.core.VO.ShareNodesVO;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;

import java.util.List;

/**
 * 用户目录服务
 * @author Chen
 */
public interface NodeService {
    /**
     * 创建一个节点
     * @param userId 创建文件的用户ID
     * @param parentId 父节点ID
     * @param isDirectory 是否文件夹
     * @param fileId 真实文件对应ID
     * @param nodeName 节点名
     */
    void createNode(Integer userId,Integer parentId,Boolean isDirectory,Integer fileId,String nodeName);

    /**
     * 为用户创建一个初始节点
     * @param userId 用户ID
     * @param username  用户名
     */
    void initUser(Integer userId,String username);
    /**
     * 重命名一个节点
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param newName 新节点名
     */
    void renameNode(Integer userId,Integer nodeId,String newName);

    /**
     * 查询该文件夹下的所有直接节点,给前端使用的方法,隐含了信息
     *
     * @param userId 用户ID
     * @param directoryId 文件夹ID
     * @return 文件夹中的所有记录
     */
    List<NodeDTO> queryChildren(Integer userId, Integer directoryId);

    /**
     * 删除该节点及节点下所有文件
     * @param userId 用户ID
     * @param nodeId 节点ID
     */
    void deleteNode(Integer userId,Integer nodeId);

    /**
     * 在一个用户内移动节点
     *
     * @param userId 用户ID
     * @param srcNodeId 源节点ID
     * @param dstNodeId 目标节点ID
     */
    void moveNodes(Integer userId,Integer srcNodeId, Integer dstNodeId);

    /**
     * 在不同用户间移动节点,在不同用户之间移动文件需要注意的事项有：
     *
     * 1、被移动的所有节点的用户ID需要改变
     * 2、被移动的节点不能再使用原来的NodeID,需要根据新用户的情况来生成NodeID
     * 3、每个节点的路径需要重新生成
     *
     * 实现思路:
     * 使用两个Map
     * 1、TreeMap存放父节点,ListNode,按照原目录结构的父节点ID组成Node列表
     * 2、HashMap存放NodeId,Node,按照原目录结构的NodeID对应Node节点
     * 3、使用队列通过拓扑排序的思路逐步更新节点信息
     *
     *
     * @param srcUserId 源用户ID
     * @param dstUserId 目的用户ID
     * @param srcNodeId 源用户节点
     * @param dstNodeId 目的用户节点
     */
    void copyNodes(Integer srcUserId, Integer dstUserId, Integer srcNodeId, Integer dstNodeId);

    /**
     * 查询被删除的节点
     * @param userId 用户ID
     * @return 被删除节点信息
     */
    List<NodeDTO> queryRecycleNodes(Integer userId);

    /**
     * 复原回收站中节点
     *
     * 思路：
     * 复原节点的时候查询该外层节点的父节点是否还存在，如果还存在就按原路径复原，如果不存在就复原在根目录
     *
     * @param userId 用户ID
     * @param nodeId 节点ID
     */
    void recoverNodes(Integer userId,Integer nodeId);

    /**
     * 最终删除节点
     * @param userId 用户ID
     * @param fullPath 完整路径
     */
    void deleteNodeFinal(Integer userId,String fullPath);

    /**
     * 最终删除节点
     * @param userId 用户ID
     * @param nodeId 节点ID
     */
    void deleteNodeFinal(Integer userId,Integer nodeId);

    /**
     * 分享文件生成资源唯一标识
     *
     *
     * 思路：
     * 分享文件的时候,只需要让其他用户看见该用户分享的具体文件即可,那就可以让需要分享的文件,都传入节点的ID,
     * 把节点的ID打包记录在一个Redis的Hash结构里,完整的Hash结构如下：
     *
     * 唯一标识             userId: xxx 分享文件的人
     *                    nodesId: xxx,xxx,xxx,xxx,... 分享的节点
     *                    checkCode: xxxx   提取密码
     * 将生成的唯一标识，返回给前端，后续其他用户来访问的时候,直接根据唯一标识,去redis里找寻对应的节点
     * 将节点展示给来访问的用户,然后调用Copy方法将需要保存的文件保存到自己的目录中去
     *
     * @param user 用户信息
     * @param shareNodeIds 分享文件ID
     * @return 唯一标识
     */
    ShareNodesVO shareNodes(UserDetail user, List<Integer> shareNodeIds);

    /**
     * 根据唯一ID,返回分享该节点的用户名
     * @param uniqueId 唯一ID
     * @return 节点用户名
     */
    String travelShare(String uniqueId);

    /**
     * 检验提取文件验证码是否正确
     * @param checkCode 验证码
     * @param uniqueId 唯一ID
     * @return 是否正确
     */
    Boolean checkShare(String checkCode,String uniqueId);

    /**
     * 返回该文件分享包含的所有节点
     * @param uniqueId  唯一标识
     * @return 件分享包含的所有节点信息
     */
    List<NodeDTO> viewRoot(String uniqueId);

    /**
     * 查询分享文件的子节点,要做节点验证
     * @param uniqueId 唯一标识
     * @param nodeId 查看的节点ID
     * @return 子节点
     */
    List<NodeDTO> queryShareChildren(String uniqueId, Integer nodeId);

    /**
     * 复制节点到
     * @param dstUserId 源用户ID
     * @param uniqueId 唯一标识
     * @param dstNodeId 目标节点ID
     * @param srcNodeIds 源节点ID
     */
    void copy(Integer dstUserId, String uniqueId, Integer dstNodeId,List<Integer> srcNodeIds);
}
