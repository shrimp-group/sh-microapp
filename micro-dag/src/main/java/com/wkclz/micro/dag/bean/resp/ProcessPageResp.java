package com.wkclz.micro.dag.bean.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 流程分页响应（含版本状态聚合） */
@Data
public class ProcessPageResp {

    @Schema(description = "流程主表 id")
    private Long id;

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "流程名称")
    private String dagName;

    @Schema(description = "聚合状态：EDITING=有草稿 / PUBLISHED=无草稿有发布版本")
    private String status;

    @Schema(description = "草稿版本 id，无草稿为 null")
    private Long draftId;

    @Schema(description = "最新已发布版本号，无发布版本为 null")
    private Integer latestVersion;

    @Schema(description = "最新已发布版本 id，无发布版本为 null")
    private Long latestPublishedId;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
