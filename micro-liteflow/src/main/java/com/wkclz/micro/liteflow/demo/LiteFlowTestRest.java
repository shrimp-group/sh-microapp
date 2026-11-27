package com.wkclz.micro.liteflow.demo;

import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.liteflow.rest.Route;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class LiteFlowTestRest {

    @Autowired
    private LiteFlowDemo liteFlowDemo;

    @GetMapping("/public/liteflow/test")
    public R<String> liteflowTest() {
        log.info("LiteFlow 测试执行");
        try {
            liteFlowDemo.run("haha");
        } catch (Exception e) {
            log.error("LiteFlow 测试执行异常", e);
            throw ValidationException.of("LiteFlow 测试执行失败: " + e.getMessage());
        }
        return R.ok("liteflow test");
    }

}
