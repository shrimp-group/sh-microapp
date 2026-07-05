package com.wkclz.micro.liteflow.demo;

import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author shrimp
 */
@Slf4j
@Component
public class LiteFlowDemo {

    @Autowired
    private FlowExecutor flowExecutor;

    public void run(String... args) throws Exception {
        LiteflowResponse chain1 = flowExecutor.execute2Resp("chain1", args);
        if (chain1.isSuccess()){
            log.info("执行成功");
        }else{
            log.info("执行失败");
        }

    }
}
