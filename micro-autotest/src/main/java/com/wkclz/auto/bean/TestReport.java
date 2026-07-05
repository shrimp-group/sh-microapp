package com.wkclz.auto.bean;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestReport implements Serializable {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long totalCostTimeMs;
    private int totalApiCount;
    private int successCount;
    private int failCount;
    private int errorCount;
    private List<TestCaseResult> results;
}
