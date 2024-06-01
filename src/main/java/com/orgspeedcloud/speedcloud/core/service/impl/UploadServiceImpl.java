package com.orgspeedcloud.speedcloud.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.orgspeedcloud.speedcloud.core.DTO.CheckAgainDTO;
import com.orgspeedcloud.speedcloud.core.DTO.CheckFileDTO;
import com.orgspeedcloud.speedcloud.core.DTO.UploadDTO;
import com.orgspeedcloud.speedcloud.core.entity.CloudFile;
import com.orgspeedcloud.speedcloud.core.entity.Node;
import com.orgspeedcloud.speedcloud.core.entity.User;
import com.orgspeedcloud.speedcloud.core.entity.UserDetail;
import com.orgspeedcloud.speedcloud.core.mapper.CloudFileMapper;
import com.orgspeedcloud.speedcloud.core.mapper.NodeMapper;
import com.orgspeedcloud.speedcloud.core.mapper.UserMapper;
import com.orgspeedcloud.speedcloud.core.service.UploadService;
import com.orgspeedcloud.speedcloud.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ?
 *
 * @author DZB
 */
@Service
@Slf4j
public class UploadServiceImpl implements UploadService {
    public static final String NODE_ID = "node_id";
    public static final String NODE_NAME = "node_name";
    public static final String USER_ID = "user_id";
    public static final String IS_DIRECTORY = "is_directory";
    public static final String PARENT_ID = "parent_id";
    public static final String MAX_NODE_ID_AS_NODE_ID = "max(node_id) as node_id";
    public static final String FILE_ID = "file_id";
    public static final String FILE_SIZE = "file_size";
    public static final String FILE_MD_5 = "file_md5";
    public static final String REDISSON = "redisson:";
    public static final char DIRECTORY_DELIMITER = '/';

    private static final String VIDEO_HOME = "/usr/local/nginx/html/hls/";
    private static final String REPOSITORY_HOME = "/home/speedcloud-repository/";

    private static final Executor threadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));
    @Resource
    private CloudFileMapper cloudFileMapper;
    @Resource
    private NodeMapper nodeMapper;
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private Redisson redisson;

    private static final ReentrantLock LOCK = new ReentrantLock();

    @Value("${spring.servlet.multipart.location}")
    private String path;

    /**
     * 上传分片
     *
     * @return 上传是否成功
     */
    @Override
    public boolean uploadFile(UploadDTO uploadDTO, int userId, String username) {
        //如果计算的得到的md5与上传的文件md5不一样说明上传中断了
        try {
            String md5 = DigestUtils.md5DigestAsHex(uploadDTO.getFile().getBytes());
            if (!md5.equals(uploadDTO.getPartMd5())) {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 如果在redis里得不到数据说明 接下来可能有多个分片去redis中查数据 有并发问题
        if (redisUtils.get(uploadDTO.getFullMd5()) == null) {
            QueryWrapper<CloudFile> wrapper = new QueryWrapper<>();
            wrapper.select(FILE_ID)
                    .eq(FILE_MD_5, uploadDTO.getFullMd5());
            // 因为当前技术是把所有的分片都打到同一台机子上 所以这里是用单机环境
            // 单机情况下用锁来锁住
            LOCK.lock();
            try {
                // 判断DB中是否有对应的数据 如果没有就加数据
                if (cloudFileMapper.selectOne(wrapper) == null) {
                    addFile(uploadDTO.getFullMd5(), uploadDTO.getNodeName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                // 释放锁
                // 设置redis key value 过期时间
                LOCK.unlock();
                redisUtils.set(uploadDTO.getFullMd5(), 1);
                redisUtils.expire(uploadDTO.getFullMd5(), 10, TimeUnit.SECONDS);
            }
        }

        // 全部上传
        if (partUpload(uploadDTO.getFile(), uploadDTO.getFullMd5(), uploadDTO.getNum(), uploadDTO.getIndex())) {
            // 此时文件已经保存到服务器了 对分片合并
            mergeFiles(new File(path + File.separator + uploadDTO.getFullMd5()), new File(path + File.separator + uploadDTO.getFullMd5() + "-"));
            // 此时文件已经合并 往DB中cloud_file表改变文件的size 从原来的-1改到文件本来的大小
            UpdateWrapper<CloudFile> wrapper = new UpdateWrapper<>();
            wrapper.eq(FILE_MD_5, uploadDTO.getFullMd5()).set(FILE_SIZE, uploadDTO.getSize());
            cloudFileMapper.update(null, wrapper);
            if (uploadDTO.getNodeName().contains(".mp4")) {
                castToM3U8(uploadDTO.getFullMd5());
            }
            addNode(uploadDTO.getFullMd5(), uploadDTO.getFullPath(), uploadDTO.getNodeName(), userId, username);
            userMapper.decreaseUserAvailable(userId,uploadDTO.getSize());
        }
        return true;
    }

    /**
     * 调用命令行ffmpeg进行转码
     * 要将该命令放入线程池中执行,原因如下：
     * 1、为了不在主线程阻塞用户,立即给前端返回信息】
     * 2、java的runtime.exec方法在一段时间之后会挂起,如果不去读取它的输入输出流,所以必须要读取一下输出流避免挂起
     * @param fullMd5 完整Md5
     */
    private void castToM3U8(String fullMd5) {
        threadPool.execute(()->{
            try{
                String mkdir = "mkdir " + VIDEO_HOME + fullMd5;
                Runtime.getRuntime().exec(mkdir);
                String command = "ffmpeg -i "+REPOSITORY_HOME+fullMd5 +" -force_key_frames \"expr:gte(t,n_forced*1)\" -hls_time 1 -hls_list_size 0 -c:v libx264 -s 1280x720 -pix_fmt yuv420p -b:a 63k -b:v 400k -r 25 "+VIDEO_HOME+fullMd5+File.separator+fullMd5+".m3u8";
                String[] commands = new String[]{"/bin/sh","-c",command};
                Process exec = Runtime.getRuntime().exec(commands);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                String len;
                while((len = bufferedReader.readLine())!=null){
                    log.info(len);
                }
            }catch (Exception e){
                throw new RuntimeException("转码失败",e);
            }
        });
    }
    /**
     * 往数据库中加数据
     *
     * @param fullMd5  整个文件的md5
     * @param fullPath 存储路径
     * @param nodeName 存储名字
     */
    private void addNode(String fullMd5, String fullPath, String nodeName, int userId, String username) {
        // 根据传入的完整路径,添加到根目录下
        String trueFullPath = DIRECTORY_DELIMITER + username + DIRECTORY_DELIMITER;
        if ("".equals(fullPath)) {
            trueFullPath += nodeName;
        } else {
            trueFullPath += fullPath;
        }

        QueryWrapper<CloudFile> cloudFileQueryWrapper = new QueryWrapper<>();
        cloudFileQueryWrapper.select(FILE_ID).eq(FILE_MD_5, fullMd5);
        // 得到存储的fileId
        int fileId = cloudFileMapper.selectOne(cloudFileQueryWrapper).getFileId();
        // 准备文件节点
        Node node = new Node();
        node.setUserId(userId);
        node.setFullPath(trueFullPath);
        node.setNodeName(nodeName);
        node.setIsDirectory(false);
        node.setFileId(fileId);
        node.setParentId(0);
        String[] split = node.getFullPath().split("/");
        Node newNode = new Node();
        newNode.setUserId(node.getUserId());
        newNode.setIsDirectory(true);
        newNode.setFileId(null);
        newNode.setParentId(0);
        StringBuilder path = new StringBuilder();
        for (int j = 1; j < split.length - 1; j++) {
            path.append(DIRECTORY_DELIMITER);
            newNode.setNodeName(split[j]);
            QueryWrapper<Node> wrapper = new QueryWrapper<>();
            wrapper.select(NODE_ID)
                    .eq(PARENT_ID, newNode.getParentId())
                    .eq(NODE_NAME, newNode.getNodeName())
                    .eq(USER_ID, newNode.getUserId())
                    .eq(IS_DIRECTORY, true);
            Node currentNode = nodeMapper.selectOne(wrapper);
            if (currentNode == null) {
                for (int k = j; k < split.length - 1; k++) {
                    int nodeId;
                    // 下面的逻辑是为了获取最大的NodeId,把当前最大NodeId放入redis便于后续取出使用,第一次放入redis的NodeID需要从数据库查询
                    Integer id = (Integer) redisUtils.get(String.valueOf(node.getUserId()));
                    if (id == null) {
                        QueryWrapper<Node> nodeQueryWrapper = new QueryWrapper<>();
                        nodeQueryWrapper
                                .select(MAX_NODE_ID_AS_NODE_ID)
                                .eq(USER_ID, node.getUserId());
                        nodeId = nodeMapper.selectOne(nodeQueryWrapper).getNodeId() + 1;
                        redisUtils.set(String.valueOf(node.getUserId()), nodeId);
                    } else {
                        id += 1;
                        redisUtils.update(String.valueOf(node.getUserId()), id);
                        nodeId = id;
                    }
                    redisUtils.expire(String.valueOf(node.getUserId()), 10, TimeUnit.SECONDS);
                    newNode.setNodeName(split[k]);
                    newNode.setNodeId(nodeId);
                    path.append(nodeId);
                    path.append("/");
                    newNode.setFullPath(path.toString());
                    newNode.setCreateTime(new Date());
                    nodeMapper.insert(newNode);
                    newNode.setParentId(nodeId);
                }
                break;
            } else {
                int nodeId = currentNode.getNodeId();
                path.append(nodeId);
                newNode.setParentId(nodeId);
                if (j == split.length - 2) {
                    path.append("/");
                }
            }
        }
        Integer id = (Integer) redisUtils.get(String.valueOf(node.getUserId()));
        if (id == null) {
            QueryWrapper<Node> nodeQueryWrapper = new QueryWrapper<>();
            nodeQueryWrapper
                    .select(MAX_NODE_ID_AS_NODE_ID)
                    .eq(USER_ID, node.getUserId());
            id = nodeMapper.selectOne(nodeQueryWrapper).getNodeId() + 1;
            redisUtils.set(String.valueOf(node.getUserId()), id);
        } else {
            id += 1;
            redisUtils.update(String.valueOf(node.getUserId()), id);
        }
        redisUtils.expire(String.valueOf(node.getUserId()), 10, TimeUnit.SECONDS);
        newNode.setNodeName(split[split.length - 1]);
        newNode.setNodeId(id);
        newNode.setIsDirectory(false);

        if (path.length() == 0) {
            path.append("/");
        }
        path.append(id);
        path.append("/");
        newNode.setFullPath(path.toString());

        QueryWrapper<Node> getCount = new QueryWrapper<>();
        getCount.eq(PARENT_ID, node.getParentId())
                .eq(NODE_NAME, node.getNodeName())
                .eq(USER_ID, node.getUserId())
                .eq(IS_DIRECTORY, false);
        //判断是否已经有同名数据
        if (nodeMapper.selectCount(getCount) >= 1) {
            long time = System.currentTimeMillis();
            nodeName = time + nodeName;
            newNode.setNodeName(nodeName);
        }
        newNode.setFileId(fileId);
        newNode.setCreateTime(new Date());
        nodeMapper.insert(newNode);
    }

    public void addFile(String fullMd5, String nodeName) {
        CloudFile file = new CloudFile();
        int index = nodeName.lastIndexOf('.');
        // 推断文件的类型 文件大小设置为-1
        if ((index == -1)) {
            file.setFileType("NULL");
        } else {
            file.setFileType(nodeName.substring(index + 1));
        }
        file.setFileMd5(fullMd5);
        file.setFileSize(-1L);
        // 操作数据库
        cloudFileMapper.insert(file);
    }

    /**
     * 保存到服务器
     *
     * @param multipartFile 分片文件
     * @param fullMd5       整个文件md5
     * @param num           分片数量
     * @param index         第几块分片
     * @return 当全部分片上传完返回true
     */
    private boolean partUpload(MultipartFile multipartFile, String fullMd5, int num, int index) {
        // 确定保存父路径
        File parentFile = new File(path + File.separator + fullMd5);
        parentFile.mkdir();
        // 存储路径
        File childFile = new File(parentFile, fullMd5 + "-" + index);
        // 如果已经存过了就不要存了 因为这个时候可能别人也在传
        if (childFile.exists()) {
            return extracted(num, parentFile);
        }
        // 去redis中拿当前要存的分片 如果拿不到数据 说明他是第一份 如果拿到了数据说明已经存过了
        if (redisUtils.get(childFile.getName()) == null) {
            redisUtils.set(childFile.getName(), 1);
            redisUtils.expire(childFile.getName(), 360, TimeUnit.SECONDS);
        } else {
            return extracted(num, parentFile);
        }
        // 用redisson锁 锁住其他要传文件的进程 如果到这是仍有两份文件同时传进来 这就是具体措施
        RLock lock = redisson.getLock(REDISSON + childFile.getName());
        try {
            // 其中一个抢到锁
            if (lock.tryLock()) {
                if (!childFile.exists()) {
                    // 在服务器上传文件
                    multipartFile.transferTo(childFile);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return extracted(num, parentFile);
    }

    /**
     * @param num        需要的文件数
     * @param parentFile 文件目录
     * @return 是否全部上传完
     */
    private boolean extracted(int num, File parentFile) {
        File[] files = parentFile.listFiles();
        assert files != null;
        return files.length == num;
    }

    /**
     * 整合文件
     *
     * @param file       整合的文件目录
     * @param resultFile 最后输出的位置
     */
    private void mergeFiles(File file, File resultFile) {
        File[] files = file.listFiles();
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        // 对文件进行排序 按照文件名 因为传过来是有个下标的
        fileList.sort((o1, o2) -> {
            if (o1.isDirectory() && o2.isFile()) {
                return -1;
            }
            if (o1.isFile() && o2.isDirectory()) {
                return 1;
            }
            return o1.getName().compareTo(o2.getName());
        });
        String absolutePath = file.getAbsolutePath();
        try {
            // 文件输出管道
            FileChannel channel = new FileOutputStream(resultFile, true).getChannel();
            for (File partFile : fileList) {
                FileChannel fileChannel = new FileInputStream(partFile).getChannel();
                channel.transferFrom(fileChannel, channel.size(), fileChannel.size());
                fileChannel.close();
                partFile.delete();
            }
            channel.close();
            file.delete();
            resultFile.renameTo(new File(absolutePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验服务器是否存在md5码相同的文件
     *
     * @return File
     */
    @Override
    public CloudFile checkFile(CheckFileDTO fileDTO, UserDetail user) {
        if(fileDTO.getSize() > user.getAvailableSize()){
            throw new RuntimeException("用户容量不足");
        }
        QueryWrapper<CloudFile> wrapper = new QueryWrapper<>();
        wrapper.select(FILE_ID, FILE_SIZE).eq(FILE_MD_5, fileDTO.getMd5());
        CloudFile result = cloudFileMapper.selectOne(wrapper);
        if (result == null) {
            return new CloudFile();
        } else {
            addNode(fileDTO.getMd5(), fileDTO.getFullPath(), fileDTO.getNodeName(), user.getUserId(), user.getUsername());
            userMapper.decreaseUserAvailable(user.getUserId(), fileDTO.getSize());
            return result;
        }
    }


    @Override
    public Integer[] checkFileAgain(CheckAgainDTO checkAgainDTO) {
        Integer[] index = checkAgainDTO.getIndex();
        File file = new File(path + File.separator + checkAgainDTO.getFullMd5());
        //如果服务器中不存在文件夹说明什么都没存
        if (!file.exists()) {
            return new Integer[0];
        }
        ArrayList<Integer> list = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for (File file1 : files) {
            String name = file1.getName();
            String substring = name.substring(name.lastIndexOf("-") + 1);
            int i = Integer.parseInt(substring);
            index[i] += 1;
        }
        for (int i = 0; i < index.length; i++) {
            if (index[i] == 1) {
                list.add(i);
            }
        }
        return list.toArray(new Integer[0]);
    }
}
