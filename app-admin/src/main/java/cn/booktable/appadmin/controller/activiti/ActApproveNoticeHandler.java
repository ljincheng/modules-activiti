package cn.booktable.appadmin.controller.activiti;

import cn.booktable.activiti.core.ActApproveEventHandler;
import cn.booktable.activiti.entity.activiti.ActInstance;
import cn.booktable.activiti.entity.activiti.ActTask;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ActApproveNoticeHandler implements ActApproveEventHandler {

    private static Logger logger= LoggerFactory.getLogger(ActApproveNoticeHandler.class);
    @Override
    public void notice(ActInstance instance, ActTask task, String status) {
        logger.info("===============####ActApproveNoticeHandler:"+status);
        logger.info("ActInstance={},ActTask={},status={}",JSON.toJSONString(instance),JSON.toJSONString(task),status);
    }
}
