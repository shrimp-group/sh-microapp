package com.wkclz.micro.dbview.bean.resp;

import com.wkclz.web.bean.EntityResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatasourceResp extends EntityResp {
    private String datasourceName;
    private String datasourceType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String remark;
}
