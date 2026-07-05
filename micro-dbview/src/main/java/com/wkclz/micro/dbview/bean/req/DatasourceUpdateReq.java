package com.wkclz.micro.dbview.bean.req;

import com.wkclz.web.bean.UpdateReq;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatasourceUpdateReq extends UpdateReq {
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
