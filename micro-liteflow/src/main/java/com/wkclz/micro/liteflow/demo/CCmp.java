package com.wkclz.micro.liteflow.demo;

import com.yomahub.liteflow.core.NodeComponent;
import org.springframework.stereotype.Component;

@Component("c")
public class CCmp extends NodeComponent {

    @Override
    public void process() {
        System.out.println("CCmp executed!");
    }
}
