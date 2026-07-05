package com.wkclz.micro.fileos.job.xxl;

import com.wkclz.micro.fileos.job.impl.MultipartCleanupJob;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * 分片上传过期清理 - XxlJob 入口
 * 当 classpath 中存在 XxlJob 依赖时自动生效，同时屏蔽 Schedule 入口
 */
@Slf4j
@Component
@ConditionalOnClass(name = "com.xxl.job.core.handler.annotation.XxlJob")
public class MultipartCleanupXxlJob {

    @Autowired
    private MultipartCleanupJob multipartCleanupJob;

    @XxlJob("fileosMultipartCleanup")
    public void cleanup() {
        log.info("XxlJob 触发分片上传过期清理任务");
        multipartCleanupJob.cleanup();
    }

}
