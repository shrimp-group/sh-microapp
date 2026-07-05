package com.wkclz.micro.audit.bean.resp;

import com.wkclz.web.bean.EntityResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "变更记录分页响应")
public class ChangeLogPageResp extends EntityResp {

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "表名")
    private String tableName;

    @Schema(description = "数据ID")
    private Long dataId;

    @Schema(description = "数据版本")
    private Integer dataVersion;

    @Schema(description = "操作类型：INSERT/UPDATE/DELETE")
    private String operateType;

    @Schema(description = "原数据")
    private Map<String, Object> dataFromEntity;

    @Schema(description = "目标数据")
    private Map<String, Object> dataToEntity;
}
