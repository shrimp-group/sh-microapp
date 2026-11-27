package com.wkclz.micro.liteflow.demo;

import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("a")
public class ACmp extends NodeComponent {

    @Override
    public void process() {
        log.info("ACmp executed!");
    }
}
