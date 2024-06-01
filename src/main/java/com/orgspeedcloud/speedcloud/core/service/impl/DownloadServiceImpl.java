package com.orgspeedcloud.speedcloud.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.orgspeedcloud.speedcloud.core.DTO.DownloadDTO;
import com.orgspeedcloud.speedcloud.core.VO.DownloadVO;
import com.orgspeedcloud.speedcloud.core.aop.authentication.AuthorizationException;
import com.orgspeedcloud.speedcloud.core.aop.authentication.BannedException;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.Node;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.CloudFileMapper;
import com.orgspeedcloud.speedcloud.core.mapper.NodeMapper;
import com.orgspeedcloud.speedcloud.core.mapper.UserMapper;
import com.orgspeedcloud.speedcloud.core.service.DownloadService;
import com.orgspeedcloud.speedcloud.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author DZB
 */
@Service
@Slf4j

public class DownloadServiceImpl implements DownloadService {
    public static final String APPLICATION_FORCE_DOWNLOAD = "application/force-download";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String ATTACHMENT_FILE_NAME = "attachment;fileName=";
    public static final String INLINE_FILE_NAME = "inline;fileName=";
    public static final String ISO_8859_1 = "iso8859-1";
    private static final String USER_TOKEN_PREFIX = "login-user-token:";
    private final static String REQUEST_COUNT = "request-count:";
    private static final Integer MAX_REQUEST_COUNT = 50;
    @Resource
    private NodeMapper nodeMapper;
    @Resource
    private CloudFileMapper cloudFileMapper;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private UserMapper userMapper;
    @Value("${spring.servlet.multipart.location}")
    private String path;


    @Override
    public void download(Integer nodeId, Integer userId, HttpServletResponse response, Boolean isOnline) {
        Node node = nodeMapper.queryNode(userId, nodeId);
        if (node.getIsDirectory()) {
            downloadDirectory(userId, nodeId, response);
        } else {
            downloadFile(userId, nodeId,null,response, isOnline);
        }
    }

    @Override
    public void rootDownload(Integer fileId, HttpServletResponse response) {
        downloadFile(null, null,fileId,response, true);
    }

    @Override
    public UserDetail authentication(String token) {
        String userId = (String) redisUtils.get(USER_TOKEN_PREFIX + token);
        if (token == null || token.trim().length() == 0 || userId == null) {
            throw new AuthorizationException();
        }
        String requestCountKey = REQUEST_COUNT + userId;
        Integer requestCount = (Integer) redisUtils.get(requestCountKey);
        if (requestCount == null) {
            redisUtils.setnx(requestCountKey, 1, 10);
        } else {
            if (requestCount >= MAX_REQUEST_COUNT) {
                UserDetail detail = new UserDetail();
                detail.setUserId(Integer.parseInt(userId));
                detail.setBanned(true);
                userMapper.updateUserById(detail);
                throw new RuntimeException("你已违反SpeedCloud社区规则被封禁,请联系管理员");
            } else {
                redisUtils.increment(requestCountKey);
            }
        }
        UserDetail user = userMapper.queryByUserId(Integer.parseInt(userId));
        if (user.getBanned()) {
            throw new BannedException();
        }
        return user;
    }

    @Override
    public String playVideo(Integer userId, Integer nodeId) {
        DownloadVO downloadVO = cloudFileMapper.queryFileMd5AndNodeName(userId, nodeId);
        String fileMd5 = downloadVO.getFileMd5();
        String fullUrl;
        if(!downloadVO.getIsBanned()){
            fullUrl = "http://47.98.150.74/hls/" + fileMd5 + File.separator + fileMd5 + ".m3u8";
        }else{
            fullUrl = "http://47.98.150.74/hls/banned/banned.m3u8";
        }
        return fullUrl;
    }

    @Override
    public String playVideo(Integer fileId) {
        CloudFile cloudFile = cloudFileMapper.queryFileById(fileId);
        String md5 = cloudFile.getFileMd5();
        return "http://47.98.150.74/hls/" + md5 + File.separator + md5 + ".m3u8";
    }

    @Override
    public void updateCloudFile(CloudFile cloudFile) {
        cloudFileMapper.updateById(cloudFile);
    }

    private void returnResourceStream(String fileMd5,String fileName,HttpServletResponse response,Boolean isOnline){
        File file = new File(path, fileMd5);
        try (OutputStream out = response.getOutputStream();
             FileInputStream in = new FileInputStream(file)) {
            // 设置响应头
            if (isOnline) {
                response.addHeader(CONTENT_DISPOSITION, INLINE_FILE_NAME + new String(fileName.getBytes(StandardCharsets.UTF_8), ISO_8859_1));
            } else {
                response.setContentType(APPLICATION_FORCE_DOWNLOAD);
                response.addHeader(CONTENT_DISPOSITION, ATTACHMENT_FILE_NAME + new String(fileName.getBytes(StandardCharsets.UTF_8), ISO_8859_1));
            }
            byte[] bytes = new byte[1024];
            int read;
            long len = 0;
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
                len += read;
            }
            response.setContentLengthLong(len);
            out.flush();
        } catch (IOException e) {
            log.error("返回流出错", e);
            throw new RuntimeException("下载过程出错");
        }
    }
    private void downloadFile(Integer userId, Integer nodeId, Integer fileId,HttpServletResponse response, Boolean isOnline) {
        String fileMd5;
        String fileName = "temp";
        // 根据ID找到File的Md5
        if(userId == null && nodeId == null){
            CloudFile cloudFile = cloudFileMapper.queryFileById(fileId);
            fileMd5 = cloudFile.getFileMd5();
        }else{
            DownloadVO downloadVO = cloudFileMapper.queryFileMd5AndNodeName(userId, nodeId);
            fileMd5 = downloadVO.getFileMd5();
            fileName = downloadVO.getNodeName();
        }
        returnResourceStream(fileMd5,fileName, response, isOnline);
    }

    /**
     * 下载文件夹方法
     * @param userId 用户ID
     * @param nodeId 节点ID
     * @param response HTTP响应
     */
    private void downloadDirectory(Integer userId, Integer nodeId, HttpServletResponse response) {
        // 设置返回的流是强制下载的
        response.setContentType(APPLICATION_FORCE_DOWNLOAD);
        // 获取顶层文件夹信息
        Node topDirectory = nodeMapper.queryNode(userId, nodeId);
        response.setHeader(CONTENT_DISPOSITION, ATTACHMENT_FILE_NAME + topDirectory.getNodeName() + ".zip");
        // 根据顶层文件夹信息，查询出所有子孙节点
        List<DownloadVO> downloadVOList = cloudFileMapper.queryDownloadVOByUserIdAndFullPath(userId, topDirectory.getFullPath());

        HashMap<Integer, List<DownloadVO>> parentMap = new HashMap<>(16);
        HashMap<Integer,DownloadVO> voMap = new HashMap<>(16);

        // 将子孙节点的信息加入到map中，方便后续查询，并按照parentID对应一个节点列表,方便层级更新
        for (DownloadVO downloadVO : downloadVOList) {
            List<DownloadVO> list = parentMap.getOrDefault(downloadVO.getParentId(), new ArrayList<>());
            list.add(downloadVO);
            parentMap.put(downloadVO.getParentId(), list);
            voMap.put(downloadVO.getNodeId(), downloadVO);
        }

        // 设置队列，为后续拓扑顺序
        LinkedList<Integer> queue = new LinkedList<>();
        // 启动步骤，先设置顶层文件夹的路径和信息
        DownloadVO srcDirectory = voMap.get(nodeId);
        srcDirectory.setFullPath(srcDirectory.getNodeName() + File.separator);
        // 将顶层文件夹的NodeId放入队列启动拓扑
        queue.push(srcDirectory.getNodeId());

        while(!queue.isEmpty()){
            // 获取已经更新好的节点ID
            Integer parentId = queue.poll();
            // 通过已经更新好的节点ID，获取其子节点的信息
            List<DownloadVO> childNodes = parentMap.get(parentId);
            if(childNodes == null){
                continue;
            }
            // 对所有子节点更新路径，如果是文件夹还需要把文件夹的ID放进队列后续继续更新
            for(DownloadVO childNode : childNodes){
                if(childNode.getIsDirectory()){
                    childNode.setFullPath(voMap.get(parentId).getFullPath() + childNode.getNodeName() + File.separator);
                    queue.push(childNode.getNodeId());
                }else{
                    childNode.setFullPath(voMap.get(parentId).getFullPath() + childNode.getNodeName());
                }
            }
        }
        // 文件输出压缩流
        try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())){
            for(DownloadVO downloadVO : downloadVOList){
                zipOutputStream.putNextEntry(new ZipEntry(downloadVO.getFullPath()));
                if(!downloadVO.getIsDirectory()){
                    FileInputStream input = new FileInputStream(new File(path + downloadVO.getFileMd5()));
                    int read;
                    byte[] bytes = new byte[1024];
                    while((read = input.read(bytes)) != -1){
                        zipOutputStream.write(bytes,0,read);
                    }
                }
                zipOutputStream.flush();
                zipOutputStream.closeEntry();
            }
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
