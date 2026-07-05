package com.wkclz.micro.audit.bean.req;

import com.wkclz.web.bean.PageReq;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "变更记录分页查询请求")
public class ChangeLogPageReq extends PageReq {

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "数据ID")
    private Long dataId;

    @Schema(description = "操作类型：INSERT/UPDATE/DELETE")
    private String operateType;

    @Schema(description = "关键字搜索")
    private String keyword;

    @Schema(description = "开始时间")
    private LocalDateTime timeFrom;

    @Schema(description = "结束时间")
    private LocalDateTime timeTo;
}
