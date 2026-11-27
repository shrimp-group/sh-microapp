package com.wkclz.micro.liteflow.demo;

import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("c")
public class CCmp extends NodeComponent {

    @Override
    public void process() {
        log.info("CCmp executed!");
    }
}
