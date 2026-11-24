package com.wkclz.micro.dag.utils;

import java.lang.management.ManagementFactory;

/**
 * 运行时辅助：进程级标识（worker id 形如 pid@host，集群内区分实例；进程存续期间不变，静态缓存）
 */
public final class RuntimeUtils {

    private static final String WORKER_ID = ManagementFactory.getRuntimeMXBean().getName();

    private RuntimeUtils() {
    }

    public static String workerId() {
        return WORKER_ID;
    }
}
