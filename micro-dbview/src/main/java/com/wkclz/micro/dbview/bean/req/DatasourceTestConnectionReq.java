package com.wkclz.micro.dbview.bean.req;

import lombok.Data;

import java.io.Serializable;

@Data
public class DatasourceTestConnectionReq implements Serializable {
    private Long id;
    private String datasourceName;
    private String datasourceType;
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
}
