package com.wkclz.micro.dbview.bean.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import com.wkclz.core.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DbviewSqlHistory extends BaseEntity {

    @Schema(description = "数据源ID")
    private Long datasourceId;

    @Schema(description = "执行人")
    private String userCode;

    @Schema(description = "SQL文本")
    private String sqlText;

    @Schema(description = "SQL类型")
    private String sqlType;

    @Schema(description = "执行状态：0-失败 1-成功")
    private Integer status;

    @Schema(description = "影响行数")
    private Long affectedRows;

    @Schema(description = "耗时(ms)")
    private Long costMs;

    @Schema(description = "错误信息")
    private String errorMessage;
}
