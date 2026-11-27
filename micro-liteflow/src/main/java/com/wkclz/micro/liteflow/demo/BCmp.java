package com.wkclz.micro.liteflow.demo;

import com.yomahub.liteflow.core.NodeComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("b")
public class BCmp extends NodeComponent {

    @Override
    public void process() {
        log.info("BCmp executed!");
    }
}
