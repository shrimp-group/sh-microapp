package com.wkclz.auto.bean.resp;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestReportResp implements Serializable {

    private String reportId;
    private String packagePath;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalCases;
    private Integer passedCases;
    private Integer failedCases;
    private List<TestCaseResp> testCases;

    @Data
    public static class TestCaseResp implements Serializable {
        private String caseName;
        private String status;
        private String message;
        private Long duration;
    }
}
