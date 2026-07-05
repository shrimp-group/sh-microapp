package com.wkclz.micro.dbview.bean.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class SqlExecuteReq implements Serializable {
    @NotNull(message = "数据源ID不能为空")
    private Long datasourceId;
    @NotBlank(message = "SQL不能为空")
    private String sql;
}
