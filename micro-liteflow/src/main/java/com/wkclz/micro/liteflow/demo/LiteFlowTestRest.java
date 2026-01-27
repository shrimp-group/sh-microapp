package com.wkclz.micro.liteflow.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LiteFlowTestRest {

    @Autowired
    private LiteFlowDemo liteFlowDemo;

    @GetMapping("/public/liteflow/test")
    public String liteflowTest() throws Exception {
        liteFlowDemo.run("haha");
        return "liteflow test";
    }

}
