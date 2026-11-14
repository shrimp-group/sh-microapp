package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExecutionLogResp {

    @Schema(description = "记录 id")
    private Long id;

    @Schema(description = "第几次执行")
    private Integer attempt;

    @Schema(description = "事件类型")
    private String event;

    @Schema(description = "详情 JSON")
    private String detailJson;

    @Schema(description = "执行机器")
    private String worker;

    @Schema(description = "发生时间")
    private LocalDateTime createTime;
}
