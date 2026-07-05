package com.wkclz.micro.fileos.job.schedule;

import com.wkclz.micro.fileos.config.FileosConfig;
import com.wkclz.micro.fileos.job.impl.MultipartCleanupJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 分片上传过期清理 - Spring Schedule 入口
 * 当 classpath 中不存在 XxlJob 依赖时自动生效
 * 可通过 sh.fileos.multipart.cleanup.enabled 控制开关，sh.fileos.multipart.cleanup.cron 自定义 cron
 */
@Slf4j
@Component
@ConditionalOnMissingClass("com.xxl.job.core.handler.annotation.XxlJob")
public class MultipartCleanupScheduledJob {

    @Autowired
    private MultipartCleanupJob multipartCleanupJob;

    @Autowired
    private FileosConfig fileosConfig;

    @Scheduled(cron = "${sh.fileos.multipart.cleanup.cron:0 0 */1 * * ?}")
    public void cleanup() {
        if (!Boolean.TRUE.equals(fileosConfig.getMultipartCleanupEnabled())) {
            log.debug("分片上传过期清理任务已禁用");
            return;
        }
        log.info("Schedule 触发分片上传过期清理任务");
        multipartCleanupJob.cleanup();
    }

}
