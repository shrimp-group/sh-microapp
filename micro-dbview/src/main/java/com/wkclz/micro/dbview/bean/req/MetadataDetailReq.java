package com.wkclz.micro.dbview.bean.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class MetadataDetailReq implements Serializable {
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;
    private String schemaName;
    @NotBlank(message = "表名不能为空")
    private String tableName;
}
