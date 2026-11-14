package com.wkclz.micro.dag.core;

import com.wkclz.micro.dag.bean.enums.BackoffType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 重试间隔计算：FIXED 固定；EXPONENTIAL 第 n 次重试间隔 = intervalSec * 2^(n-1)
 */
@Slf4j
@Component
public class DagRetryCalculator {

    public LocalDateTime nextRetryTime(BackoffType backoff, int attempt, int intervalSec, LocalDateTime now) {
        log.info("DagRetryCalculator.nextRetryTime backoff={} attempt={} intervalSec={}", backoff, attempt, intervalSec);
        long sec;
        if (backoff == BackoffType.EXPONENTIAL) {
            sec = (long) intervalSec * (1L << Math.min(attempt - 1, 20)); // 防溢出，封顶 2^20
        } else {
            sec = intervalSec;
        }
        return now.plusSeconds(sec);
    }
}
