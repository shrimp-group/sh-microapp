package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.enums.BackoffType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 重试间隔单测：FIXED 固定间隔 / EXPONENTIAL 指数翻倍
 */
class DagRetryCalculatorTest {

    private final DagRetryCalculator calc = new DagRetryCalculator();

    @Test
    void fixedBackoff() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        assertEquals(base.plusSeconds(30),
            calc.nextRetryTime(BackoffType.FIXED, 1, 30, base));
        assertEquals(base.plusSeconds(30),
            calc.nextRetryTime(BackoffType.FIXED, 3, 30, base));
    }

    @Test
    void exponentialBackoff() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 0, 0, 0);
        // 第 1 次重试间隔 intervalSec=30，第 2 次 60，第 3 次 120
        assertEquals(base.plusSeconds(30), calc.nextRetryTime(BackoffType.EXPONENTIAL, 1, 30, base));
        assertEquals(base.plusSeconds(60), calc.nextRetryTime(BackoffType.EXPONENTIAL, 2, 30, base));
        assertEquals(base.plusSeconds(120), calc.nextRetryTime(BackoffType.EXPONENTIAL, 3, 30, base));
    }
}
