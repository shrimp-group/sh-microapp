package com.wkclz.micro.dag.bean.entity;

import com.wkclz.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DAG 流程版本（一行一个版本，0=草稿，发布重挂递增）
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DagProcessVersion extends BaseEntity {

    @Schema(description = "流程编码")
    private String dagCode;

    @Schema(description = "版本号，0=草稿，发布递增")
    private Integer dagVersion;

    @Schema(description = "状态：DRAFT/PUBLISHED")
    private String status;
}
