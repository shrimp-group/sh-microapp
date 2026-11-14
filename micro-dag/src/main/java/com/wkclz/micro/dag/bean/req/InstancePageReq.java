package com.wkclz.micro.dag.bean.req;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实例分页查询：复用 BaseEntity 分页字段（current/size），按非空字段等值过滤
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InstancePageReq extends BaseEntity {

    @Schema(description = "实例流水号，精确匹配")
    private String instanceCode;

    @Schema(description = "跟踪编码，精确匹配")
    private String traceCode;

    @Schema(description = "状态过滤")
    private String status;

    @Schema(description = "流程编码过滤")
    private String dagCode;
}
