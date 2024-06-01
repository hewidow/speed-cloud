package com.orgspeedcloud.speedcloud.core.schedule;

import com.orgspeedcloud.speedcloud.core.entity.Node;
import com.orgspeedcloud.speedcloud.core.mapper.NodeMapper;
import com.orgspeedcloud.speedcloud.core.service.NodeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 定时扫描全表删除需要清空的文件
 * @author Chen
 */
@Component
public class DeleteSchedule {
    @Resource
    NodeService nodeService;
    @Resource
    NodeMapper nodeMapper;

    private static final String CRON_EXPRESSION = "0 0 0 * * *";
    private static final long OVER_TIME = 10 * 3600 * 24 * 1000;

    @Scheduled(cron = CRON_EXPRESSION)
    public void deleteNode(){
        Node wrapper = new Node();
        wrapper.setDeleteRoot(true);
        List<Node> nodes = nodeMapper.queryNodesByNode(wrapper);
        for(Node node : nodes){
            long pastTime = System.currentTimeMillis() - node.getDeleteTime().getTime();
            if(pastTime >= OVER_TIME){
                nodeService.deleteNodeFinal(node.getUserId(), node.getFullPath());
            }
        }
    }
}
