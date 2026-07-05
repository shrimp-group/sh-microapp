package com.wkclz.micro.dbview.bean.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DatasourceCreateReq {
    @NotBlank(message = "数据源名称不能为空")
    private String datasourceName;
    @NotBlank(message = "主机地址不能为空")
    private String host;
    private Integer port;
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String databaseName;
    private String remark;
}
